package com.libentity.core.state;

/**
 * A StateMutator that only allows the state to be changed once. Subsequent attempts to change the
 * state will throw an IllegalStateException.
 */
public class SingleChangeStateMutator<S> implements StateMutator<S> {
    private S targetState;
    private boolean changed = false;

    public SingleChangeStateMutator(S initialState) {
        this.targetState = initialState;
    }

    @Override
    public void setState(S state) {
        if (changed) {
            throw new IllegalStateException("State can only be changed once during action execution");
        }
        this.targetState = state;
        this.changed = true;
    }

    @Override
    public boolean isUpdated() {
        return changed;
    }

    @Override
    public S getState() {
        return targetState;
    }
}
