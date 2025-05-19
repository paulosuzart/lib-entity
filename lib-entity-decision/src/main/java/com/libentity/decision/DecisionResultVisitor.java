package com.libentity.decision;

import java.util.List;
import java.util.Map;

public interface DecisionResultVisitor<O, V> {

    void visitResult(HitPolicy<O, V> hitPolicy);

    void visitEvaluatedRules(List<HitPolicy.EvaluatedMatchingRule<V, O>> evaluatedRules);

    void visitEvaluatedRule(HitPolicy.EvaluatedMatchingRule<V, O> evaluatedRule);

    void visitEvaluatedCompiledRule(HitPolicy.EvaluatedCompiledRule<V> vEvaluatedCompiledRule);

    void visitInputVariables(Map<String, Object> inputVariables);

    void visitOutput(O outputValue);
}
