package com.libentity.decision;

import java.util.List;

/**
 * Auxiliary class to avoid reflection. Every input type <pre>I</pre> gets a class of type <pre>V</pre> generated so the
 * rules can be extracted from <pre>I</pre> into a {@code List<CompiledRule<V, ?>>}.
 * @param <I>
 * @param <V>
 */
public interface InputProvider<I, V> {

    /**
     * "compiles" each rule of an input type
     */
    List<CompiledRule<V, ?>> getCompileRules(I input);
}
