package com.libentity.example.payment.service;

public interface ExpressionVisitor {

    void visitExpression(Expression expression);

    void visitFact(Expression.Fact fact);

    void visitAnd(Expression.And and);

    void visitOr(Expression.Or or);
}
