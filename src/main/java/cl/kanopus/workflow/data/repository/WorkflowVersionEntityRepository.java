package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowVersionEntityRepository extends JpaRepository<WorkflowVersionEntity, Long> {
    Optional<WorkflowVersionEntity> findByDefinitionIdAndIsDraftTrue(Long definitionId);
    Optional<WorkflowVersionEntity> findByDefinitionIdAndIsDraftFalse(Long definitionId);
    Optional<WorkflowVersionEntity> findByDefinitionIdAndVersionNumber(Long definitionId, Integer versionNumber);
}
