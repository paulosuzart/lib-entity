package com.example;

import static com.libentity.decision.Rule.gt;
import static com.libentity.decision.Rule.gte;
import static com.libentity.decision.Rule.is;
import static com.libentity.decision.Rule.lte;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import com.libentity.decision.DecisionResult;
import com.libentity.decision.DecisionResult.Unique.UniqueResult.UniqueOutput;
import com.libentity.decision.DecisionTable;
import com.libentity.decision.EvaluationPolicy;
import com.libentity.decision.MatchingRule;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSampleApprovePaymentInput {

    @Test
    void testProviderWorks() {
        var input = new ApprovePaymentInput(
                gte(BigDecimal.TEN), gte(Instant.now().minus(10, ChronoUnit.DAYS)), gte(2), is(true));
        assertEquals(
                4, new ApprovePaymentInputRuleProvider().getCompileRules(input).size());
    }

    @Test
    void testFirstPolicyWorks() {
        var inputValue =
                new ApprovePaymentInputValue(BigDecimal.TEN, Instant.now().minus(5, ChronoUnit.DAYS), 5, true);
        var inputProvider = new ApprovePaymentInputRuleProvider();
        // Using direct access to attribute intentionally for simplicity
        var rule1 = new ApprovePaymentInput(
                gte(BigDecimal.TEN), gte(Instant.now().minus(10, ChronoUnit.DAYS)), gte(2), is(true));

        // Using direct access to attribute intentionally for simplicity
        var rule2 = new ApprovePaymentInput(
                gt(BigDecimal.ZERO), gte(Instant.now().minus(12, ChronoUnit.DAYS)), gte(1), is(true));

        var rules = List.of(
                MatchingRule.of(rule1, "Allowed", inputProvider), MatchingRule.of(rule2, "Denied", inputProvider));

        var decision = new DecisionTable<>("Sample", rules, inputProvider);
        var outcome = decision.evaluate(inputValue, EvaluationPolicy.First);
        if (outcome instanceof DecisionResult.First<String, ApprovePaymentInputValue> out) {
            assertEquals(Optional.of("Allowed"), out.getOutput());
        } else {
            Assertions.fail("Unexpected decision outcome");
        }
    }

    @Test
    void testUniquePolicyWorks_NonUniqueResult() {
        var inputValue =
                new ApprovePaymentInputValue(BigDecimal.TEN, Instant.now().minus(5, ChronoUnit.DAYS), 5, true);
        var inputProvider = new ApprovePaymentInputRuleProvider();
        // Using direct access to attribute intentionally for simplicity
        var rule1 = new ApprovePaymentInput(
                gte(BigDecimal.TEN), gte(Instant.now().minus(10, ChronoUnit.DAYS)), gte(2), is(true));

        // Using direct access to attribute intentionally for simplicity
        var rule2 = new ApprovePaymentInput(
                gt(BigDecimal.ZERO), gte(Instant.now().minus(12, ChronoUnit.DAYS)), gte(1), is(true));

        var rules = List.of(
                MatchingRule.of(rule1, "Allowed", inputProvider), MatchingRule.of(rule2, "Denied", inputProvider));

        var decision = new DecisionTable<>("Sample", rules, inputProvider);
        var outcome = decision.evaluate(inputValue, EvaluationPolicy.Unique);
        if (outcome instanceof DecisionResult.Unique<String, ApprovePaymentInputValue> out) {
            assertEquals(new DecisionResult.Unique.UniqueResult.NonUniqueOutput(), out.getOutput());
        } else {
            Assertions.fail("Unexpected decision outcome");
        }
    }

    @Test
    void testUniquePolicyWorks_UniqueResult() {
        var inputValue =
                new ApprovePaymentInputValue(BigDecimal.TEN, Instant.now().minus(5, ChronoUnit.DAYS), 5, true);
        var inputProvider = new ApprovePaymentInputRuleProvider();
        // Using direct access to attribute intentionally for simplicity
        var rule1 = new ApprovePaymentInput(
                gte(BigDecimal.valueOf(300.0)), gte(Instant.now().minus(10, ChronoUnit.DAYS)), gte(2), is(true));

        // Using direct access to attribute intentionally for simplicity
        var rule2 = new ApprovePaymentInput(
                gt(BigDecimal.ZERO), gte(Instant.now().minus(12, ChronoUnit.DAYS)), lte(8), is(true));

        var rules = List.of(
                MatchingRule.of(rule1, "Allowed", inputProvider), MatchingRule.of(rule2, "Denied", inputProvider));

        var decision = new DecisionTable<>("Sample", rules, inputProvider);
        var outcome = decision.evaluate(inputValue, EvaluationPolicy.Unique);

        if (outcome instanceof DecisionResult.Unique<String, ApprovePaymentInputValue> out) {

            switch (out.getOutput()) {
                case UniqueOutput<?> u
                when u.output().isPresent() && u.output().get() instanceof String s -> assertEquals("Denied", s);
                default -> Assertions.fail("Unexpected Unique decision outcome");
            }
        } else {
            Assertions.fail("Unexpected decision outcome");
        }
    }

    @Test
    void testCollectPolicyWorks() {
        var inputValue =
                new ApprovePaymentInputValue(BigDecimal.TEN, Instant.now().minus(5, ChronoUnit.DAYS), 5, true);
        var inputProvider = new ApprovePaymentInputRuleProvider();
        // Using direct access to attribute intentionally for simplicity
        var rule1 = new ApprovePaymentInput(
                gte(BigDecimal.valueOf(7.0)), gte(Instant.now().minus(10, ChronoUnit.DAYS)), gte(2), is(true));

        // Using direct access to attribute intentionally for simplicity
        var rule2 = new ApprovePaymentInput(
                gt(BigDecimal.ZERO), gte(Instant.now().minus(12, ChronoUnit.DAYS)), lte(8), is(true));

        var rules = List.of(
                MatchingRule.of(rule1, "Allowed", inputProvider), MatchingRule.of(rule2, "Denied", inputProvider));

        var decision = new DecisionTable<>("Sample", rules, inputProvider);
        var outcome = decision.evaluate(inputValue, EvaluationPolicy.Collect);

        if (outcome instanceof DecisionResult.Collect<String, ApprovePaymentInputValue> out) {

            assertIterableEquals(List.of("Allowed", "Denied"), out.getOutput());
        } else {
            Assertions.fail("Unexpected decision outcome");
        }
    }
}
