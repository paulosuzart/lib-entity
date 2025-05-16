package com.libentity.decision;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    public DecisionResult<O, V> evaluateFirst(V value) {
        if (value == null) {
            throw new RuntimeException("Value cannot be null");
        }

        if (matchingRules == null || matchingRules.isEmpty()) {
            throw new RuntimeException("No matching rules found");
        }

        Map<String, Object> inputVariables = inputProvider.getCompileRules(matchingRules.getFirst().getInput()).stream()
                .collect(Collectors.toMap(
                        CompiledRule::name, e -> e.extractionFunction().apply(value)));

        for (MatchingRule<I, O, V> matchingRule : matchingRules) {

            if (matchingRule.getRules() == null || matchingRule.getRules().isEmpty()) {
                throw new RuntimeException("No rules found for matching rule");
            }
            boolean matches = true;
            Map<String, Boolean> resultByAttribute = new LinkedHashMap<>();
            for (var rule : matchingRule.getRules()) {
                var attributeValue = rule.extractionFunction().apply(value);
                // we need to escape java generic dirty here
                var evalF = (Function<Object, Boolean>) rule.evalFunction();
                var result = evalF.apply(attributeValue);
                matches = matches && result;
                resultByAttribute.put(rule.name(), result);
            }
            if (matches) {
                // early termination
                return new DecisionResult.FirstMatch<>(value, matchingRule.getOutput(), inputVariables);
            }
        }
        return new DecisionResult.None<>(value, null, inputVariables);
    }
}
