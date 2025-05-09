package com.libentity.core.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Builder for field definitions with a fluent API.
 *
 * @param <V> The type of the field value
 * @param <S> The type of state
 * @param <R> The type of request
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class FieldBuilder<V, S, R> {
    private final String name;
    private final Class<V> type;
    private final List<InStateValidationEntry<S, R>> inStateValidators = new ArrayList<>();
    private final List<StateTransitionValidationEntry<S, R>> stateTransitionValidators = new ArrayList<>();

    /** Add a validator for a specific state. */
    public FieldBuilder<V, S, R> validateInState(S state, InStateValidator<S, R> validator) {
        inStateValidators.add(new InStateValidationEntry<>(state, validator));
        return this;
    }

    /** Add a validator for a specific state transition. */
    public FieldBuilder<V, S, R> validateStateTransition(
            S fromState, S toState, StateTransitionValidator<S, R> validator) {
        stateTransitionValidators.add(new StateTransitionValidationEntry<>(fromState, toState, validator));
        return this;
    }

    /** Build the field definition. */
    public FieldDefinition<V, S, R> build() {
        return FieldDefinition.<V, S, R>builder()
                .name(name)
                .type(type)
                .inStateValidators(inStateValidators)
                .stateTransitionValidators(stateTransitionValidators)
                .build();
    }
}
