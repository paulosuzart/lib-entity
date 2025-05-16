package com.libentity.decision;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class RuleEval<T> {

    protected Predicate<T> predicate;

    RuleEval(Predicate<T> evalFn) {
        this.predicate = evalFn;
    }

    public boolean eval(T value) {
        return predicate.test(value);
    }

    public RuleEval<T> not() {
        return new RuleEval<>(t -> !eval(t));
    }

    public static class CatchAll<T> extends RuleEval<T> {
        CatchAll() {
            super(t -> true);
        }
    }

    public static class Is<T> extends RuleEval<T> {
        Is(T target) {
            super(t -> Objects.equals(target, t));
        }
    }

    static class IsSet<T> extends RuleEval<T> {
        IsSet() {
            super(Objects::nonNull);
        }
    }

    static final class Test<T> extends RuleEval<T> {

        Test(Predicate<T> predicate) {
            super(predicate);
        }
    }

    static class Lt<T extends Number> extends RuleEval<T> {
        private T target;

        public Lt(T target) {
            super(value -> {
                if (value == null || target == null) {
                    return false;
                }
                BigDecimal valueBD = new BigDecimal(value.toString());
                BigDecimal targetBD = new BigDecimal(target.toString());
                return valueBD.compareTo(targetBD) < 0;
            });
            this.target = target;
        }
    }

    static class Lte<T extends Number> extends RuleEval<T> {
        private T target;

        public Lte(T target) {
            // keeping full code duplication for now
            super(value -> {
                if (value == null || target == null) {
                    return false;
                }
                BigDecimal valueBD = new BigDecimal(value.toString());
                BigDecimal targetBD = new BigDecimal(target.toString());
                return valueBD.compareTo(targetBD) <= 0;
            });
            this.target = target;
        }
    }

    public static class Gt<T extends Number> extends RuleEval<T> {
        private T target;

        public Gt(T target) {
            super(value -> {
                if (value == null || target == null) {
                    return false;
                }
                BigDecimal valueBD = new BigDecimal(value.toString());
                BigDecimal targetBD = new BigDecimal(target.toString());
                return valueBD.compareTo(targetBD) > 0;
            });
            this.target = target;
        }
    }

    public static class Gte<T extends Number> extends RuleEval<T> {
        private T target;

        public Gte(T target) {
            super(value -> {
                if (value == null || target == null) {
                    return false;
                }
                BigDecimal valueBD = new BigDecimal(value.toString());
                BigDecimal targetBD = new BigDecimal(target.toString());
                return valueBD.compareTo(targetBD) >= 0;
            });
            this.target = target;
        }
    }

    public static class In<T> extends RuleEval<T> {
        Set<T> target;

        public In(Set<T> target) {
            super(target::contains);
            this.target = target;
        }
    }
}
