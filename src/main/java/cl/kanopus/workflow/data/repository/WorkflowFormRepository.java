package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowForm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkflowFormRepository extends JpaRepository<WorkflowForm, Long> {
    Optional<WorkflowForm> findByVersionId(Long versionId);
}
