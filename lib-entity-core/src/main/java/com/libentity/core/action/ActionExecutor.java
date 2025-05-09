package com.libentity.core.action;

import com.libentity.core.validation.ValidationContext;
import java.util.List;

/**
 * Interface for executing actions on entities with proper validation and state management.
 * Different implementations can provide synchronous or asynchronous execution strategies.
 *
 * @param <S> The type of state
 * @param <R> The type of request
 */
public interface ActionExecutor<S, R> {
    /**
     * Executes an action and returns the result.
     *
     * @param currentState The current state
     * @param request The request context
     * @param ctx The validation context
     * @param command The action command (must implement ActionCommand)
     * @param <C> The type of the action command
     * @return The action result containing the new state, request and command
     */
    <C> ActionResult<S, R, C> execute(S currentState, R request, ValidationContext ctx, C command);

    /**
     * Returns the names of all actions whose onlyIf predicate returns true for the given state, entity data, and request.
     */
    List<String> getAllowedActions(S currentState, R request);
}
