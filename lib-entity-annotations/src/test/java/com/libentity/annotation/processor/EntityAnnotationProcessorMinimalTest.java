package com.libentity.annotation.processor;

import static org.junit.jupiter.api.Assertions.*;

import com.libentity.core.entity.EntityType;
import com.libentity.core.entity.FieldDefinition;
import com.libentity.core.entity.StateTransitionValidationEntry;
import org.junit.jupiter.api.Test;

public class EntityAnnotationProcessorMinimalTest {
    @Test
    void testProcessorFindsHandlerAndValidators() {
        EntityAnnotationProcessor processor = new EntityAnnotationProcessor();
        EntityTypeRegistry registry = processor.buildEntityTypes("com.libentity.annotation.processor");

        // Test entity types
        assertTrue(registry.entityTypes().containsKey("Payment"), "EntityType for Payment should be registered");
        EntityType<PaymentState, Object> entityType = registry.entityTypes().get("Payment");
        assertNotNull(entityType, "EntityType for Payment should not be null");
        assertTrue(entityType.getActions().containsKey("submitPayment"), "Action 'submitPayment' should be registered");

        // Test command to action mapping
        assertFalse(registry.commandToActionName().isEmpty(), "Command to action mapping should not be empty");
        assertTrue(
                registry.commandToActionName().containsKey(SubmitPaymentCommand.class),
                "SubmitPaymentCommand should be in the command mapping");
        assertEquals(
                "submitPayment",
                registry.commandToActionName().get(SubmitPaymentCommand.class),
                "SubmitPaymentCommand should map to 'submitPayment' action");

        // Check field
        assertTrue(entityType.getFields().containsKey("amount"), "Field 'amount' should be registered");
        // Check in-state validator
        assertFalse(entityType.getInStateValidators().isEmpty(), "Should have in-state validators");
        // Check transition validator from DRAFT to APPROVED
        boolean hasTransitionValidator = entityType.getTransitionValidators().stream()
                .anyMatch(v -> ((StateTransitionValidationEntry<?, ?>) v).getFromState() == null
                        && ((StateTransitionValidationEntry<?, ?>) v).getToState() == null);
        assertTrue(hasTransitionValidator, "Should have a transition validator");

        // --- FIELD-LEVEL VALIDATORS TESTS ---
        // Field-level in-state and transition validators should be present on 'amount' field
        var amountField = entityType.getFields().get("amount");
        assertNotNull(amountField, "Field 'amount' should exist");
        FieldDefinition<?, ?, ?> amountFieldDef = (FieldDefinition<?, ?, ?>) amountField;
        assertEquals(
                java.math.BigDecimal.class, amountFieldDef.getType(), "Field 'amount' should have type BigDecimal");
        // InStateValidators (classic + field-level)
        assertFalse(amountFieldDef.getInStateValidators().isEmpty(), "Field 'amount' should have in-state validators");
        // TransitionValidators (classic + field-level)
        assertTrue(
                !amountFieldDef.getStateTransitionValidators().isEmpty(),
                "Field 'amount' should have transition validators");
    }

    enum DummyState {
        FOO,
        BAR
    }

    static class DummyContext {}

    // Minimal Action annotation instance for tests
    static com.libentity.annotation.Action dummyAction = new com.libentity.annotation.Action() {
        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return com.libentity.annotation.Action.class;
        }

        @Override
        public String name() {
            return "";
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public Class<?> handler() {
            return Object.class;
        }

        @Override
        public String[] allowedStates() {
            return new String[0];
        }

        @Override
        public Class<?> command() {
            return Object.class;
        }
    };

    // Negative: handle method wrong parameter count
    static class BadHandle {
        @com.libentity.annotation.Handle
        public void handle(DummyState state, Object request, Object command) {}
    }

    // Negative: in-state validator wrong signature
    static class BadInStateValidator {
        public void validate(String notState, Object req, DummyContext ctx) {}
    }

    // Negative: transition validator wrong signature
    static class BadTransitionValidator {
        public void validate(DummyState from, DummyState to, Object req, String notCtx) {}
    }

    @Test
    void testFindHandleMethod_invalidParamCount() {
        EntityAnnotationProcessor processor = new EntityAnnotationProcessor();
        Exception ex = assertThrows(
                Exception.class,
                () -> processor.findHandleMethod(BadHandle.class, DummyState.class, dummyAction, null));
        assertTrue(ex.getMessage().contains("@Handle method must have exactly 4 parameters"));
    }

    @Test
    void testFindInStateValidatorMethod_invalidSignature() {
        EntityAnnotationProcessor processor = new EntityAnnotationProcessor();
        Exception ex = assertThrows(
                Exception.class,
                () -> processor.findInStateValidatorMethod(
                        BadInStateValidator.class, DummyState.class, DummyContext.class));
        assertTrue(ex.getMessage().contains("InStateValidator: first parameter must be the state enum type"));
    }

    @Test
    void testFindTransitionValidatorMethod_invalidSignature() {
        EntityAnnotationProcessor processor = new EntityAnnotationProcessor();
        Exception ex = assertThrows(
                Exception.class,
                () -> processor.findTransitionValidatorMethod(
                        BadTransitionValidator.class, DummyState.class, DummyContext.class));
        assertTrue(ex.getMessage().contains("TransitionValidator: fourth parameter must be ValidationContext"));
    }
}
