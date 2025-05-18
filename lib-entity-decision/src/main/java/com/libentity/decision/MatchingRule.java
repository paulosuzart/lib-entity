package com.libentity.decision;

import java.util.List;
import lombok.Getter;

@Getter
public class MatchingRule<I, O, V> {

    private final I input;
    private final O output;
    private final List<CompiledRule<V, ?>> rules;

    public MatchingRule(I input, O output, InputProvider<I, V> inputProvider) {
        this.rules = inputProvider.getCompileRules(input);
        this.output = output;
        this.input = input;
    }

    public static <I, O, V> MatchingRule<I, O, V> of(I input, O output, InputProvider<I, V> inputProvider) {
        return new MatchingRule<>(input, output, inputProvider);
    }
}
