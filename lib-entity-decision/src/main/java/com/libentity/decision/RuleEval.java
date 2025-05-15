package com.libentity.decision;

import java.util.Objects;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;

public sealed interface RuleEval<T> permits RuleEval.CatchAll, RuleEval.Is, RuleEval.IsSet, RuleEval.Test {
    boolean eval(T value);

    final class CatchAll<T> implements RuleEval<T> {
        @Override
        public boolean eval(Object ignored) {
            return true;
        }
    }

    @AllArgsConstructor
    final class Is<T> implements RuleEval<T> {
        T target;

        @Override
        public boolean eval(T value) {
            return Objects.equals(target, value);
        }
    }

    @AllArgsConstructor
    final class IsSet<T> implements RuleEval<T> {

        @Override
        public boolean eval(T value) {
            return value != null;
        }
    }

    @AllArgsConstructor
    final class Test<T> implements RuleEval<T> {
        Predicate<T> predicate;

        @Override
        public boolean eval(T value) {
            return predicate.test(value);
        }
    }
}
