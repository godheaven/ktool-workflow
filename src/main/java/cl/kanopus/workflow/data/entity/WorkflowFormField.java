package cl.kanopus.workflow.data.entity;

import cl.kanopus.workflow.model.form.FieldType;
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

    @Column(name = "field_key", nullable = false)
    private String key; // semantic key (e.g., incidentType)

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType fieldType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String helpText;
    private String placeholder;

    private boolean required;
    private boolean readonly;
    private boolean hidden;
    private boolean disabled;
    private boolean searchable;
    private boolean auditable;
    private boolean sensitive;

    private int orderIndex;
    private String sectionKey;
    private String cssClass;
    private String icon;

    @Column(columnDefinition = "TEXT")
    private String configJson;

    @Column(columnDefinition = "TEXT")
    private String rulesJson;

    @Column(columnDefinition = "TEXT")
    private String layoutJson;
}
