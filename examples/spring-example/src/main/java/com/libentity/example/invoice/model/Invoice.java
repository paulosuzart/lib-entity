package com.libentity.example.invoice.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class Invoice {
    private Long id;
    private String employeeId;
    private BigDecimal vat;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate submittedAt;
    private String submitterId;
    private String submitterDeviceId;
    private LocalDate approvalDate;
    private String approverId;
    private String rejectionReason;
    private String rejectedBy;
    private LocalDate rejectionDate;
    private String receiptNumber;
    private boolean readyForApproval;
    private String approvalComment;
    private InvoiceState state = InvoiceState.DRAFT; // Default state
}
