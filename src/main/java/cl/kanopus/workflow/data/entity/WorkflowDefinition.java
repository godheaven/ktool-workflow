package cl.kanopus.workflow.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wf_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private String complexity = "Low";
    @Builder.Default
    private int rating = 3;
    @Builder.Default
    private String ownerName = "Ioni owcher";
    @Builder.Default
    private String ownerAvatarUrl = "https://ui-avatars.com/api/?name=Ioni+Owcher&background=random";
    @Builder.Default
    private String lastUpdated = "09/13/2015";
    @Builder.Default
    private String versionLabel = "v 1.0";
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL)
    private List<WorkflowVersion> versions = new ArrayList<>();
}
