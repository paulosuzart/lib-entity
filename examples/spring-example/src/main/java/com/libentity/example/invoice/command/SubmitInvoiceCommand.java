package com.libentity.example.invoice.command;

import lombok.Data;

@Data
public class SubmitInvoiceCommand implements InvoiceActionCommand {
    private String submitterId;
    private String submitterDeviceId;

    @Override
    public String getActionName() {
        return "submit";
    }
}
