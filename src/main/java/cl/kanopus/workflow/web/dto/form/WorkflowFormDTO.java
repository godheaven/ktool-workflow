package cl.kanopus.workflow.web.dto.form;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowFormDTO {
    private Long id;
    private String name;
    private String description;
    private List<FieldDefinitionDTO> fields;
    private List<FormSectionDTO> sections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormSectionDTO {
        private String key;
        private String label;
        private String icon;
        private int orderIndex;
    }
}
