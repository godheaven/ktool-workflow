package cl.kanopus.workflow.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wf_instance_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstanceData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instance_id", nullable = false)
    private WorkflowInstance instance;

    @Column(nullable = false)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String fieldValue;
}
