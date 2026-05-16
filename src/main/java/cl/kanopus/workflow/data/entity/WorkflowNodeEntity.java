package cl.kanopus.workflow.data.entity;

import cl.kanopus.workflow.model.enums.NodeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "designer_workflow_nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowNodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The ID provided by the visual editor (e.g., Drawflow ID)
    private String visualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private WorkflowVersionEntity version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeType type;

    private String label;

    private Double positionX;
    private Double positionY;

    @Column(columnDefinition = "TEXT")
    private String configJson;

    private Integer slaHours;
    private String timeoutAction;
    private String escalationRule;
    private String visibilityRule;
}
