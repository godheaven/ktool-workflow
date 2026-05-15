package cl.kanopus.workflow.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wf_transitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTransition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private WorkflowVersion version;

    @ManyToOne
    @JoinColumn(name = "from_state_id", nullable = false)
    private WorkflowState fromState;

    @ManyToOne
    @JoinColumn(name = "to_state_id", nullable = false)
    private WorkflowState toState;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String ruleExpression;
}
