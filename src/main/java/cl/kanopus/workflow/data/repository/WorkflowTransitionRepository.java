package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
    List<WorkflowTransition> findByFromStateId(Long fromStateId);
}
