package cl.kanopus.workflow.model.form.configs;

import lombok.Data;
import java.util.List;

@Data
public class OptionsFieldConfig implements FieldConfig {
    private List<Option> options;
    private String layout; // VERTICAL, HORIZONTAL
    private Boolean allowOther;
    private String otherLabel;
    private Boolean searchable;
    private Boolean clearable;
    private String dynamicSource; // URL or Service Key
    private Integer minSelections;
    private Integer maxSelections;
    private Boolean chips;
    private Boolean allowCustomValues;

    @Data
    public static class Option {
        private String label;
        private String value;
    }
}
