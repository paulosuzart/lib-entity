package com.libentity.core.entity;

import com.libentity.core.validation.ValidationContext;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Defines a field in an entity.
 *
 * @param <V> The type of the field value
 * @param <S> The type of state
 * @param <R> The type of request
 */
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FieldDefinition<V, S, R> {
    private final String name;
    private final Class<V> type;
    private final List<InStateValidationEntry<S, R>> inStateValidators;
    private final List<StateTransitionValidationEntry<S, R>> stateTransitionValidators;

    /** Validate the field value in the given state. */
    public void validateInState(S state, R request, ValidationContext ctx) {
        for (InStateValidationEntry<S, R> entry : inStateValidators) {
            if (entry.getTargetState().equals(state)) {
                entry.getValidator().validate(state, request, ctx);
            }
        }
    }

    /** Validate the field value during a state transition. */
    public void validateStateTransition(S fromState, S toState, R request, ValidationContext ctx) {
        for (StateTransitionValidationEntry<S, R> entry : stateTransitionValidators) {
            if (entry.getFromState().equals(fromState) && entry.getToState().equals(toState)) {
                entry.getValidator().validate(fromState, toState, request, ctx);
            }
        }
    }
}
