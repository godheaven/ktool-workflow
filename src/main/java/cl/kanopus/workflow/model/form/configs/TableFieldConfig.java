package cl.kanopus.workflow.model.form.configs;

import lombok.Data;
import java.util.List;

@Data
public class TableFieldConfig implements FieldConfig {
    private List<ColumnConfig> columns;
    private Boolean addRowEnabled;
    private Boolean deleteRowEnabled;
    private Integer maxRows;
    private Integer minRows;
    private Boolean sortable;
    private Boolean inlineEdit;
    private Boolean pagination;

    @Data
    public static class ColumnConfig {
        private String key;
        private String label;
        private String type; // Reusing FieldType as string for simplicity in JSON
        private Object config; // Nested config
    }
}
