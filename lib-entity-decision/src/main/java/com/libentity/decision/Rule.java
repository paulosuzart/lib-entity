package com.libentity.decision;

import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;

@Getter
public class Rule<T> {

    private final RuleEval<T> ruleEval;

    public Rule(RuleEval<T> ruleEval) {
        this.ruleEval = ruleEval;
    }

    public boolean eval(T value) {
        return ruleEval.eval(value);
    }

    public Rule<T> not() {
        return new Rule<>(ruleEval.not());
    }

    public static <T> Rule<T> any() {
        return new Rule<>(new RuleEval.CatchAll<>());
    }

    public static <T> Rule<T> is(T arg) {
        return new Rule<>(new RuleEval.Is<>(arg));
    }

    public static <T extends Comparable<T>> Rule<T> gt(T arg) {
        return new Rule<>(new RuleEval.Gt<>(arg));
    }

    public static <T extends Number> Rule<T> gte(T arg) {
        return new Rule<>(new RuleEval.Gte<>(arg));
    }

    public static <T extends Number> Rule<T> lt(T arg) {
        return new Rule<>(new RuleEval.Lt<>(arg));
    }

    public static <T> Rule<T> in(Set<T> arg) {
        return new Rule<>(new RuleEval.In<>(arg));
    }

    public static <T> Rule<T> isSet() {
        return new Rule<>(new RuleEval.IsSet<>());
    }

    public static <T> Rule<T> test(Predicate<T> predicate) {
        return new Rule<>(new RuleEval.Test<>(predicate));
    }
}
