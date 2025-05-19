package com.libentity.decision;

import com.libentity.decision.internal.DiagnosticTextResultVisitor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A Hit policy knows how to extract the result of a bunch of evaluated rules into the expected
 * output concerning the intention of the Hit policy.
 *
 * @param <O>
 * @param <V>
 */
@Getter
@RequiredArgsConstructor
public abstract sealed class HitPolicy<O, V>
        permits HitPolicy.Collect, HitPolicy.First, HitPolicy.Sum, HitPolicy.Unique {
    private final V inputValue;
    private final Map<String, Object> variables;
    private final List<EvaluatedMatchingRule<V, O>> evaluatedRules;

    /**
     * Generates a textual representation of the evaluation and Hit Policy application to the result.
     */
    public String diagnose() {
        var x = new DiagnosticTextResultVisitor<O, V>();
        x.visitResult(this);
        return x.getResult();
    }

    /**
     * After a {@code Rule<K>} is executed, a {@link EvaluatedCompiledRule} is generated.
     */
    public record EvaluatedCompiledRule<V>(CompiledRule<V, ?> rule, boolean evaluated, boolean truthy) {}

    /**
     * Stores the whole {@link MatchingRule} evaluation result.
     */
    public record EvaluatedMatchingRule<V, O>(
            boolean truthy, List<EvaluatedCompiledRule<V>> evaluatedRuleList, O output) {}

    /**
     * A Hit Policy that will extract the first output of a truthy evaluated {@link MatchingRule}.
     */
    @EqualsAndHashCode(callSuper = true)
    public static final class First<O, V> extends HitPolicy<O, V> {
        public First(V inputValue, Map<String, Object> variables, List<EvaluatedMatchingRule<V, O>> evaluatedRules) {
            super(inputValue, variables, evaluatedRules);
        }

        public Optional<O> getOutput() {
            return getEvaluatedRules().stream()
                    .filter(e -> e.truthy)
                    .map(EvaluatedMatchingRule::output)
                    .findFirst();
        }
    }

    /**
     * A Hit Policy that return all results of all rules evaluated.
     */
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static final class Collect<O, V> extends HitPolicy<O, V> {

        public Collect(V inputValue, Map<String, Object> variables, List<EvaluatedMatchingRule<V, O>> evaluatedRules) {
            super(inputValue, variables, evaluatedRules);
        }

        public List<O> getOutput() {
            return getEvaluatedRules().stream()
                    .map(EvaluatedMatchingRule::output)
                    .collect(Collectors.toList());
        }
    }

    /**
     * A Hit Policy that lets the additional computation <pre>merge</pre> of the distinct outputs.
     * The matched outputs are reduced by the provided <pre>BinaryOperator</pre>.
     */
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static final class Sum<O, V> extends HitPolicy<O, V> {

        private final BinaryOperator<O> op;

        public Sum(
                V inputValue,
                Map<String, Object> variables,
                List<EvaluatedMatchingRule<V, O>> evaluatedRules,
                BinaryOperator<O> op) {
            super(inputValue, variables, evaluatedRules);
            this.op = op;
        }

        public Optional<O> getOutput() {
            return getEvaluatedRules().stream()
                    .filter(EvaluatedMatchingRule::truthy)
                    .map(EvaluatedMatchingRule::output)
                    .distinct()
                    .reduce(op);
        }
    }

    /**
     * A Hit Policy that returns the output of a rule as long as there is no other rule matching. The Output is wrapped
     * in a {@link UniqueResult} to indicate either an acceptable unique outcome {@link UniqueResult.UniqueOutput} or
     * the impossibility to pick up just one matched rule output represented by {@link UniqueResult.NonUniqueOutput}.
     */
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static final class Unique<O, V> extends HitPolicy<O, V> {
        public Unique(V inputValue, Map<String, Object> variables, List<EvaluatedMatchingRule<V, O>> evaluatedRules) {
            super(inputValue, variables, evaluatedRules);
        }

        public UniqueResult getOutput() {
            var allResults = getEvaluatedRules().stream()
                    .filter(res -> res.truthy)
                    .map(EvaluatedMatchingRule::output)
                    .toList();
            if (allResults.size() == 1) {
                return new UniqueResult.UniqueOutput<>(Optional.of(allResults.get(0)));
            } else {
                return new UniqueResult.NonUniqueOutput();
            }
        }

        public sealed interface UniqueResult {
            record UniqueOutput<O>(Optional<O> output) implements UniqueResult {}

            record NonUniqueOutput() implements UniqueResult {}
        }
    }
}
