package com.libentity.core.action;

import java.util.Set;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Builder for action definitions with a fluent API.
 *
 * @param <S> The type of state
 * @param <R> The type of request
 * @param <C> The type of command
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionBuilder<S, R, C> {
    private final String name;
    private String description;
    private Set<S> allowedStates;
    private @Nullable ActionAvailabilityPredicate<S, R, C> onlyIf;
    private ActionHandler<S, R, C> handler;

    /** Set the description of the action. */
    public ActionBuilder<S, R, C> description(String description) {
        this.description = description;
        return this;
    }

    /** Set the states in which this action is allowed. */
    public ActionBuilder<S, R, C> allowedStates(Set<S> states) {
        this.allowedStates = states;
        return this;
    }

    /**
     * Set a predicate that must be true for this action to be allowed. The predicate is evaluated
     * before the action handler is executed.
     */
    public ActionBuilder<S, R, C> onlyIf(ActionAvailabilityPredicate<S, R, C> predicate) {
        this.onlyIf = predicate;
        return this;
    }

    /** Set the handler that executes this action. */
    public ActionBuilder<S, R, C> handler(ActionHandler<S, R, C> handler) {
        this.handler = handler;
        return this;
    }

    /** Build the action definition. */
    public ActionDefinition<S, R, C> build() {
        return ActionDefinition.<S, R, C>builder()
                .name(name)
                .description(description)
                .allowedStates(allowedStates)
                .onlyIf(onlyIf)
                .handler(handler)
                .build();
    }
}
