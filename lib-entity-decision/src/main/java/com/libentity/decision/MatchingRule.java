package com.libentity.decision;

import java.util.List;
import lombok.Getter;

@Getter
public class MatchingRule<I, O, V> {

    private final I matching;
    private final O output;
    private final List<CompiledRule<V, ?>> rules;

    public MatchingRule(I matching, O output, InputProvider<I, V> inputProvider) {
        this.rules = inputProvider.getCompileRules(matching);
        this.output = output;
        this.matching = matching;
    }
}
