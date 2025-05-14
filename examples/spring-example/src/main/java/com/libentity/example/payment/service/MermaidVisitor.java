package com.libentity.example.payment.service;

import java.util.IdentityHashMap;
import java.util.Map;

public class MermaidVisitor implements ExpressionVisitor {

    private final StringBuilder sb = new StringBuilder("graph TD\n");
    private int idCounter = 0;
    private final Map<Expression, String> ids = new IdentityHashMap<>();

    public String print(Expression root) {
        visitExpression(root);
        return sb.toString();
    }

    @Override
    public void visitExpression(Expression expression) {
        if (expression instanceof Expression.Fact fact) {
            visitFact(fact);
        } else if (expression instanceof Expression.And and) {
            visitAnd(and);
        } else if (expression instanceof Expression.Or or) {
            visitOr(or);
        }
    }

    private String getId(Expression expr) {
        return ids.computeIfAbsent(expr, e -> "N" + (++idCounter));
    }

    @Override
    public void visitFact(Expression.Fact fact) {
        String id = getId(fact);
        sb.append(id).append("[\"").append(fact.name).append("\"]\n");
    }

    @Override
    public void visitAnd(Expression.And and) {
        String id = getId(and);
        sb.append(id).append("[\"AND\"]\n");
        for (Expression child : and.rules) {
            visitExpression(child);
            String childId = getId(child);

            sb.append(id)
                    .append(child.truthy ? " ==> " : " -.-> ")
                    .append(childId)
                    .append("\n");
        }
    }

    @Override
    public void visitOr(Expression.Or or) {
        String id = getId(or);
        sb.append(id).append("[\"OR\"]\n");
        for (Expression child : or.rules) {
            visitExpression(child);
            String childId = getId(child);
            sb.append(id)
                    .append(child.truthy ? " ==> " : " -.-> ")
                    .append(childId)
                    .append("\n");
        }
    }
}
