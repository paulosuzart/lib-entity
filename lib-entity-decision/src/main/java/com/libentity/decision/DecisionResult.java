package com.libentity.decision;

import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
public abstract sealed class DecisionResult<O, V> {
    private final V inputValue;
    private final O output;
    private final Map<String, Object> variables;
    private final List<EvaluatedRule<V>> evaluatedRules;

    DecisionResult(V inputValue, O output, Map<String, Object> variables, List<EvaluatedRule<V>> evaluatedRules) {
        this.inputValue = inputValue;
        this.output = output;
        this.variables = variables;
        this.evaluatedRules = evaluatedRules;
    }

    public String diagnose() {
        var x = new DiagnosticTextVisitor<O, V>();
        x.visitResult(this);
        return x.getResult();
    }

    public record EvaluatedCompiledRule<V>(CompiledRule<V, ?> rule, boolean evaluated, boolean truthy) {}

    public record EvaluatedRule<V>(boolean evaluated, boolean truthy, List<EvaluatedCompiledRule<V>> evaluatedRuleList) {}

    @EqualsAndHashCode(callSuper = true)
    public static final class FirstMatch<O, V> extends DecisionResult<O, V> {

        public FirstMatch(V inputValue, O output, Map<String, Object> variables, List<EvaluatedRule<V>> evaluatedRules) {
            super(inputValue, output, variables, evaluatedRules);
        }

        //        private final Map<String, Boolean> resultByRuleName;
        //        private final Map<String, CompileRuleEvaluationResult> resultByRuleNamex;
        //        private final DecisionContext decisionContext;
        //        private final DecisionTable decisionTable;
    }

    @EqualsAndHashCode(callSuper = true)
    public static final class None<O, V> extends DecisionResult<O, V> {
        public None(V inputValue, O output, Map<String, Object> variables, List<EvaluatedRule<V>> evaluatedRules) {
            super(inputValue, output, variables, evaluatedRules);
        }
        //        private final DecisionContext<V> decisionContext;
        //        private final DecisionTable decisionTable;
    }
}
