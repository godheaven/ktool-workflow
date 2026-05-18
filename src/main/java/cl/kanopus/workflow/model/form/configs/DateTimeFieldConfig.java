package cl.kanopus.workflow.model.form.configs;

import lombok.Data;

@Data
public class DateTimeFieldConfig implements FieldConfig {
    private String minDateTime;
    private String maxDateTime;
    private String timezone;
    private String dateFormat;
    private String timeFormat;
}
