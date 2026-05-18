package cl.kanopus.workflow.model.form.configs;

import lombok.Data;

@Data
public class BooleanFieldConfig implements FieldConfig {
    private String trueLabel;
    private String falseLabel;
    private Boolean defaultValue;
    private Boolean renderAsSwitch;
    private String checkboxLabel;
}
