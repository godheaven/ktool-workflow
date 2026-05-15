package cl.kanopus.workflow.service;

import cl.kanopus.workflow.data.entity.*;
import cl.kanopus.workflow.data.repository.*;
import cl.kanopus.workflow.engine.execution.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowVersionRepository versionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowEngine workflowEngine;

    public org.springframework.data.domain.Page<WorkflowDefinition> findAllDefinitions(
            String search, String complexity, Boolean enabled, org.springframework.data.domain.Pageable pageable) {
        if ((search == null || search.isEmpty()) && (complexity == null || complexity.isEmpty()) && enabled == null) {
            return definitionRepository.findAll(pageable);
        }
        return definitionRepository.findByFilters(search, complexity, enabled, pageable);
    }

    public List<WorkflowDefinition> findAllDefinitions() {
        return definitionRepository.findAll();
    }

    public WorkflowDefinition saveDefinition(WorkflowDefinition definition) {
        return definitionRepository.save(definition);
    }

    @Transactional
    public WorkflowInstance createInstance(Long definitionId, User creator, Map<String, String> data) {
        WorkflowVersion version = versionRepository.findByDefinitionIdAndActiveTrue(definitionId)
                .orElseThrow(() -> new RuntimeException("No active version found for definition"));
        
        return workflowEngine.startWorkflow(version, creator, data);
    }

    public WorkflowInstance findInstanceById(Long id) {
        return instanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instance not found"));
    }
}
