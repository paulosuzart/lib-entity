package com.libentity.example.commands;

import lombok.Data;

@Data
public class MarkAsPaidCommand implements InvoiceActionCommand {
    private String receiptNumber;

    @Override
    public String getActionName() {
        return "markAsPaid";
    }
}
