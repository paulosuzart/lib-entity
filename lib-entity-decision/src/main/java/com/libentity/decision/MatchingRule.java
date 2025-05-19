package com.libentity.decision;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a matching rule that matches a specific input to a corresponding output based on compiled rules.
 *
 * @param <I> The type of the input
 * @param <O> The type of the output
 * @param <V> The type of the intermediate value
 */
@Getter
@EqualsAndHashCode
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
