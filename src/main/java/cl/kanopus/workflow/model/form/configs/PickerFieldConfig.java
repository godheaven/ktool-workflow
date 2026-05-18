package cl.kanopus.workflow.model.form.configs;

import lombok.Data;
import java.util.List;

@Data
public class PickerFieldConfig implements FieldConfig {
    private Boolean multiple;
    private List<String> allowedRoles;
    private List<String> allowedGroups;
    private Boolean searchable;
    private Boolean showEmail;
    private Boolean showAvatar;
    private Boolean allowExternalUsers;
    
    // ORG specific
    private String organizationType;
    private List<Integer> allowedLevels;
    private Boolean hierarchical;
}
