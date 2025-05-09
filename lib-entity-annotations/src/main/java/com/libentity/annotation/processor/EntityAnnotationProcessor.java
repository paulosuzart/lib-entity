package com.libentity.annotation.processor;

import com.libentity.annotation.*;
import com.libentity.core.action.ActionDefinition;
import com.libentity.core.entity.EntityType;
import com.libentity.core.entity.FieldDefinition;
import com.libentity.core.entity.InStateValidator;
import com.libentity.core.entity.StateTransitionValidationEntry;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Method;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityAnnotationProcessor {

    public record Options(InstanceFactory instanceFactory) {}

    @FunctionalInterface
    public interface InstanceFactory {
        <T> T getInstance(Class<T> clazz);
    }

    private final InstanceFactory instanceFactory;

    public EntityAnnotationProcessor() {
        this(new Options(new DefaultInstanceFactory()));
    }

    public EntityAnnotationProcessor(Options options) {
        this.instanceFactory = options.instanceFactory;
        log.debug(
                "EntityAnnotationProcessor constructed with InstanceFactory: {}",
                instanceFactory.getClass().getName());
        if (instanceFactory instanceof DefaultInstanceFactory) {
            log.debug("Using DefaultInstanceFactory (reflection-based instantiation)");
        } else {
            log.debug("Using custom InstanceFactory implementation");
        }
    }

    // Helper to find and validate the @Handle method
    Method findHandleMethod(
            Class<?> handlerClass, Class<? extends Enum<?>> stateEnum, Action actionAnn, Class<?> entityClass) {
        Method handleMethod = null;
        for (Method m : handlerClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(com.libentity.annotation.Handle.class)) {
                ValidationUtils.validateHandleMethod(m, stateEnum, actionAnn.command());
                handleMethod = m;
                break;
            }
        }
        if (handleMethod == null) {
            throw new IllegalStateException(
                    "No @Handle method found in action handler class " + handlerClass.getName());
        }
        return handleMethod;
    }

    // Helper to find and validate the validate method for in-state validators
    Method findInStateValidatorMethod(
            Class<?> validatorClass, Class<? extends Enum<?>> stateEnum, Class<?> validationContextClass) {
        Method validateMethod = null;
        for (Method m : validatorClass.getDeclaredMethods()) {
            if (m.getName().equals("validate")) {
                ValidationUtils.validateInStateValidatorMethod(m, stateEnum, validationContextClass);
                validateMethod = m;
                break;
            }
        }
        if (validateMethod == null) {
            throw new IllegalStateException(
                    "No 'validate' method found in in-state validator class " + validatorClass.getName());
        }
        return validateMethod;
    }

    // Helper to find and validate the validate method for transition validators
    Method findTransitionValidatorMethod(
            Class<?> validatorClass, Class<? extends Enum<?>> stateEnum, Class<?> validationContextClass) {
        Method validateMethod = null;
        for (Method m : validatorClass.getDeclaredMethods()) {
            if (m.getName().equals("validate")) {
                ValidationUtils.validateTransitionValidatorMethod(m, stateEnum, validationContextClass);
                validateMethod = m;
                break;
            }
        }
        if (validateMethod == null) {
            throw new IllegalStateException(
                    "No 'validate' method found in transition validator class " + validatorClass.getName());
        }
        return validateMethod;
    }

    public EntityTypeRegistry buildEntityTypes(String... basePackages) {
        Map<String, EntityType> entityTypes = new HashMap<>();
        Map<Class<?>, String> commandToActionName = new HashMap<>();
        log.debug("Starting entity type scan for packages: {}", Arrays.toString(basePackages));
        try (ScanResult scanResult =
                new ClassGraph().enableAllInfo().acceptPackages(basePackages).scan()) {
            List<Class<?>> entityClasses = scanResult
                    .getClassesWithAnnotation(EntityDefinition.class.getName())
                    .loadClasses();
            log.debug("Found {} entities: {}", entityClasses.size(), entityClasses);
            for (Class<?> entityClass : entityClasses) {
                EntityDefinition entityAnn = entityClass.getAnnotation(EntityDefinition.class);
                String entityName = entityAnn.name();
                log.debug("Processing entity: {} (class: {})", entityName, entityClass.getName());
                Class<? extends Enum<?>> stateEnum = entityAnn.stateEnum();
                Map<String, FieldDefinition> fields = new HashMap<>();
                for (Field fieldAnn : entityAnn.fields()) {
                    // FIELD-LEVEL IN-STATE VALIDATORS
                    List<InStateValidator> fieldInStateValidators = new ArrayList<>();
                    for (Class<?> validatorClass : fieldAnn.inStateValidators()) {
                        try {
                            Object validatorInstance = instanceFactory.getInstance(validatorClass);
                            Method validateMethod = findInStateValidatorMethod(
                                    validatorClass, stateEnum, com.libentity.core.validation.ValidationContext.class);
                            fieldInStateValidators.add((state, request, ctx) -> {
                                try {
                                    validateMethod.setAccessible(true);
                                    validateMethod.invoke(validatorInstance, state, request, ctx);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    "Failed to instantiate field in-state validator: " + validatorClass, e);
                        }
                    }

                    // FIELD-LEVEL TRANSITION VALIDATORS
                    List<StateTransitionValidationEntry> fieldTransitionValidators = new ArrayList<>();
                    for (Class<?> validatorClass : fieldAnn.transitionValidators()) {
                        try {
                            Object validatorInstance = instanceFactory.getInstance(validatorClass);
                            Method validateMethod = findTransitionValidatorMethod(
                                    validatorClass, stateEnum, com.libentity.core.validation.ValidationContext.class);
                            fieldTransitionValidators.add(
                                    new StateTransitionValidationEntry(null, null, (from, to, request, ctx) -> {
                                        try {
                                            validateMethod.setAccessible(true);
                                            validateMethod.invoke(validatorInstance, from, to, request, ctx);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    "Failed to instantiate field transition validator: " + validatorClass, e);
                        }
                    }

                    fields.put(
                            fieldAnn.name(),
                            new FieldDefinition(
                                    fieldAnn.name(),
                                    fieldAnn.type(),
                                    fieldInStateValidators,
                                    fieldTransitionValidators));
                }
                Map<String, ActionDefinition> actions = new HashMap<>();
                log.debug("Entity '{}' has {} actions", entityName, entityAnn.actions().length);
                for (Action actionAnn : entityAnn.actions()) {
                    Set<String> allowedStates = new HashSet<>(Arrays.asList(actionAnn.allowedStates()));
                    Class<?> handlerClass = actionAnn.handler();
                    // Map command class to action name
                    commandToActionName.put(actionAnn.command(), actionAnn.name());
                    log.debug(
                            "  Found action: '{}' (handler: {}, command: {})",
                            actionAnn.name(),
                            handlerClass.getName(),
                            actionAnn.command().getName());
                    Method onlyIfMethod = null;
                    for (Method m : handlerClass.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(com.libentity.annotation.OnlyIf.class)) {
                            if (m.getParameterCount() != 3) {
                                throw new IllegalStateException(
                                        "@OnlyIf method must have exactly 3 parameters (state, request, command)");
                            }
                            onlyIfMethod = m;
                            break;
                        }
                    }
                    Method handleMethod = findHandleMethod(handlerClass, entityAnn.stateEnum(), actionAnn, entityClass);
                    Method finalOnlyIfMethod = onlyIfMethod;
                    //noinspection rawtypes
                    actions.put(
                            actionAnn.name(),
                            new ActionDefinition(
                                    actionAnn.name(),
                                    actionAnn.description(),
                                    allowedStates,
                                    (state, req, cmd) -> {
                                        if (finalOnlyIfMethod != null) {
                                            try {
                                                Object handlerInstance = instanceFactory.getInstance(handlerClass);
                                                finalOnlyIfMethod.setAccessible(true);
                                                Object result =
                                                        finalOnlyIfMethod.invoke(handlerInstance, state, req, cmd);
                                                return result instanceof Boolean ? (Boolean) result : false;
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        // TODO offer as a configuration option
                                        return true;
                                    },
                                    (state, req, cmd, mutator) -> {
                                        try {
                                            Object handlerInstance = instanceFactory.getInstance(handlerClass);
                                            handleMethod.setAccessible(true);
                                            handleMethod.invoke(handlerInstance, state, req, cmd, mutator);
                                        } catch (Exception e) {
                                            throw new RuntimeException(
                                                    "Failed to execute action handler: " + handlerClass, e);
                                        }
                                    }));
                }
                @SuppressWarnings("rawtypes")
                List<InStateValidator> inStateValidators = new ArrayList<>();
                for (Class<?> validatorClass : entityAnn.inStateValidators()) {
                    try {
                        Object validatorInstance = instanceFactory.getInstance(validatorClass);
                        Method validateMethod = findInStateValidatorMethod(
                                validatorClass,
                                entityAnn.stateEnum(),
                                com.libentity.core.validation.ValidationContext.class);
                        inStateValidators.add((state, request, ctx) -> {
                            try {
                                validateMethod.setAccessible(true);
                                validateMethod.invoke(validatorInstance, state, request, ctx);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate in-state validator: " + validatorClass, e);
                    }
                }
                @SuppressWarnings("rawtypes")
                List<StateTransitionValidationEntry> transitionValidators = new ArrayList<>();
                for (Class<?> validatorClass : entityAnn.transitionValidators()) {
                    try {
                        Object validatorInstance = instanceFactory.getInstance(validatorClass);
                        Method validateMethod = findTransitionValidatorMethod(
                                validatorClass,
                                entityAnn.stateEnum(),
                                com.libentity.core.validation.ValidationContext.class);
                        //noinspection rawtypes
                        transitionValidators.add(
                                new StateTransitionValidationEntry(null, null, (from, to, request, ctx) -> {
                                    try {
                                        validateMethod.setAccessible(true);
                                        validateMethod.invoke(validatorInstance, from, to, request, ctx);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to instantiate transition validator: " + validatorClass, e);
                    }
                }
                @SuppressWarnings("rawtypes")
                Map<Object, List<InStateValidator>> inStateValidatorMap = new HashMap<>();
                inStateValidatorMap.put(null, inStateValidators);
                //noinspection rawtypes
                EntityType entityType =
                        new EntityType(entityName, fields, inStateValidatorMap, transitionValidators, actions);
                entityTypes.put(entityName, entityType);
            }
        }
        return new EntityTypeRegistry(entityTypes, commandToActionName);
    }

    private static class DefaultInstanceFactory implements InstanceFactory {
        @Override
        public <T> T getInstance(Class<T> clazz) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
