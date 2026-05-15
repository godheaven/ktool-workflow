package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowFormField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowFormFieldRepository extends JpaRepository<WorkflowFormField, Long> {
}
