package com.libentity.core.validation;

/**
 * Generic validator interface for entity and field validation.
 *
 * @param <S> The type of state
 * @param <E> The type of entity data
 * @param <R> The type of request
 */
@FunctionalInterface
public interface Validator<S, E, R> {
    /**
     * Validate entity data in the current state with an optional request.
     *
     * @param currentState Current state of the entity
     * @param entityData Data being validated
     * @param request Optional request data
     * @param ctx Context for collecting validation errors
     */
    void validate(S currentState, E entityData, R request, ValidationContext ctx);
}
