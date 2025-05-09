package com.libentity.annotation.processor;

import java.lang.reflect.Method;

final class ValidationUtils {
    private ValidationUtils() {}

    static void validateHandleMethod(Method m, Class<?> stateEnum, Class<?> expectedCommandType) {
        if (m.getParameterCount() != 4) {
            throw new IllegalStateException(
                    "@Handle method must have exactly 4 parameters (state, request, command, mutator)");
        }
        Class<?>[] params = m.getParameterTypes();
        if (!params[0].equals(stateEnum)) {
            throw new IllegalStateException(
                    "@Handle method: first parameter must be the state enum type: " + stateEnum.getName());
        }
        if (!params[2].equals(expectedCommandType)) {
            throw new IllegalStateException(
                    "@Handle method: third parameter must be the command type: " + expectedCommandType.getName());
        }
        if (!com.libentity.core.state.StateMutator.class.isAssignableFrom(params[3])) {
            throw new IllegalStateException("@Handle method: fourth parameter must be StateMutator");
        }
    }

    static void validateInStateValidatorMethod(Method m, Class<?> stateEnum, Class<?> validationContextClass) {
        if (m.getParameterCount() != 3) {
            throw new IllegalStateException(
                    "InStateValidator 'validate' method must have exactly 3 parameters (state, request, ctx)");
        }
        Class<?>[] params = m.getParameterTypes();
        if (!params[0].equals(stateEnum)) {
            throw new IllegalStateException(
                    "InStateValidator: first parameter must be the state enum type: " + stateEnum.getName());
        }
        if (!params[2].equals(validationContextClass)) {
            throw new IllegalStateException("InStateValidator: third parameter must be ValidationContext");
        }
    }

    static void validateTransitionValidatorMethod(Method m, Class<?> stateEnum, Class<?> validationContextClass) {
        if (m.getParameterCount() != 4) {
            throw new IllegalStateException(
                    "TransitionValidator 'validate' method must have exactly 4 parameters (from, to, request, ctx)");
        }
        Class<?>[] params = m.getParameterTypes();
        if (!params[0].equals(stateEnum)) {
            throw new IllegalStateException(
                    "TransitionValidator: first parameter must be the state enum type: " + stateEnum.getName());
        }
        if (!params[1].equals(stateEnum)) {
            throw new IllegalStateException(
                    "TransitionValidator: second parameter must be the state enum type: " + stateEnum.getName());
        }
        if (!params[3].equals(validationContextClass)) {
            throw new IllegalStateException("TransitionValidator: fourth parameter must be ValidationContext");
        }
    }
    // More validators will be added as needed
}
