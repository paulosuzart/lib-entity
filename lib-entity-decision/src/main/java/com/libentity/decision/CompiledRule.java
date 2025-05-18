package com.libentity.decision;

import java.util.function.Function;

public record CompiledRule<V, K>(
        String name, Matcher<K> matcher, Function<K, Boolean> evalFunction, Function<V, K> extractionFunction) {}
