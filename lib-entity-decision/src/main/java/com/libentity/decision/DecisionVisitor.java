package com.libentity.decision;

import java.util.Map;

public interface DecisionVisitor<O, V> {

    void visitResult(DecisionResult<O, V> decisionResult);

    void visitInputVariables(Map<String, Object> inputVariables);

    void visitDecisionTable(DecisionTable decisionTable);

    void visitMatchingRule(MatchingRule matchingRule);

    void visitInput(V inputValue);

    void visitRule(Rule rule);

    void visitOutput(O outputValue);
}
