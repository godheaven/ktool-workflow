package cl.kanopus.workflow.model.form.rules;

import lombok.Data;
import java.util.List;

@Data
public class FieldRules {
    private List<RuleCondition> visibleIf;
    private List<RuleCondition> requiredIf;
    private List<RuleCondition> readonlyIf;
    private List<RuleCondition> enabledIf;
    private List<RuleCondition> validateIf;
    private String calculateIf; // Expression string
    private List<ValidationRule> customValidations;

    @Data
    public static class ValidationRule {
        private String type; // ValidationType
        private Object value;
        private String message;
        private List<RuleCondition> applyIf;
    }
}
