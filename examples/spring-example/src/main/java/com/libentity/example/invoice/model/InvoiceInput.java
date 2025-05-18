package com.libentity.example.invoice.model;

import com.libentity.decision.DecisionInput;
import com.libentity.decision.Rule;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@DecisionInput
public class InvoiceInput {
    Rule<String> requesterId;
    Rule<LocalDate> isDateSet;
    Rule<BigDecimal> amount;
    Rule<String> isApproved;
}
