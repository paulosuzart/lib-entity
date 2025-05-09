package com.libentity.core.entity;

import lombok.Value;

/** A validator that runs when an entity enters a specific state. */
@Value
public class InStateValidationEntry<S, R> {
    S targetState;
    InStateValidator<S, R> validator;
}
