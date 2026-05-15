package cl.kanopus.workflow.data.repository;

import cl.kanopus.workflow.data.entity.WorkflowDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    
    @Query("SELECT w FROM WorkflowDefinition w WHERE " +
           "(:search IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:complexity IS NULL OR w.complexity = :complexity) AND " +
           "(:enabled IS NULL OR w.enabled = :enabled)")
    Page<WorkflowDefinition> findByFilters(
            @Param("search") String search,
            @Param("complexity") String complexity,
            @Param("enabled") Boolean enabled,
            Pageable pageable);
}
