package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface WorkflowDefinitionEntityRepository extends JpaRepository<WorkflowDefinitionEntity, Long> {
    
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE " +
           "(:search IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:complexity IS NULL OR w.complexity = :complexity) AND " +
           "(:enabled IS NULL OR w.enabled = :enabled)")
    Page<WorkflowDefinitionEntity> findByFilters(
            @Param("search") String search, 
            @Param("complexity") String complexity, 
            @Param("enabled") Boolean enabled, 
            Pageable pageable);
}
