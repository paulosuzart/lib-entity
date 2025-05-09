package com.libentity.example.model;

import com.libentity.annotation.Handle;
import com.libentity.core.state.StateMutator;
import com.libentity.example.commands.ApprovePaymentCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApprovePaymentAction {
    @Handle
    public void handle(
            PaymentState state,
            PaymentRequestContext req,
            ApprovePaymentCommand cmd,
            StateMutator<PaymentState> mutator) {
        log.info("Approving payment. Don't do this at home.");
        req.newPayment().setApprovalComment(cmd.comment());
        mutator.setState(PaymentState.APPROVED);
    }
}
