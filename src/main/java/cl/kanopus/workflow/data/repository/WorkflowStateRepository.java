package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStateRepository extends JpaRepository<WorkflowState, Long> {
}
