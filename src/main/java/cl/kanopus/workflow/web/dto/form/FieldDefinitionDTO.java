package cl.kanopus.workflow.web.dto.form;

import cl.kanopus.workflow.model.form.FieldType;
import cl.kanopus.workflow.model.form.configs.FieldConfig;
import cl.kanopus.workflow.model.form.rules.FieldRules;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinitionDTO {
    private Long id;
    private String key;
    private String label;
    private FieldType type;
    private String description;
    private String helpText;
    private String placeholder;
    
    private boolean required;
    private boolean readonly;
    private boolean hidden;
    private boolean disabled;
    private boolean searchable;
    private boolean auditable;
    private boolean sensitive;
    
    private int orderIndex;
    private String sectionKey;
    private String cssClass;
    private String icon;
    
    private FieldConfig config;
    private FieldRules rules;
    private Object layout; // Generic layout metadata
}
