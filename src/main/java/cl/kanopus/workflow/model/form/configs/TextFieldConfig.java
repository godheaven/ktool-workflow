package cl.kanopus.workflow.model.form.configs;

import lombok.Data;
import java.util.List;

@Data
public class TextFieldConfig implements FieldConfig {
    private Integer minLength;
    private Integer maxLength;
    private String regex;
    private String mask;
    private Boolean uppercase;
    private Boolean lowercase;
    private Boolean trim;
    private String autocomplete;
    private List<String> allowedCharacters;
}
