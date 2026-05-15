package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
    List<WorkflowTask> findByAssigneeIdAndStatus(Long assigneeId, String status);
    List<WorkflowTask> findByRoleIdAndStatus(Long roleId, String status);
}
