package com.libentity.example.invoice.command;

import lombok.Data;

@Data
public class ApproveInvoiceCommand implements InvoiceActionCommand {
    private String approverId;
    private String approvalComment;

    @Override
    public String getActionName() {
        return "approve";
    }
}
