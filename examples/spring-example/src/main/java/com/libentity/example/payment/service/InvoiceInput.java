package com.libentity.example.payment.service;

import com.libentity.decision.DecisionInput;
import com.libentity.decision.Rule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@DecisionInput
public class InvoiceInput {
    Rule<UUID> isVatExempt;
    Rule<LocalDate> isDateSet;
    Rule<BigDecimal> amount;
    Rule<UUID> isApproved;
}
