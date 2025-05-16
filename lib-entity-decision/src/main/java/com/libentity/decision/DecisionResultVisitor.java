package com.libentity.decision;

import java.util.Map;

public interface DecisionResultVisitor<O, V> {

    void visitResult(DecisionResult<O, V> decisionResult);

    void visitInputVariables(Map<String, Object> inputVariables);

    void visitOutput(O outputValue);
}
