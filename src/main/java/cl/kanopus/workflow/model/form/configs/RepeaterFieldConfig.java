package cl.kanopus.workflow.model.form.configs;

import cl.kanopus.workflow.web.dto.form.FieldDefinitionDTO;
import lombok.Data;
import java.util.List;

@Data
public class RepeaterFieldConfig implements FieldConfig {
    private List<FieldDefinitionDTO> templateFields;
    private Integer minItems;
    private Integer maxItems;
    private Boolean collapsible;
    private Boolean sortable;
}
