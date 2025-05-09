package com.libentity.core.state;

/** Allows mutation of an entity's state. */
public interface StateMutator<S> {
    /** Set the entity's state. */
    void setState(S state);

    /** Get the entity's current state. */
    S getState();

    boolean isUpdated();
}
