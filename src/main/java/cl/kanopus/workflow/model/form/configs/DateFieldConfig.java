package cl.kanopus.workflow.model.form.configs;

import lombok.Data;

@Data
public class DateFieldConfig implements FieldConfig {
    private String minDate;
    private String maxDate;
    private String dateFormat;
    private Boolean disableWeekends;
    private Boolean disablePast;
    private Boolean disableFuture;
}
