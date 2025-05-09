package com.libentity.example.commands;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
@JsonTypeName("create")
public class CreateInvoiceCommand implements InvoiceActionCommand {
    private String employeeId;
    private BigDecimal vat;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String submitterId;
    private String submitterDeviceId;

    @Override
    public String getActionName() {
        return "create";
    }
}
