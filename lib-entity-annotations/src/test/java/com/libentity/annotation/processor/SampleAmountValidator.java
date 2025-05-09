package com.libentity.annotation.processor;

import com.libentity.annotation.InStateValidator;
import com.libentity.core.validation.ValidationContext;

@InStateValidator(entity = "Payment", state = "DRAFT")
public class SampleAmountValidator {
    public void validate(PaymentState state, PaymentRequest request, ValidationContext ctx) {
        if (request.getAmount() <= 0) {
            ctx.addError("AMOUNT_INVALID", "Amount must be greater than zero");
        }
    }
}
