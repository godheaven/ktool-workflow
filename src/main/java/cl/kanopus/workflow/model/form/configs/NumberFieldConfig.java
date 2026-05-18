package cl.kanopus.workflow.model.form.configs;

import lombok.Data;

@Data
public class NumberFieldConfig implements FieldConfig {
    private Double min;
    private Double max;
    private Double step;
    private Boolean allowNegative;
    private String thousandSeparator;
}
