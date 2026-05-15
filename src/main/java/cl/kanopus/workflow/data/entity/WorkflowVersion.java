package cl.kanopus.workflow.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wf_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "definition_id", nullable = false)
    private WorkflowDefinition definition;

    private int versionNumber;
    private boolean active;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL)
    private List<WorkflowState> states = new ArrayList<>();

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL)
    private List<WorkflowTransition> transitions = new ArrayList<>();

    @OneToOne(mappedBy = "version", cascade = CascadeType.ALL)
    private WorkflowForm form;
}
