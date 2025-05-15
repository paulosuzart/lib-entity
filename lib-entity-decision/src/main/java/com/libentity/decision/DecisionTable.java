package com.libentity.decision;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class DecisionTable<I, O, V> {
    private final List<MatchingRule<I, O, V>> matchingRules;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionTable<?, ?, ?> that = (DecisionTable<?, ?, ?>) o;
        return Objects.equals(matchingRules, that.matchingRules);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(matchingRules);
    }

    public DecisionTable(List<MatchingRule<I, O, V>> matchingRules) {
        this.matchingRules = matchingRules;
    }

    public DecisionResult evaluateFirst(V value) {
        for (MatchingRule<I, O, V> matchingRule : matchingRules) {
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
                return new DecisionResult.FirstMatch<>(matchingRule.getOutput(), resultByAttribute);
            }

        }
        return new DecisionResult.None<>();
    }

    public sealed interface DecisionResult {
        record FirstMatch<O>(O output, Map<String, Boolean> resultByRuleName) implements DecisionResult {}

        record None<O>() implements DecisionResult {}
    }
}
