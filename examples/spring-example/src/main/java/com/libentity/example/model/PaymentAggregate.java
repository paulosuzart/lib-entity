package com.libentity.example.model;

import com.libentity.annotation.Action;
import com.libentity.annotation.EntityDefinition;
import com.libentity.annotation.Field;
import com.libentity.example.commands.ApprovePaymentCommand;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@EntityDefinition(
        name = "Payment",
        fields = {
            @Field(name = "id", type = UUID.class),
            @Field(name = "amount", type = BigDecimal.class),
            @Field(name = "state", type = PaymentState.class),
            @Field(name = "approvalComment", type = String.class)
        },
        stateEnum = PaymentState.class,
        actions = {
            @Action(
                    name = "submitPayment",
                    description = "Submit a payment",
                    handler = ApprovePaymentAction.class,
                    command = ApprovePaymentCommand.class)
        })
@Data
@NoArgsConstructor
public class PaymentAggregate {
    private UUID id;
    private BigDecimal amount;
    private PaymentState state;
    private String approvalComment;
}
