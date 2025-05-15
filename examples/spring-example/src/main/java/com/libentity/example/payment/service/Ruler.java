package com.libentity.example.payment.service;

import static com.libentity.decision.Rule.any;
import static com.libentity.decision.Rule.is;
import static com.libentity.decision.Rule.isSet;
import static com.libentity.decision.Rule.test;

import com.libentity.decision.DecisionTable;
import com.libentity.decision.MatchingRule;
import com.libentity.example.invoice.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Ruler {
    enum Action {
        ALLOW_APPROVAL,
        DENY_APPROVAL
    }

    static void sExpression() {
        var vatExempt = UUID.randomUUID();
        var inputProvider = new InvoiceInputRuleProvider();
        var rules = List.of(
                new MatchingRule<>(
                        new InvoiceInput(
                                // any() means it will basically evaluate to true
                                any(),
                                // it is true if the attribute is present
                                isSet(),
                                // custom arbitrary tests
                                test(v -> v.doubleValue() > 0.0)),
                        Action.DENY_APPROVAL,
                        inputProvider),
                new MatchingRule<>(
                        new InvoiceInput(is(vatExempt), any(), test(v -> v.doubleValue() > 100.0)),
                        Action.ALLOW_APPROVAL,
                        inputProvider));

        var out = new DecisionTable<>(rules)
                .evaluateFirst(new InvoiceInputValue(UUID.randomUUID(), LocalDate.now(), BigDecimal.TEN));

        System.out.println("Output was" + out);
    }

    public static void main(String[] args) {

        Invoice invoice = new Invoice();
        invoice.setAmount(BigDecimal.TEN);
        invoice.setDueDate(LocalDate.now());
        sExpression();
        //        var ammMiss = new Expression.Fact("invoice amount missing", () -> invoice.getAmount() == null);
        //        var hasAmount = ammMiss.not();
        //
        //        var hasDate = new Expression.Or(List.of(
        //                new Expression.Fact("invoice has date", () -> invoice.getDueDate() != null),
        //                new Expression.Fact("invoice has x", () -> invoice.getApprovalDate() != null)));
        //
        //        var hasData = new Expression.And(List.of(
        //                hasAmount, hasDate, new Expression.Fact("approver is set", () -> invoice.getApproverId() !=
        // null)));
        //
        //        hasData.eval();
        //
        //        System.out.println(new MermaidVisitor().print(hasData));
    }
}
