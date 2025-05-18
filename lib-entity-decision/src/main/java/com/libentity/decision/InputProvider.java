package com.libentity.decision;

import java.util.List;

public interface InputProvider<I, V> {

    /**
     * "compiles" each rule of an input type
     */
    List<CompiledRule<V, ?>> getCompileRules(I input);
}
