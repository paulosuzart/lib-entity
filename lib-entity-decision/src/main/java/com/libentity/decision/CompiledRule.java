package com.libentity.decision;

import java.util.function.Function;

public record CompiledRule<V, K>(
        String name, RuleEval<K> ruleEval, Function<K, Boolean> evalFunction, Function<V, K> extractionFunction) {}
