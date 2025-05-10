package com.libentity.example.invoice.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.libentity.core.action.ActionCommand;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "actionName",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SubmitInvoiceCommand.class, name = "submit"),
    @JsonSubTypes.Type(value = ApproveInvoiceCommand.class, name = "approve"),
    @JsonSubTypes.Type(value = RejectInvoiceCommand.class, name = "reject"),
    @JsonSubTypes.Type(value = MarkAsPaidCommand.class, name = "markAsPaid"),
    @JsonSubTypes.Type(value = CreateInvoiceCommand.class, name = "create")
    // Add more as needed
})
public interface InvoiceActionCommand extends ActionCommand {}
