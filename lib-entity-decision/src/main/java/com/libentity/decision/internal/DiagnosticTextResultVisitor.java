package com.libentity.decision.internal;

import static java.lang.Math.max;

import com.libentity.decision.DecisionResult;
import com.libentity.decision.DecisionResultVisitor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class DiagnosticTextResultVisitor<O, V> implements DecisionResultVisitor<O, V> {

    private StringBuilder sb = new StringBuilder();
    private int rule = 0;
    private int longestVariableSize = 3;

    public String getResult() {
        return sb.toString();
    }

    @Override
    public void visitResult(DecisionResult<O, V> decisionResult) {
        sb.append("Diagnostics: \n");
        if (decisionResult instanceof DecisionResult.None) {
            visitOutput(decisionResult.getOutput());
        } else if (decisionResult instanceof DecisionResult.FirstMatch<O, V> f) {
            visitOutput(decisionResult.getOutput());
            visitInputVariables(f.getVariables());
            visitEvaluatedRules(f.getEvaluatedRules());
            sb.append("\n");
        }
    }

    private void visitEvaluatedRules(List<DecisionResult.EvaluatedRule<V>> evaluatedRules) {
        for (DecisionResult.EvaluatedRule<V> evaluatedRule : evaluatedRules) {
            visitEvaluatedRule(evaluatedRule);
        }
    }

    private void visitEvaluatedRule(DecisionResult.EvaluatedRule<V> evaluatedRule) {
        sb.append("Rule ");
        sb.append(rule++);
        sb.append(" [").append(getTruthyMarker(evaluatedRule.truthy())).append("]:\n");
        for (DecisionResult.EvaluatedCompiledRule<V> vEvaluatedCompiledRule : evaluatedRule.evaluatedRuleList()) {
            visitEvaluatedCompiledRule(vEvaluatedCompiledRule);
        }
    }

    private String getTruthyMarker(boolean truthy) {
        return truthy ? "t" : "f";
    }

    private void visitEvaluatedCompiledRule(DecisionResult.EvaluatedCompiledRule<V> vEvaluatedCompiledRule) {
        sb.append("  ");
        sb.append(StringUtils.rightPad(vEvaluatedCompiledRule.rule().name(), longestVariableSize + 2))
                .append("[")
                .append(getTruthyMarker(vEvaluatedCompiledRule.truthy()))
                .append("]: ");
        sb.append(vEvaluatedCompiledRule.rule().ruleEval());
        sb.append("\n");
    }

    @Override
    public void visitInputVariables(Map<String, Object> inputVariables) {
        sb.append("Input: \n  ");
        var str = inputVariables.entrySet().stream()
                .peek(e -> longestVariableSize =
                        max(longestVariableSize, e.getKey().length()))
                .map(e -> "%s: %s".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n  "));
        sb.append(str);
        sb.append("\n");
    }

    @Override
    public void visitOutput(Object outputValue) {
        sb.append("Result: ");
        sb.append(outputValue);
        sb.append("\n");
    }
}
