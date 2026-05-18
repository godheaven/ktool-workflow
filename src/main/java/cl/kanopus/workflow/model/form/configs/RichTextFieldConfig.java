package cl.kanopus.workflow.model.form.configs;

import lombok.Data;
import java.util.List;

@Data
public class RichTextFieldConfig implements FieldConfig {
    private List<String> toolbarOptions;
    private Integer maxLength;
    private Boolean allowImages;
    private Boolean allowTables;
    private Boolean allowHtml;
    private Boolean sanitizeHtml;
}
