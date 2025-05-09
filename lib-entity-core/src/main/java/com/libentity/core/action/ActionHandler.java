package com.libentity.core.action;

import com.libentity.core.state.StateMutator;

/**
 * Handler for executing an action.
 *
 * @param <S> The type of state
 * @param <R> The type of request
 * @param <C> The type of command
 */
@FunctionalInterface
public interface ActionHandler<S, R, C> {
    /**
     * Execute the action.
     *
     * @param currentState The current state of the entity
     * @param request The request that triggered this action
     * @param command The action-specific command
     * @param mutator For changing the entity's state
     */
    void execute(S currentState, R request, C command, StateMutator<S> mutator);
}
