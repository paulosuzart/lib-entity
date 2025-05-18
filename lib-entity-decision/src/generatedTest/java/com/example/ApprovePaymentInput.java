package com.example;

import com.libentity.decision.DecisionInput;
import com.libentity.decision.Rule;
import java.math.BigDecimal;
import java.time.Instant;

@DecisionInput
public class ApprovePaymentInput {
    Rule<BigDecimal> amount;
    Rule<Instant> approvedSince;
    Rule<Integer> numberOfApprovals;
    Rule<Boolean> currencyIsValid;

    public ApprovePaymentInput(
            Rule<BigDecimal> amount,
            Rule<Instant> approvedSince,
            Rule<Integer> numberOfApprovals,
            Rule<Boolean> currencyIsValid) {
        this.amount = amount;
        this.approvedSince = approvedSince;
        this.numberOfApprovals = numberOfApprovals;
        this.currencyIsValid = currencyIsValid;
    }
}
