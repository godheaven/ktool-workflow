package cl.kanopus.workflow.model.form.rules;

import lombok.Data;

@Data
public class RuleCondition {
    private String field; // semantic key
    private String operator; // EQUALS, NOT_EQUALS, CONTAINS, GREATER_THAN, etc.
    private Object value;
}
