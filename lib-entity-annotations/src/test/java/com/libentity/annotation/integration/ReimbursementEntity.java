package com.libentity.annotation.integration;

import com.libentity.annotation.*;
import java.math.BigDecimal;
import lombok.Data;

@EntityDefinition(
        name = "Reimbursement",
        stateEnum = ReimbursementState.class,
        fields = {
            @Field(name = "amount", type = BigDecimal.class, required = true),
            @Field(name = "description", type = String.class)
        },
        actions = {
            @Action(
                    name = "submitReimbursement",
                    description = "Submit a reimbursement",
                    handler = SubmitReimbursementHandler.class,
                    command = SubmitReimbursementCommand.class),
            @Action(
                    name = "approveReimbursement",
                    description = "Approve a reimbursement",
                    handler = ApproveReimbursementHandler.class,
                    command = ApproveReimbursementCommand.class)
        })
@Data
public class ReimbursementEntity {
    private BigDecimal amount;
    private String description;
    private ReimbursementState status;
}
