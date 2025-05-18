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

@Getter
@RequiredArgsConstructor
public abstract sealed class HitPolicy<O, V>
        permits HitPolicy.Collect, HitPolicy.First, HitPolicy.Sum, HitPolicy.Unique {
    private final V inputValue;
    private final Map<String, Object> variables;
    private final List<EvaluatedMatchingRule<V, O>> evaluatedRules;

    public String diagnose() {
        var x = new DiagnosticTextResultVisitor<O, V>();
        x.visitResult(this);
        return x.getResult();
    }

    public record EvaluatedCompiledRule<V>(CompiledRule<V, ?> rule, boolean evaluated, boolean truthy) {}

    public record EvaluatedMatchingRule<V, O>(
            boolean truthy, List<EvaluatedCompiledRule<V>> evaluatedRuleList, O output) {}

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
                    .distinct()
                    .map(EvaluatedMatchingRule::output)
                    .reduce(op);
        }
    }

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
