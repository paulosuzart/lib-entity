package com.libentity.decision;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import lombok.Getter;

/**
 * Represents a decision table that can evaluate input values based on matching rules.
 *
 * @param <I> The type of the input to the matching rules.
 * @param <O> The type of the output value.
 * @param <V> The type of the input value to be evaluated.
 */
@Getter
public class DecisionTable<I, O, V> {

    private final String name;
    private final List<MatchingRule<I, O, V>> matchingRules;
    private final InputProvider<I, V> inputProvider;

    public DecisionTable(String name, List<MatchingRule<I, O, V>> matchingRules, InputProvider<I, V> inputProvider) {
        this.name = name;
        this.matchingRules = matchingRules;
        this.inputProvider = inputProvider;
    }

    public HitPolicy.Unique<O, V> evaluateUnique(V value) {
        var factory = new ResultFactory<O, V, HitPolicy.Unique<O, V>>() {
            @Override
            public HitPolicy.Unique<O, V> getResult(
                    V inputValue,
                    Map<String, Object> variables,
                    List<HitPolicy.EvaluatedMatchingRule<V, O>> evaluatedRules) {
                return new HitPolicy.Unique<>(value, variables, evaluatedRules);
            }
        };
        return doEvaluate(value, factory);
    }

    public HitPolicy.Sum<O, V> evaluateSum(V value, BinaryOperator<O> mergeFunction) {
        var factory = new ResultFactory<O, V, HitPolicy.Sum<O, V>>() {

            @Override
            public HitPolicy.Sum<O, V> getResult(
                    V inputValue,
                    Map<String, Object> variables,
                    List<HitPolicy.EvaluatedMatchingRule<V, O>> evaluatedRules) {
                return new HitPolicy.Sum<>(inputValue, variables, evaluatedRules, mergeFunction);
            }
        };
        return doEvaluate(value, factory);
    }

    public HitPolicy.Collect<O, V> evaluateCollect(V value) {
        var factory = new ResultFactory<O, V, HitPolicy.Collect<O, V>>() {

            @Override
            public HitPolicy.Collect<O, V> getResult(
                    V inputValue,
                    Map<String, Object> variables,
                    List<HitPolicy.EvaluatedMatchingRule<V, O>> evaluatedRules) {
                return new HitPolicy.Collect<>(inputValue, variables, evaluatedRules);
            }
        };
        return doEvaluate(value, factory);
    }

    public HitPolicy.First<O, V> evaluateFirst(V inputValue) {
        var factory = new ResultFactory<O, V, HitPolicy.First<O, V>>() {

            @Override
            public HitPolicy.First<O, V> getResult(
                    V inputValue,
                    Map<String, Object> variables,
                    List<HitPolicy.EvaluatedMatchingRule<V, O>> evaluatedRules) {
                return new HitPolicy.First<>(inputValue, variables, evaluatedRules);
            }
        };
        return doEvaluate(inputValue, factory);
    }

    private <Z extends HitPolicy<O, V>> Z doEvaluate(V value, ResultFactory<O, V, Z> resultFactory) {
        if (value == null) {
            throw new RuntimeException("Value cannot be null");
        }

        if (matchingRules == null || matchingRules.isEmpty()) {
            throw new RuntimeException("No matching rules found");
        }

        Map<String, Object> inputVariables = new LinkedHashMap<>();
        inputProvider
                .getCompileRules(matchingRules.getFirst().getInput())
                .forEach(
                        e -> inputVariables.put(e.name(), e.extractionFunction().apply(value)));

        List<HitPolicy.EvaluatedMatchingRule<V, O>> evaluatedRules = new ArrayList<>();
        for (MatchingRule<I, O, V> matchingRule : matchingRules) {
            List<HitPolicy.EvaluatedCompiledRule<V>> evaluatedCompiledRules = new ArrayList<>();
            if (matchingRule.getRules() == null || matchingRule.getRules().isEmpty()) {
                throw new RuntimeException("No rules found for matching rule");
            }
            boolean matches = true;
            for (var rule : matchingRule.getRules()) {
                var attributeValue = rule.extractionFunction().apply(value);
                // we need to escape java generic dirty here
                @SuppressWarnings("unchecked")
                var evalF = (Function<Object, Boolean>) rule.evalFunction();
                var result = evalF.apply(attributeValue);
                matches = matches && result;
                evaluatedCompiledRules.add(new HitPolicy.EvaluatedCompiledRule<>(rule, true, result));
            }

            var truthy = evaluatedCompiledRules.stream().allMatch(HitPolicy.EvaluatedCompiledRule::truthy);
            evaluatedRules.add(new HitPolicy.EvaluatedMatchingRule<>(
                    truthy, evaluatedCompiledRules, matches ? matchingRule.getOutput() : null));
        }
        return resultFactory.getResult(value, inputVariables, evaluatedRules);
    }

    interface ResultFactory<O, V, Z extends HitPolicy<O, V>> {
        Z getResult(
                V inputValue,
                Map<String, Object> variables,
                List<HitPolicy.EvaluatedMatchingRule<V, O>> evaluatedRules);
    }
}
