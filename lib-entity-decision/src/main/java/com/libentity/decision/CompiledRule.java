package com.libentity.decision;

import java.util.function.Function;

/**
 * A compiled rule is a attribute of an Input type (the one where attributes are of {@code Rule<K>} type).
 * <br/>
 *
 * @param name               The name of the attribute in the input type
 * @param matcher            The corresponding matcher {@link Matcher}
 * @param evalFunction  The function that takes the attribute underlying value and applies the matcher predicate to it.
 * @param extractionFunction The function that knows how to extract the attribute {@code K} out of a value {@code V}
 * @param <V> Is the value object generated from the Input Type. {@code V} is generated out of the InputType of a rule.
 * @param <K> Is the type of and individual attribute of the Input Value.
 */
public record CompiledRule<V, K>(
        String name, Matcher<K> matcher, Function<K, Boolean> evalFunction, Function<V, K> extractionFunction) {}
