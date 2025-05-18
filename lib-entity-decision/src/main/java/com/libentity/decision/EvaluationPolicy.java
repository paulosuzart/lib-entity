package com.libentity.decision;

import java.util.List;
import java.util.Map;

public enum EvaluationPolicy {
    First {
        @Override
        <O, V> DecisionResult<O, V> crateResult(
                V inputValue,
                Map<String, Object> variables,
                List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules) {
            return new DecisionResult.First<>(inputValue, variables, evaluatedRules);
        }
    },
    Collect {
        @Override
        <O, V> DecisionResult<O, V> crateResult(
                V inputValue,
                Map<String, Object> variables,
                List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules) {
            return new DecisionResult.Collect<>(inputValue, variables, evaluatedRules);
        }
    },
    Unique {
        @Override
        <O, V> DecisionResult<O, V> crateResult(
                V inputValue,
                Map<String, Object> variables,
                List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules) {
            return new DecisionResult.Unique<>(inputValue, variables, evaluatedRules);
        }
    };

    abstract <O, V> DecisionResult<O, V> crateResult(
            V inputValue,
            Map<String, Object> variables,
            List<DecisionResult.EvaluatedMatchingRule<V, O>> evaluatedRules);
}
