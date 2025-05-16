package com.libentity.decision;

import static java.lang.Math.max;

import java.util.Map;
import java.util.stream.Collectors;

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
            sb.append("Result: None\n");
        } else if (decisionResult instanceof DecisionResult.FirstMatch<O, V> f) {
            visitOutput(decisionResult.getOutput());
            visitInputVariables(f.getVariables());
            sb.append("\n");
        }
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
