package com.libentity.decision;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

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

    public DecisionResult<O, V> evaluate(V value, EvaluationPolicy evaluationPolicy) {
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
