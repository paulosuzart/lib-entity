package com.libentity.core.action;

@FunctionalInterface
public interface ActionAvailabilityPredicate<S, R, C> {
    boolean test(S state, R request, C command);
}
