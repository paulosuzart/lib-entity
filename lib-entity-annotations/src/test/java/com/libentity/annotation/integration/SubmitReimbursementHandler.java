package com.libentity.annotation.integration;

import com.libentity.annotation.Handle;
import com.libentity.core.state.StateMutator;

public class SubmitReimbursementHandler {
    @Handle
    public void handle(
            ReimbursementState state,
            Object request,
            SubmitReimbursementCommand command,
            StateMutator<ReimbursementState> mutator) {
        mutator.setState(ReimbursementState.SUBMITTED);
    }
}
