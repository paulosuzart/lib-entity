package com.libentity.decision;

import static java.lang.Math.max;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class DiagnosticTextVisitor<O, V> implements DecisionVisitor<O, V> {

    private StringBuilder sb = new StringBuilder();
    int rule = 0;
    int longestVariableSize = 1;

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
        sb.append("[")
                .append(evaluatedRule.truthy() ? (char) 0x2717 : (char) 0x2713)
                .append("]:\n");
        for (DecisionResult.EvaluatedCompiledRule<V> vEvaluatedCompiledRule : evaluatedRule.evaluatedRuleList()) {
            visitEvaluatedCompiledRule(vEvaluatedCompiledRule);
        }
    }

    private void visitEvaluatedCompiledRule(DecisionResult.EvaluatedCompiledRule<V> vEvaluatedCompiledRule) {
        sb.append("  ");
        sb.append(StringUtils.rightPad(
                        vEvaluatedCompiledRule.rule().name(),
                        longestVariableSize
                                - vEvaluatedCompiledRule.rule().name().length()))
                .append("[")
                .append(vEvaluatedCompiledRule.truthy() ? "✓" : "✗")
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
    public void visitDecisionTable(DecisionTable decisionTable) {
        sb.append("Diagnostic Table. Name: \n");
        sb.append(decisionTable.getName());
        sb.append("\n");
    }

    @Override
    public void visitMatchingRule(MatchingRule matchingRule) {
        sb.append("Matching Rule[ \n");
    }

    @Override
    public void visitInput(Object inputValue) {
        sb.append("Input: ");
        sb.append(inputValue);
    }

    @Override
    public void visitRule(Rule rule) {}

    @Override
    public void visitOutput(Object outputValue) {
        sb.append("Result: ");
        sb.append(outputValue);
        sb.append("\n");
    }
}
