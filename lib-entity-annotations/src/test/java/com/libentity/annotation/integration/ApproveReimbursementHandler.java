package com.libentity.annotation.integration;

import com.libentity.annotation.Handle;
import com.libentity.core.state.StateMutator;

public class ApproveReimbursementHandler {
    @Handle
    public void handle(
            ReimbursementState state,
            Object request,
            ApproveReimbursementCommand command,
            StateMutator<ReimbursementState> mutator) {
        mutator.setState(ReimbursementState.APPROVED);
    }
}
