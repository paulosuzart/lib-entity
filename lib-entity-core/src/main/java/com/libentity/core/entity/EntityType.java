package com.libentity.core.entity;

import com.libentity.core.action.ActionBuilder;
import com.libentity.core.action.ActionDefinition;
import com.libentity.core.validation.ValidationContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Getter;

/**
 * Defines an entity type with its fields, validators, and actions.
 *
 * @param <S> The type of state
 * @param <R> The type of request
 */
@Getter
public class EntityType<S, R> {
    private final String name;
    private final Map<String, FieldDefinition<?, S, R>> fields;
    private final Map<S, List<InStateValidator<S, R>>> inStateValidators;
    private final List<StateTransitionValidationEntry<S, R>> transitionValidators;
    private final Map<String, ActionDefinition<S, R, ?>> actions;

    public EntityType(
            String name,
            Map<String, FieldDefinition<?, S, R>> fields,
            Map<S, List<InStateValidator<S, R>>> inStateValidators,
            List<StateTransitionValidationEntry<S, R>> transitionValidators,
            Map<String, ActionDefinition<S, R, ?>> actions) {
        this.name = name;
        this.fields = fields;
        this.inStateValidators = inStateValidators;
        this.transitionValidators = transitionValidators;
        this.actions = actions;
    }

    /** Start building a new entity type. */
    public static <S, R> EntityTypeBuilder<S, R> builder(String name) {
        return new EntityTypeBuilder<>(name);
    }

    /** Validate the entity in a specific state. */
    public void validateState(S state, R request, ValidationContext ctx) {
        // Validate fields first
        for (FieldDefinition<?, S, R> field : fields.values()) {
            field.validateInState(state, request, ctx);
        }

        // Then run state validators
        // TODO Should we support state validators at entity level?
        List<InStateValidator<S, R>> validators = inStateValidators.get(state);
        if (validators != null) {
            for (InStateValidator<S, R> validator : validators) {
                validator.validate(state, request, ctx);
            }
        }
    }

    /** Validate a state transition. */
    public void validateTransition(S fromState, S toState, R request, ValidationContext ctx) {
        // Validate field transitions first
        for (FieldDefinition<?, S, R> field : fields.values()) {
            field.validateStateTransition(fromState, toState, request, ctx);
        }

        // Then run transition validators
        // TODO Should we support transition validators at entity level?
        for (StateTransitionValidationEntry<S, R> entry : transitionValidators) {
            if (entry.getFromState().equals(fromState) && entry.getToState().equals(toState)) {
                entry.getValidator().validate(fromState, toState, request, ctx);
            }
        }
    }

    /** Builder for entity types with a fluent API. */
    public static class EntityTypeBuilder<S, R> {
        private final String name;
        private final Map<String, FieldDefinition<?, S, R>> fields = new HashMap<>();
        private final Map<S, List<InStateValidator<S, R>>> inStateValidators = new HashMap<>();
        private final List<StateTransitionValidationEntry<S, R>> transitionValidators = new ArrayList<>();
        private final Map<String, ActionDefinition<S, R, ?>> actions = new HashMap<>();

        private EntityTypeBuilder(String name) {
            this.name = name;
        }

        /** Add a field to the entity type. */
        public <V> EntityTypeBuilder<S, R> field(String name, Class<V> type, Consumer<FieldBuilder<V, S, R>> config) {
            FieldBuilder<V, S, R> builder = new FieldBuilder<>(name, type);
            config.accept(builder);
            fields.put(name, builder.build());
            return this;
        }

        /** Add a validator for a specific state. */
        public EntityTypeBuilder<S, R> validateInState(S state, InStateValidator<S, R> validator) {
            inStateValidators.computeIfAbsent(state, k -> new ArrayList<>()).add(validator);
            return this;
        }

        /** Add a validator for state transitions. */
        public EntityTypeBuilder<S, R> validateTransition(
                S fromState, S toState, StateTransitionValidator<S, R> validator) {
            transitionValidators.add(new StateTransitionValidationEntry<>(fromState, toState, validator));
            return this;
        }

        /** Add an action to the entity type. */
        public <C> EntityTypeBuilder<S, R> action(String name, Consumer<ActionBuilder<S, R, C>> config) {
            ActionBuilder<S, R, C> builder = new ActionBuilder<>(name);
            config.accept(builder);
            actions.put(name, builder.build());
            return this;
        }

        /** Build the entity type. */
        public EntityType<S, R> build() {
            return new EntityType<>(
                    name,
                    new HashMap<>(fields),
                    new HashMap<>(inStateValidators),
                    new ArrayList<>(transitionValidators),
                    new HashMap<>(actions));
        }
    }
}
