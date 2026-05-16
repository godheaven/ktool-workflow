package cl.kanopus.workflow.web.dto;

import cl.kanopus.workflow.model.enums.NodeType;
import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeDTO {
    private String id;
    private NodeType type;
    private String label;
    private Double x;
    private Double y;
    private Map<String, Object> config;
    private Integer slaHours;
}
