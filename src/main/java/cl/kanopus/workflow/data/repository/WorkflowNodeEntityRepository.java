package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowNodeEntityRepository extends JpaRepository<WorkflowNodeEntity, Long> {
}
