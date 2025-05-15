package com.libentity.decision;

import java.util.function.Predicate;

public class Rule<T> {

    public RuleEval<T> ruleEvaly;

    public Rule(RuleEval<T> ruleEval) {
        this.ruleEvaly = ruleEval;
    }

    public static <T> Rule<T> any() {
        return new Rule<>(new RuleEval.CatchAll<>());
    }

    public static <T> Rule<T> is(T arg) {
        return new Rule<>(new RuleEval.Is<>(arg));
    }

    public static <T> Rule<T> isSet() {
        return new Rule<>(new RuleEval.IsSet<>());
    }

    public static <T> Rule<T> test(Predicate<T> predicate) {
        return new Rule<>(new RuleEval.Test<>(predicate));
    }
}
