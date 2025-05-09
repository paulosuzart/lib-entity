package com.libentity.annotation.processor;

import com.libentity.annotation.*;
import java.math.BigDecimal;

@EntityDefinition(
        name = "Payment",
        stateEnum = PaymentState.class,
        fields = {
            @Field(
                    name = "amount",
                    type = BigDecimal.class,
                    required = true,
                    inStateValidators = {SampleAmountValidator.class},
                    transitionValidators = {SampleTransitionValidator.class})
        },
        actions = {
            @Action(
                    name = "submitPayment",
                    description = "Submit a payment",
                    handler = PaymentActionHandler.class,
                    command = SubmitPaymentCommand.class)
        },
        inStateValidators = {SampleAmountValidator.class},
        transitionValidators = {SampleTransitionValidator.class})
public class PaymentEntity {}
