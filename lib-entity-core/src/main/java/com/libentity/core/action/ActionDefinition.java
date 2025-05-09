package com.libentity.core.action;

import com.libentity.core.entity.EntityType;
import com.libentity.core.state.StateMutator;
import com.libentity.core.validation.ValidationContext;
import com.libentity.core.validation.ValidationError;
import com.libentity.core.validation.ValidationException;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

/**
 * Defines an action that can be performed on an entity.
 *
 * @param <S> The type of state
 * @param <R> The type of request
 * @param <C> The type of command
 */
@Builder
@Getter
public class ActionDefinition<S, R, C> {
    private final String name;
    private final String description;
    private final Set<S> allowedStates;
    private final ActionAvailabilityPredicate<S, R, C> onlyIf;
    private final ActionHandler<S, R, C> handler;

    /**
     * Constructor for ActionDefinition.
     *
     * @param name            The name of the action.
     * @param description     The description of the action.
     * @param allowedStates   The set of states in which the action is allowed.
     * @param onlyIf          The predicate that determines if the action is allowed.
     * @param handler         The handler that executes the action.
     */
    public ActionDefinition(
            String name,
            String description,
            Set<S> allowedStates,
            ActionAvailabilityPredicate<S, R, C> onlyIf,
            ActionHandler<S, R, C> handler) {
        this.name = name;
        this.description = description;
        this.allowedStates = allowedStates;
        this.onlyIf = onlyIf;
        this.handler = handler;
    }

    /** Execute this action. */
    public void execute(
            S currentState,
            R request,
            C command,
            ValidationContext ctx,
            StateMutator<S> mutator,
            EntityType<S, R> entityType) {
        // Check if action can be executed
        if (allowedStates != null && allowedStates.isEmpty()) {
            if (onlyIf != null && !onlyIf.test(currentState, request, command)) {
                throw new ValidationException(List.of(ValidationError.builder()
                        .code("ACTION_NOT_ALLOWED")
                        .defaultMessage("Action " + name + " is not allowed in current state")
                        .build()));
            }
        } else {
            if (allowedStates != null && !allowedStates.contains(currentState)
                    || (onlyIf != null && !onlyIf.test(currentState, request, command))) {
                throw new ValidationException(List.of(ValidationError.builder()
                        .code("ACTION_NOT_ALLOWED")
                        .defaultMessage("Action " + name + " is not allowed in current state")
                        .build()));
            }
        }

        // Execute the action handler
        handler.execute(currentState, request, command, mutator);

        // If state changed, validate the transition and new state
        S targetState = mutator.getState();
        if (!targetState.equals(currentState)) {
            entityType.validateTransition(currentState, targetState, request, ctx);
            if (!ctx.hasErrors()) {
                entityType.validateState(targetState, request, ctx);
            }
        }

        // If validation passed, apply the state change
        if (!ctx.hasErrors() && !mutator.isUpdated()) {
            mutator.setState(targetState);
        }
    }
}
