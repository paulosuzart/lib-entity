package com.libentity.decision;

import java.util.List;
import java.util.Objects;

public class DecisionTable<I, O, V> {
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

    private List<MatchingRule<I, O>> matchingRules;

    public List<MatchingRule<I, O>> getMatchingRules() {
        return matchingRules;
    }

    public DecisionTable(List<MatchingRule<I, O>> matchingRules) {
        this.matchingRules = matchingRules;
    }

    public O evaluate(List<MatchingRule<I, O>> matchingRules, V value) {
        for (MatchingRule<I, O> matchingRule : matchingRules) {
            //            matchingRule.getRules().stream().anyMatch(p -> p.ruleEval.apply(value));
        }
        return null;
    }
}
