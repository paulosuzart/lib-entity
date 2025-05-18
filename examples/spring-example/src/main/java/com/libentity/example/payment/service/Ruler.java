package com.libentity.example.payment.service;

import static com.libentity.decision.Rule.any;
import static com.libentity.decision.Rule.gt;
import static com.libentity.decision.Rule.in;
import static com.libentity.decision.Rule.is;
import static com.libentity.decision.Rule.isSet;
import static com.libentity.decision.Rule.lt;

import com.libentity.decision.DecisionResult;
import com.libentity.decision.DecisionTable;
import com.libentity.decision.MatchingRule;
import com.libentity.example.invoice.model.Invoice;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Ruler {
    enum Action {
        ALLOW_APPROVAL,
        DENY_APPROVAL
    }

    static void sExpression() {
        var vatExempt = UUID.randomUUID();
        var inputProvider = new InvoiceInputRuleProvider();
        var approver = UUID.randomUUID();
        var rules = List.of(
                new MatchingRule<>(
                        new InvoiceInput(
                                // any() means it will basically evaluate to true
                                in(Set.of(vatExempt, UUID.randomUUID())),
                                // it is true if the attribute is present
                                isSet(),
                                // custom arbitrary tests
                                lt(BigDecimal.valueOf(200.0)),
                                isSet()),
                        Action.DENY_APPROVAL,
                        inputProvider),
                new MatchingRule<>(
                        new InvoiceInput(
                                // If the org fully matches
                                in(Set.of(vatExempt)),
                                // catch all
                                any(),
                                // grater than 100
                                gt(BigDecimal.valueOf(100.0)),
                                is(approver)),
                        Action.ALLOW_APPROVAL,
                        inputProvider));

        var result = new DecisionTable<>("Invoice Can Export", rules, inputProvider)
                .evaluateFirst(
                        new InvoiceInputValue(vatExempt, LocalDate.now(), BigDecimal.TEN, approver),
                        DecisionTable.EvaluationPolicy.Unique);

        if (Objects.requireNonNull(result) instanceof DecisionResult.First<Action, InvoiceInputValue> out) {
            System.out.println(out.diagnose());
            System.out.println("The output is " + out.getOutput().get());

        } else if (Objects.requireNonNull(result) instanceof DecisionResult.Unique<Action, InvoiceInputValue> out) {
            System.out.println(out.diagnose());
            System.out.println(out.getOutput());
        } else {
            throw new IllegalStateException("Unexpected value: " + result);
        }

        //        var x = new DecisionTable<>("Invoice Can Export", rules, inputProvider)
        //                .collect(new InvoiceInputValue(vatExempt, LocalDate.now(), BigDecimal.TEN, approver));
        //
        //        System.out.println("Collect");
        //        System.out.println(x.diagnose());
    }

    public static void main(String[] args) {

        Invoice invoice = new Invoice();
        invoice.setAmount(BigDecimal.TEN);
        invoice.setDueDate(LocalDate.now());
        sExpression();
    }
}
