package com.libentity.example.payment.service;

import com.libentity.decision.DecisionTable;
import com.libentity.decision.MatchingRule;
import com.libentity.decision.Rule;
import com.libentity.example.invoice.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Ruler {

    void sExpression(Invoice inv) {
        var vatExempt = UUID.randomUUID();
        var rules = List.of(
                new MatchingRule<>(
                        new InvoiceInput(
                                new Rule<>(Rule.RuleInEval::any),
                                new Rule<>(Rule.RuleInEval::isSet),
                                new Rule<>(e -> e.test(v -> v.doubleValue() > 0.0))),
                        Boolean.TRUE,
                        InvoiceInputRuleProvider::getRules),
                new MatchingRule<>(
                        new InvoiceInput(
                                new Rule<>(e -> e.is(vatExempt)),
                                new Rule<>(Rule.RuleInEval::any),
                                new Rule<>(e -> e.test(v -> v.doubleValue() > 100.0))),
                        Boolean.TRUE,
                        InvoiceInputRuleProvider::getRules));

        var out = new DecisionTable<>(rules)
                .evaluate(rules, new InvoiceInputValue(UUID.randomUUID(), LocalDate.now(), BigDecimal.TEN));
    }

    public static void main(String[] args) {

        Invoice invoice = new Invoice();
        invoice.setAmount(BigDecimal.TEN);
        invoice.setDueDate(LocalDate.now());

        var ammMiss = new Expression.Fact("invoice amount missing", () -> invoice.getAmount() == null);
        var hasAmount = ammMiss.not();

        var hasDate = new Expression.Or(List.of(
                new Expression.Fact("invoice has date", () -> invoice.getDueDate() != null),
                new Expression.Fact("invoice has x", () -> invoice.getApprovalDate() != null)));

        var hasData = new Expression.And(List.of(
                hasAmount, hasDate, new Expression.Fact("approver is set", () -> invoice.getApproverId() != null)));

        hasData.eval();

        System.out.println(new MermaidVisitor().print(hasData));
    }
}
