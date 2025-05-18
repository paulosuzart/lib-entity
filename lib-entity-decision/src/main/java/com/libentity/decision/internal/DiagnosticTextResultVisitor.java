package com.libentity.decision.internal;

import static java.lang.Math.max;

import com.libentity.decision.DecisionResultVisitor;
import com.libentity.decision.HitPolicy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class DiagnosticTextResultVisitor<O, V> implements DecisionResultVisitor<O, V> {

    private StringBuilder sb = new StringBuilder();
    private int rule = 0;
    private int longestVariableSize = 3;

    public String getResult() {
        return sb.toString();
    }

    @Override
    public void visitResult(HitPolicy<O, V> hitPolicy) {
        sb.append("Diagnostics: \n");
        sb.append("Hit Policy: ").append(hitPolicy.getClass().getSimpleName()).append("\n");
        if (hitPolicy instanceof HitPolicy.First<O, V> c) {
            visitOutput(c.getOutput());
            visitInputVariables(hitPolicy.getVariables());
            visitEvaluatedRules(c.getEvaluatedRules());
        } else if (hitPolicy instanceof HitPolicy.Collect<O, V> c) {
            visitOutput(c.getOutput());
            visitInputVariables(hitPolicy.getVariables());
            visitEvaluatedRules(c.getEvaluatedRules());

        } else if (hitPolicy instanceof HitPolicy.Unique<O, V> c) {
            visitOutput(c.getOutput());
            visitInputVariables(hitPolicy.getVariables());
            visitEvaluatedRules(c.getEvaluatedRules());
        } else if (hitPolicy instanceof HitPolicy.Sum<O, V> c) {
            visitOutput(c.getOutput());
            visitInputVariables(hitPolicy.getVariables());
            visitEvaluatedRules(c.getEvaluatedRules());
        }
        sb.append("\n");
    }

    private void visitEvaluatedRules(List<HitPolicy.EvaluatedMatchingRule<V, O>> evaluatedRules) {
        for (HitPolicy.EvaluatedMatchingRule<V, O> evaluatedRule : evaluatedRules) {
            visitEvaluatedRule(evaluatedRule);
        }
    }

    private void visitEvaluatedRule(HitPolicy.EvaluatedMatchingRule<V, O> evaluatedRule) {
        sb.append("Rule ");
        sb.append(rule++);
        sb.append(" [").append(getTruthyMarker(evaluatedRule.truthy())).append("]:\n");
        for (HitPolicy.EvaluatedCompiledRule<V> vEvaluatedCompiledRule : evaluatedRule.evaluatedRuleList()) {
            visitEvaluatedCompiledRule(vEvaluatedCompiledRule);
        }
    }

    private String getTruthyMarker(boolean truthy) {
        return truthy ? "t" : "f";
    }

    private void visitEvaluatedCompiledRule(HitPolicy.EvaluatedCompiledRule<V> vEvaluatedCompiledRule) {
        sb.append("  ");
        sb.append(StringUtils.rightPad(vEvaluatedCompiledRule.rule().name(), longestVariableSize + 2))
                .append("[")
                .append(getTruthyMarker(vEvaluatedCompiledRule.truthy()))
                .append("]: ");
        sb.append(vEvaluatedCompiledRule.rule().matcher());
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
