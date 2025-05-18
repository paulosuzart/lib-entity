package com.libentity.decision;

import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;

@Getter
public class Rule<T> {

    private final Matcher<T> matcher;

    public Rule(Matcher<T> matcher) {
        this.matcher = matcher;
    }

    public boolean eval(T value) {
        return matcher.eval(value);
    }

    public Rule<T> not() {
        return new Rule<>(matcher.not());
    }

    public static <T> Rule<T> any() {
        return new Rule<>(new Matcher.CatchAll<>());
    }

    public static <T> Rule<T> is(T arg) {
        return new Rule<>(new Matcher.Is<>(arg));
    }

    public static <T extends Comparable<T>> Rule<T> gt(T arg) {
        return new Rule<>(new Matcher.Gt<>(arg));
    }

    public static <T extends Comparable<T>> Rule<T> gte(T arg) {
        return new Rule<>(new Matcher.Gte<>(arg));
    }

    public static <T extends Comparable<T>> Rule<T> lt(T arg) {
        return new Rule<>(new Matcher.Lt<>(arg));
    }

    public static <T extends Comparable<T>> Rule<T> lte(T arg) {
        return new Rule<>(new Matcher.Lte<>(arg));
    }

    public static <T> Rule<T> in(Set<T> arg) {
        return new Rule<>(new Matcher.In<>(arg));
    }

    public static <T> Rule<T> isSet() {
        return new Rule<>(new Matcher.IsSet<>());
    }

    public static <T> Rule<T> test(Predicate<T> predicate) {
        return new Rule<>(new Matcher.Test<>(predicate));
    }
}
