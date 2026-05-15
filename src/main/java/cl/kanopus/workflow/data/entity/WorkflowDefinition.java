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

    private boolean active = true;

    private String complexity = "Low";
    private int rating = 3;
    private String ownerName = "Ioni owcher";
    private String ownerAvatarUrl = "https://ui-avatars.com/api/?name=Ioni+Owcher&background=random";
    private String lastUpdated = "09/13/2015";
    private String versionLabel = "v 1.0";
    private boolean enabled = true;

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL)
    private List<WorkflowVersion> versions = new ArrayList<>();
}
