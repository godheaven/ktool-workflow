package cl.kanopus.workflow.web.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeDTO {
    private String id;
    private String source;
    private String target;
    private String sourceOutput;
    private String targetInput;
    private String label;
    private String condition;
}
