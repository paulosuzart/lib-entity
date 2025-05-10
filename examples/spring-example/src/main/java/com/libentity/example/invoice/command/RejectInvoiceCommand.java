package com.libentity.example.invoice.command;

import lombok.Data;

@Data
public class RejectInvoiceCommand implements InvoiceActionCommand {
    private String rejectorId;
    private String rejectionReason;

    @Override
    public String getActionName() {
        return "reject";
    }
}
