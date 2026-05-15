package cl.kanopus.workflow.engine.rule;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpelRuleEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();

    public boolean evaluate(String expression, Map<String, Object> variables) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        try {
            Expression exp = parser.parseExpression(expression);
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(variables);
            return Boolean.TRUE.equals(exp.getValue(context, Boolean.class));
        } catch (Exception e) {
            // Log error
            return false;
        }
    }
}
