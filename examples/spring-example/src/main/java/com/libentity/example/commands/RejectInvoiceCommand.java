package com.libentity.example.commands;

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
