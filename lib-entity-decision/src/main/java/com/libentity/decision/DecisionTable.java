package com.libentity.decision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class DecisionTable<I, O, V> {
    public enum EvaluationPolicy {
        First {
            @Override
            <O, V> DecisionResult<O, V> crateResult(
                    V inputValue,
                    Map<String, Object> variables,
                    List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules) {
                return new DecisionResult.First<>(inputValue, variables, evaluatedRules);
            }
        },
        Collect {
            @Override
            <O, V> DecisionResult<O, V> crateResult(
                    V inputValue,
                    Map<String, Object> variables,
                    List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules) {
                return new DecisionResult.Collect<>(inputValue, variables, evaluatedRules);
            }
        },
        Unique {
            @Override
            <O, V> DecisionResult<O, V> crateResult(
                    V inputValue,
                    Map<String, Object> variables,
                    List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules) {
                return new DecisionResult.Unique<>(inputValue, variables, evaluatedRules);
            }
        };

        abstract <O, V> DecisionResult<O, V> crateResult(
                V inputValue,
                Map<String, Object> variables,
                List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules);
    }

    private final String name;
    private final List<MatchingRule<I, O, V>> matchingRules;
    private final InputProvider<I, V> inputProvider;

    public DecisionTable(String name, List<MatchingRule<I, O, V>> matchingRules, InputProvider<I, V> inputProvider) {
        this.name = name;
        this.matchingRules = matchingRules;
        this.inputProvider = inputProvider;
    }

    public DecisionResult<O, V> evaluateFirst(V value, EvaluationPolicy evaluationPolicy) {
        if (value == null) {
            throw new RuntimeException("Value cannot be null");
        }

        if (matchingRules == null || matchingRules.isEmpty()) {
            throw new RuntimeException("No matching rules found");
        }

        Map<String, Object> inputVariables = inputProvider
                .getCompileRules(matchingRules.getFirst().getInput())
                .stream()
                .collect(Collectors.toMap(
                        CompiledRule::name, e -> e.extractionFunction().apply(value)));

        List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules = new ArrayList<>();
        for (MatchingRule<I, O, V> matchingRule : matchingRules) {
            List<DecisionResult.EvaluatedCompiledRule<V>> evaluatedCompiledRules = new ArrayList<>();
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
                evaluatedCompiledRules.add(new DecisionResult.EvaluatedCompiledRule<>(rule, true, matches));
            }

            var truthy = evaluatedCompiledRules.stream().allMatch(DecisionResult.EvaluatedCompiledRule::truthy);
            evaluatedRules.add(new DecisionResult.EvaluatedMatchingRule<>(
                    truthy, evaluatedCompiledRules, matches ? matchingRule.getOutput() : null));
        }
        return evaluationPolicy.crateResult(value, inputVariables, evaluatedRules);
    }
}
