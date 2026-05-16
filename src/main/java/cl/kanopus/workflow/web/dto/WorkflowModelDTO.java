package cl.kanopus.workflow.web.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowModelDTO {
    private List<NodeDTO> nodes;
    private List<EdgeDTO> edges;
}
