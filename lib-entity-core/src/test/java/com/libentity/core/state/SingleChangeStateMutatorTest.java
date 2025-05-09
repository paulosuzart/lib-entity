package com.libentity.core.state;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SingleChangeStateMutatorTest {
    @Test
    void allowsSingleStateChange() {
        SingleChangeStateMutator<String> mutator = new SingleChangeStateMutator<>("A");
        mutator.setState("B");
        assertEquals("B", mutator.getState());
    }

    @Test
    void throwsOnMultipleStateChanges() {
        SingleChangeStateMutator<String> mutator = new SingleChangeStateMutator<>("A");
        mutator.setState("B");
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> mutator.setState("C"));
        assertEquals("State can only be changed once during action execution", ex.getMessage());
    }

    @Test
    void returnsInitialStateIfNotChanged() {
        SingleChangeStateMutator<String> mutator = new SingleChangeStateMutator<>("A");
        assertEquals("A", mutator.getState());
    }
}
