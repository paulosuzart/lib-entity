package com.libentity.decision;

import java.util.List;
import java.util.function.Function;

public class MatchingRule<I, O> {

    private I matching;
    private O output;
    private final List<Rule<?>> rules;

    public MatchingRule(I matching, O output, Function<I, List<Rule<?>>> inputProvider) {
        this.rules = inputProvider.apply(matching);
    }

    List<Rule<?>> getRules() {
        return rules;
    }
}
