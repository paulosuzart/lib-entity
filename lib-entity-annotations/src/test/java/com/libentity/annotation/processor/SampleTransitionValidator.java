package com.libentity.annotation.processor;

import com.libentity.annotation.TransitionValidator;
import com.libentity.core.validation.ValidationContext;

@TransitionValidator(entity = "Payment", from = "DRAFT", to = "APPROVED")
public class SampleTransitionValidator {
    public void validate(PaymentState from, PaymentState to, PaymentRequest request, ValidationContext ctx) {
        // For demonstration, require amount > 100 for approval
        if (request.getAmount() <= 100) {
            ctx.addError("AMOUNT_TOO_LOW", "Amount must be greater than 100 for approval");
        }
    }
}
