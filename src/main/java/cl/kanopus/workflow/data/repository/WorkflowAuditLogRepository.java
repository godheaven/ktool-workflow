package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowAuditLogRepository extends JpaRepository<WorkflowAuditLog, Long> {
    List<WorkflowAuditLog> findByInstanceIdOrderByCreatedAtDesc(Long instanceId);
}
