package cl.kanopus.workflow.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wf_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private WorkflowVersion version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    private boolean isInitial;
    private boolean isFinal;
}
