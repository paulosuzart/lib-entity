package com.libentity.core.action;

import com.libentity.core.entity.EntityType;
import com.libentity.core.state.SingleChangeStateMutator;
import com.libentity.core.state.StateMutator;
import com.libentity.core.validation.ValidationContext;
import com.libentity.core.validation.ValidationException;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Synchronous implementation of ActionExecutor that executes actions immediately in the same thread.
 *
 * @param <S> The type of state
 * @param <R> The type of request
 */
@Builder
@RequiredArgsConstructor
@Slf4j
public class SyncActionExecutor<S, R> implements ActionExecutor<S, R> {
    /**
     * The entity type to execute actions on.
     */
    private final EntityType<S, R> entityType;
    /**
     * A supplier of state mutators for the entity type. This can be used to
     * customize the state management strategy.
     */
    @Builder.Default
    private final Function<S, StateMutator<S>> stateMutatorSupplier = SingleChangeStateMutator::new;

    @Builder.Default
    private final Function<Object, String> commandToActionResolver = (command) -> {
        if (command instanceof ActionCommand actionCommand) {
            return actionCommand.getActionName();
        }
        throw new IllegalArgumentException("Command must implement ActionCommand");
    };

    @Override
    public <C> ActionResult<S, R, C> execute(S currentState, R request, ValidationContext ctx, C command) {
        log.debug(
                "Trying to execute action for command {} - {} for {}",
                command.getClass(),
                currentState,
                entityType.getName());
        String actionName = commandToActionResolver.apply(command);
        log.debug("{}/{}#{} - Action name detected", entityType.getName(), currentState, actionName);
        @SuppressWarnings("unchecked")
        ActionDefinition<S, R, C> action =
                (ActionDefinition<S, R, C>) entityType.getActions().get(actionName);
        if (action == null) {
            throw new IllegalArgumentException("No action defined for '" + actionName + "'");
        }
        StateMutator<S> stateHolder = stateMutatorSupplier.apply(currentState);
        action.execute(currentState, request, command, ctx, stateHolder, entityType);
        log.debug(
                "{}/{}#{} Action executed. Has errors: {}",
                entityType.getName(),
                currentState,
                actionName,
                ctx.hasErrors());
        if (ctx.hasErrors()) {
            log.debug(
                    "Action executed detected state before {} and after {} for {}",
                    currentState,
                    stateHolder.getState(),
                    entityType.getName());
            throw new ValidationException(ctx.getErrors());
        }
        log.debug(
                "{}/{}#{} Returning result Action. New state {}",
                entityType.getName(),
                currentState,
                actionName,
                stateHolder.getState());
        return new ActionResult<>(stateHolder.getState(), request, command);
    }

    @Override
    public List<String> getAllowedActions(S currentState, R request) {
        return entityType.getActions().values().stream()
                .filter(action -> {
                    // Only allow if allowedStates contains currentState
                    if (action.getAllowedStates() != null
                            && !action.getAllowedStates().isEmpty()
                            && !action.getAllowedStates().contains(currentState)) {
                        return false;
                    }
                    // Only allow if onlyIf is null or returns true
                    var onlyIf = action.getOnlyIf();
                    if (onlyIf == null) return true;
                    // Command is not available in this context, so pass null
                    return onlyIf.test(currentState, request, null);
                })
                .map(ActionDefinition::getName)
                .toList();
    }
}
