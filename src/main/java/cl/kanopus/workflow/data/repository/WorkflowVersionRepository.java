package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkflowVersionRepository extends JpaRepository<WorkflowVersion, Long> {
    Optional<WorkflowVersion> findByDefinitionIdAndActiveTrue(Long definitionId);
}
