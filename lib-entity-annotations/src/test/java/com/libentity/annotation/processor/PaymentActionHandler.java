package com.libentity.annotation.processor;

import com.libentity.annotation.Handle;
import com.libentity.annotation.OnlyIf;
import com.libentity.core.state.StateMutator;

public class PaymentActionHandler {
    @Handle
    public void handle(
            PaymentState state,
            PaymentRequest request,
            SubmitPaymentCommand command,
            StateMutator<PaymentState> mutator) {
        // Example state mutation logic
        if (request.getAmount() > 100) {
            mutator.setState(PaymentState.PENDING_APPROVAL);
        }
    }

    @OnlyIf
    public boolean canSubmit(PaymentState state, PaymentRequest request, SubmitPaymentCommand command) {
        return request.getAmount() > 0;
    }
}
