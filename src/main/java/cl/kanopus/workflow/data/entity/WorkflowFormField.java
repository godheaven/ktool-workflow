package cl.kanopus.workflow.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wf_form_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowFormField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private WorkflowForm form;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String fieldType; // TEXT, NUMBER, DATE, SELECT, etc.

    private boolean required;
    private int orderIndex;

    @Column(columnDefinition = "TEXT")
    private String options;
}
