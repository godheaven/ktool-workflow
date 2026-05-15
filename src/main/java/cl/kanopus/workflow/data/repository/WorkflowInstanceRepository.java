package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    List<WorkflowInstance> findByCreatorId(Long creatorId);
}
