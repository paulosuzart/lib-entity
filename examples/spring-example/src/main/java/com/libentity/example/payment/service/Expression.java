package com.libentity.example.payment.service;

import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract sealed class Expression implements Rulez permits Expression.And, Expression.Fact, Expression.Or {

    String name;

    abstract Expression not();

    protected boolean evaluated;
    protected boolean truthy;

    public boolean eval() {
        return false;
    }

    @RequiredArgsConstructor
    public static final class And extends Expression {
        public final List<Expression> rules;

        @Override
        Expression not() {
            return new Expression.Fact("Not", () -> !eval());
        }

        @Override
        public boolean eval() {
            evaluated = true;
            if (rules.isEmpty()) return false;

            boolean result = true;
            for (var rule : rules) {
                result = result && rule.eval();
            }
            truthy = result;
            return result;
        }
    }

    @RequiredArgsConstructor
    public static final class Or extends Expression {
        public final List<Expression> rules;

        @Override
        Expression not() {
            return new Expression.Fact("Not", () -> !eval());
        }

        @Override
        public boolean eval() {
            evaluated = true;
            if (rules.isEmpty()) return false;

            boolean result = false;
            for (var rule : rules) {
                result = result || rule.eval();
            }
            truthy = result;
            return result;
        }
    }

    @RequiredArgsConstructor
    public static final class Fact extends Expression {
        public final String name;
        private final Supplier<Boolean> supplier;

        @Override
        Expression not() {
            return new Expression.Fact("Not " + name, () -> !eval());
        }

        @Override
        public boolean eval() {
            evaluated = true;
            var result = supplier.get();
            truthy = result;
            return result;
        }
    }
}
