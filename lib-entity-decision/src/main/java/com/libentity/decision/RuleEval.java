package com.libentity.decision;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RuleEval<T> {

    protected Predicate<T> predicate;
    protected String label;

    @Override
    public String toString() {
        return this.label;
    }

    RuleEval(Predicate<T> evalFn, String label) {
        this.predicate = evalFn;
        this.label = label;
    }

    public boolean eval(T value) {
        return predicate.test(value);
    }

    public RuleEval<T> not() {
        return new RuleEval<>(t -> !eval(t), "!" + label);
    }

    public static class CatchAll<T> extends RuleEval<T> {
        CatchAll() {
            super(t -> true, "-");
        }
    }

    public static class Is<T> extends RuleEval<T> {
        Is(T target) {
            super(t -> Objects.equals(target, t), "=");
        }
    }

    static class IsSet<T> extends RuleEval<T> {
        IsSet() {
            super(Objects::nonNull, "isSet");
        }
    }

    static final class Test<T> extends RuleEval<T> {

        Test(Predicate<T> predicate) {
            super(predicate, "opaque predicate");
        }
    }

    static class Lt<T extends Number> extends RuleEval<T> {
        private T target;

        public Lt(T target) {
            super(
                    value -> {
                        if (value == null || target == null) {
                            return false;
                        }
                        BigDecimal valueBD = new BigDecimal(value.toString());
                        BigDecimal targetBD = new BigDecimal(target.toString());
                        return valueBD.compareTo(targetBD) < 0;
                    },
                    "< %s".formatted(target));
            this.target = target;
        }
    }

    static class Lte<T extends Number> extends RuleEval<T> {
        private T target;

        public Lte(T target) {
            // keeping full code duplication for now
            super(
                    value -> {
                        if (value == null || target == null) {
                            return false;
                        }
                        BigDecimal valueBD = new BigDecimal(value.toString());
                        BigDecimal targetBD = new BigDecimal(target.toString());
                        return valueBD.compareTo(targetBD) <= 0;
                    },
                    "<= %s".formatted(target));
            this.target = target;
        }
    }

    public static class Gt<T extends Comparable<T>> extends RuleEval<T> {
        private T target;

        public Gt(T target) {
            super(
                    value -> {
                        if (value == null || target == null) {
                            return false;
                        }
                        return value.compareTo(target) > 0;
                    },
                    "> %s".formatted(target));
            this.target = target;
        }
    }

    public static class Gte<T extends Number> extends RuleEval<T> {
        private T target;

        public Gte(T target) {
            super(
                    value -> {
                        if (value == null || target == null) {
                            return false;
                        }
                        BigDecimal valueBD = new BigDecimal(value.toString());
                        BigDecimal targetBD = new BigDecimal(target.toString());
                        return valueBD.compareTo(targetBD) >= 0;
                    },
                    ">= %s".formatted(target));
            this.target = target;
        }
    }

    public static class In<T> extends RuleEval<T> {
        private static final int MAX_IN_LABEL = 5;
        private Set<T> target;

        public In(Set<T> target) {
            super(
                    target::contains,
                    "in( %s )%s"
                            .formatted(
                                    target.stream()
                                            .map(Object::toString)
                                            .limit(MAX_IN_LABEL)
                                            .collect(Collectors.joining(", ")),
                                    target.size() > MAX_IN_LABEL ? " ... " + target.size() + " elements " : ""));
            this.target = target;
        }
    }
}
