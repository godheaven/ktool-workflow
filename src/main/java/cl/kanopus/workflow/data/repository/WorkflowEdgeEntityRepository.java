package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowEdgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowEdgeEntityRepository extends JpaRepository<WorkflowEdgeEntity, Long> {
}
