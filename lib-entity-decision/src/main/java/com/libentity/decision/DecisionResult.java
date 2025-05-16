package com.libentity.decision;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
public abstract sealed class DecisionResult<O, V> {
    private final V inputValue;
    private final O output;
    Map<String, Object> variables;

    DecisionResult(V inputValue, O output, Map<String, Object> variables) {
        this.inputValue = inputValue;
        this.output = output;
        this.variables = variables;
    }

    public String diagnose() {
        var x = new DiagnosticTextVisitor<O, V>();
        x.visitResult(this);
        return x.getResult();
    }
    //    record CompileRuleEvaluationResult(boolean evaluated, boolean truthy) {}
    //
    //    record DecisionContext<V, O>(V input, List<CompileRuleEvaluationResult<V>> compileRuleEvaluationResults) {}

    @EqualsAndHashCode(callSuper = true)
    public static final class FirstMatch<O, V> extends DecisionResult<O, V> {

        public FirstMatch(V inputValue, O output, Map<String, Object> variables) {
            super(inputValue, output, variables);
        }

        //        private final Map<String, Boolean> resultByRuleName;
        //        private final Map<String, CompileRuleEvaluationResult> resultByRuleNamex;
        //        private final DecisionContext decisionContext;
        //        private final DecisionTable decisionTable;
    }

    @EqualsAndHashCode(callSuper = true)
    public static final class None<O, V> extends DecisionResult<O, V> {
        public None(V inputValue, O output, Map<String, Object> variables) {
            super(inputValue, output, variables);
        }
        //        private final DecisionContext<V> decisionContext;
        //        private final DecisionTable decisionTable;
    }
}
