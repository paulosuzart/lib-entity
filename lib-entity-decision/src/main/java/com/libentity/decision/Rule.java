package com.libentity.decision;

import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;

public class Rule<T> {

    public Function<RuleInEval<T>, RuleEval> ruleEval;

    public record RuleInEval<T>(T value) {
        public RuleEval any() {
            return new CatchAll();
        }

        public RuleEval is(T arg) {
            return new Is<>(arg);
        }

        public RuleEval isSet() {
            return new IsSet<>(value);
        }

        public RuleEval test(Predicate<T> predicate) {
            return new Test<>(value, predicate);
        }
    }

    public Rule(Function<RuleInEval<T>, RuleEval> eval) {
        this.ruleEval = eval;
    }

    public RuleEval catchAll() {
        return new CatchAll();
    }

    public RuleEval is(T arg) {
        return new Is<>(arg);
    }

    public RuleEval isSet(T arg) {
        return new IsSet<>(arg);
    }

    //    public RuleEval test(Predicate<T> predicate) {
    //        return new Test<>(value, predicate);
    //    }

    public abstract static sealed class RuleEval permits CatchAll, Is, IsSet, Test {
        abstract boolean eval();
    }

    public static final class CatchAll extends RuleEval {

        @Override
        boolean eval() {
            return true;
        }
    }

    @AllArgsConstructor
    public static final class Is<T> extends RuleEval {
        T target;

        @Override
        boolean eval() {
            return false;
        }
    }

    @AllArgsConstructor
    public static final class IsSet<T> extends RuleEval {
        T target;

        @Override
        boolean eval() {
            return target != null;
        }
    }

    @AllArgsConstructor
    public static final class Test<T> extends RuleEval {
        T target;
        Predicate<T> predicate;

        @Override
        boolean eval() {
            return predicate.test(target);
        }
    }
}
