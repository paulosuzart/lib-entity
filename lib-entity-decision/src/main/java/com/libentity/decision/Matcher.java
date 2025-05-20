package com.libentity.decision;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;

/**
 * A Matcher simply wraps a predicate and a label.
 * <p><b>Important:</b>At the moment the label is eagerly calculated.</p>
 */
@EqualsAndHashCode
public class Matcher<T> {

    protected Predicate<T> predicate;
    protected String label;

    @Override
    public String toString() {
        return this.label;
    }

    Matcher(Predicate<T> evalFn, String label) {
        this.predicate = evalFn;
        this.label = label;
    }

    public boolean eval(T value) {
        return predicate.test(value);
    }

    public Matcher<T> not() {
        return new Matcher<>(predicate.negate(), "!" + label);
    }

    public Matcher<T> and(Matcher<T> other) {
        return new Matcher<>(predicate.and(other.predicate), label + " and " + other.label);
    }

    public Matcher<T> or(Matcher<T> other) {
        return new Matcher<>(predicate.or(other.predicate), label + " or " + other.label);
    }

    public static class CatchAll<T> extends Matcher<T> {
        CatchAll() {
            super(t -> true, "-");
        }
    }

    public static class Is<T> extends Matcher<T> {
        Is(T target) {
            super(t -> Objects.equals(target, t), "=");
        }
    }

    public static class IsPresent<T> extends Matcher<T> {
        IsPresent() {
            super(
                    (T s) -> {
                        if (s == null) {
                            return false;
                        }
                        if (s instanceof Optional) {
                            return ((Optional<?>) s).isPresent();
                        }
                        return true;
                    },
                    "isPresent");
        }
    }

    public static final class Test<T> extends Matcher<T> {
        Test(Predicate<T> predicate) {
            super(predicate, "opaque predicate");
        }
    }

    public static class Lt<T extends Comparable<T>> extends Matcher<T> {
        Lt(T target) {
            super(
                    value -> {
                        if (value == null || target == null) {
                            return false;
                        }
                        return value.compareTo(target) < 0;
                    },
                    "< %s".formatted(target));
        }
    }

    public static class Lte<T extends Comparable<T>> extends Matcher<T> {
        Lte(T target) {
            // keeping full code duplication for now
            super(
                    value -> {
                        if (value == null || target == null) {
                            return false;
                        }
                        return value.compareTo(target) <= 0;
                    },
                    "<= %s".formatted(target));
        }
    }

    public static class Gt<T extends Comparable<T>> extends Matcher<T> {
        public Gt(T target) {
            super(
                    value -> {
                        if (value == null || target == null) {
                            return false;
                        }
                        return value.compareTo(target) > 0;
                    },
                    "> %s".formatted(target));
        }
    }

    public static class Gte<T extends Comparable<T>> extends Matcher<T> {
        Gte(T target) {
            super(
                    value -> {
                        if (value == null || target == null) {
                            return false;
                        }
                        return value.compareTo(target) >= 0;
                    },
                    ">= %s".formatted(target));
        }
    }

    public static class In<T> extends Matcher<T> {
        private static final int MAX_IN_LABEL = 5;

        In(Set<T> target) {
            super(
                    target::contains,
                    "in( %s )%s"
                            .formatted(
                                    target.stream()
                                            .map(Object::toString)
                                            .limit(MAX_IN_LABEL)
                                            .collect(Collectors.joining(", ")),
                                    target.size() > MAX_IN_LABEL ? " ... " + target.size() + " elements " : ""));
        }
    }
}
