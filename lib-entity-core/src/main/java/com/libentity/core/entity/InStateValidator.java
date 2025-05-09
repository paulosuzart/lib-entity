package com.libentity.core.entity;

import com.libentity.core.validation.ValidationContext;

/**
 * Validates an entity when it reaches a specific state.
 *
 * @param <S> The type of state
 * @param <R> The type of request
 */
public interface InStateValidator<S, R> {
    /**
     * Validate the entity in the given state.
     *
     * @param state The current state
     * @param request The request that triggered this validation
     * @param ctx Context for collecting validation errors
     */
    void validate(S state, R request, ValidationContext ctx);
}
