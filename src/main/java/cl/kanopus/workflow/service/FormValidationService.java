package cl.kanopus.workflow.service;

import cl.kanopus.workflow.model.form.ValidationType;
import cl.kanopus.workflow.model.form.rules.FieldRules;
import cl.kanopus.workflow.web.dto.form.FieldDefinitionDTO;
import cl.kanopus.workflow.web.dto.form.WorkflowFormDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class FormValidationService {

    public List<ValidationError> validate(WorkflowFormDTO formDef, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        for (FieldDefinitionDTO field : formDef.getFields()) {
            Object value = data.get(field.getKey());

            // 1. Basic Required Validation
            if (field.isRequired() && (value == null || value.toString().isEmpty())) {
                errors.add(new ValidationError(field.getKey(), "This field is required"));
                continue;
            }

            if (value == null || value.toString().isEmpty()) continue;

            // 2. Type specific validations (from config)
            // Example for TEXT
            if (field.getType().name().equals("TEXT")) {
                // Here we would cast field.getConfig() to TextFieldConfig and check min/max
            }

            // 3. Custom rules from rules.customValidations
            if (field.getRules() != null && field.getRules().getCustomValidations() != null) {
                for (FieldRules.ValidationRule rule : field.getRules().getCustomValidations()) {
                    if (!validateRule(rule, value)) {
                        errors.add(new ValidationError(field.getKey(), rule.getMessage()));
                    }
                }
            }
        }

        return errors;
    }

    private boolean validateRule(FieldRules.ValidationRule rule, Object value) {
        // Implementation for REGEX, MIN, MAX, etc.
        return true; // Placeholder
    }

    public record ValidationError(String field, String message) {}
}
