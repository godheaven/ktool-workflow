package cl.kanopus.workflow.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "designer_workflow_edges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowEdgeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private WorkflowVersionEntity version;

    private String sourceNodeId;
    private String targetNodeId;
    
    private String sourceOutput;
    private String targetInput;

    @Column(columnDefinition = "TEXT")
    private String conditionExpression;

    private String label;
}
