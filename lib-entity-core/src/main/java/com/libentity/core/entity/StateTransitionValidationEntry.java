package com.libentity.core.entity;

import lombok.Value;

/** Holds a validator for a specific state transition. */
@Value
public class StateTransitionValidationEntry<S, R> {
    S fromState;
    S toState;
    StateTransitionValidator<S, R> validator;
}
