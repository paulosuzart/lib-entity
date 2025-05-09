package com.libentity.core.entity;

import com.libentity.core.validation.ValidationContext;

/**
 * A validator that runs during a specific state transition.
 *
 * @param <S> The type of state
 * @param <R> The type of request
 */
@FunctionalInterface
public interface StateTransitionValidator<S, R> {
    /**
     * Validate a state transition.
     *
     * @param fromState The state being transitioned from
     * @param toState The state being transitioned to
     * @param request The request
     * @param ctx The validation context
     */
    void validate(S fromState, S toState, R request, ValidationContext ctx);
}
