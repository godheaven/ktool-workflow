package cl.kanopus.workflow.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wf_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private WorkflowVersion version;

    @ManyToOne
    @JoinColumn(name = "current_state_id", nullable = false)
    private WorkflowState currentState;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, CANCELLED

    @Builder.Default
    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL)
    private List<WorkflowInstanceData> data = new ArrayList<>();
}
