package cl.kanopus.workflow.service;

import cl.kanopus.workflow.data.entity.*;
import cl.kanopus.workflow.data.repository.*;
import cl.kanopus.workflow.model.enums.WorkflowStatus;
import cl.kanopus.workflow.web.dto.EdgeDTO;
import cl.kanopus.workflow.web.dto.NodeDTO;
import cl.kanopus.workflow.web.dto.WorkflowModelDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowDesignerService {

    private final WorkflowDefinitionEntityRepository definitionRepository;
    private final WorkflowVersionEntityRepository versionRepository;
    private final ObjectMapper objectMapper;

    public List<WorkflowDefinitionEntity> getAllDefinitions() {
        return definitionRepository.findAll();
    }

    public WorkflowDefinitionEntity saveDefinition(WorkflowDefinitionEntity definition) {
        return definitionRepository.save(definition);
    }

    public WorkflowDefinitionEntity getDefinition(Long id) {
        return definitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow definition not found: " + id));
    }

    @Transactional
    public WorkflowDefinitionEntity createDefinition(String name, String description) {
        WorkflowDefinitionEntity definition = WorkflowDefinitionEntity.builder()
                .name(name)
                .description(description)
                .status(WorkflowStatus.DRAFT)
                .currentVersion(1)
                .build();
        
        definition = definitionRepository.save(definition);
        
        WorkflowVersionEntity draft = WorkflowVersionEntity.builder()
                .definition(definition)
                .versionNumber(1)
                .isDraft(true)
                .build();
        
        versionRepository.save(draft);
        return definition;
    }

    public WorkflowVersionEntity getDraft(Long definitionId) {
        return versionRepository.findByDefinitionIdAndIsDraftTrue(definitionId)
                .orElseGet(() -> {
                    log.info("Draft not found for definition {}, creating a new one.", definitionId);
                    WorkflowDefinitionEntity definition = getDefinition(definitionId);
                    WorkflowVersionEntity draft = WorkflowVersionEntity.builder()
                            .definition(definition)
                            .versionNumber(definition.getCurrentVersion())
                            .isDraft(true)
                            .build();
                    return versionRepository.save(draft);
                });
    }

    public WorkflowModelDTO getModel(Long versionId) {
        WorkflowVersionEntity version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found: " + versionId));
        
        List<NodeDTO> nodes = version.getNodes().stream().map(node -> {
            try {
                Map<String, Object> config = objectMapper.readValue(node.getConfigJson(), Map.class);
                return NodeDTO.builder()
                        .id(node.getVisualId())
                        .type(node.getType())
                        .label(node.getLabel())
                        .x(node.getPositionX())
                        .y(node.getPositionY())
                        .config(config)
                        .slaHours(node.getSlaHours())
                        .build();
            } catch (Exception e) {
                log.error("Error parsing node config JSON", e);
                return null;
            }
        }).collect(Collectors.toList());

        List<EdgeDTO> edges = version.getEdges().stream().map(edge -> 
            EdgeDTO.builder()
                    .id(edge.getId().toString())
                    .source(edge.getSourceNodeId())
                    .target(edge.getTargetNodeId())
                    .sourceOutput(edge.getSourceOutput())
                    .targetInput(edge.getTargetInput())
                    .label(edge.getLabel())
                    .condition(edge.getConditionExpression())
                    .build()
        ).collect(Collectors.toList());

        return WorkflowModelDTO.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    @Transactional
    public void saveModel(Long versionId, WorkflowModelDTO model) {
        WorkflowVersionEntity version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found: " + versionId));
        
        if (!version.isDraft()) {
            throw new RuntimeException("Cannot edit a published version");
        }

        // Clear existing nodes and edges
        version.getNodes().clear();
        version.getEdges().clear();
        versionRepository.saveAndFlush(version);

        // Add new nodes
        List<WorkflowNodeEntity> nodes = new ArrayList<>();
        for (NodeDTO nodeDto : model.getNodes()) {
            try {
                String configJson = objectMapper.writeValueAsString(nodeDto.getConfig());
                nodes.add(WorkflowNodeEntity.builder()
                        .version(version)
                        .visualId(nodeDto.getId())
                        .type(nodeDto.getType())
                        .label(nodeDto.getLabel())
                        .positionX(nodeDto.getX())
                        .positionY(nodeDto.getY())
                        .configJson(configJson)
                        .slaHours(nodeDto.getSlaHours())
                        .build());
            } catch (JsonProcessingException e) {
                log.error("Error serializing node config", e);
            }
        }
        version.getNodes().addAll(nodes);

        // Add new edges
        List<WorkflowEdgeEntity> edges = new ArrayList<>();
        for (EdgeDTO edgeDto : model.getEdges()) {
            edges.add(WorkflowEdgeEntity.builder()
                    .version(version)
                    .sourceNodeId(edgeDto.getSource())
                    .targetNodeId(edgeDto.getTarget())
                    .sourceOutput(edgeDto.getSourceOutput())
                    .targetInput(edgeDto.getTargetInput())
                    .label(edgeDto.getLabel())
                    .conditionExpression(edgeDto.getCondition())
                    .build());
        }
        version.getEdges().addAll(edges);

        versionRepository.save(version);
        
        // Update definition timestamp
        WorkflowDefinitionEntity definition = version.getDefinition();
        definition.setUpdatedAt(java.time.LocalDateTime.now());
        definitionRepository.save(definition);
    }

    @Transactional
    public void publishWorkflow(Long definitionId) {
        WorkflowVersionEntity draft = getDraft(definitionId);
        
        // Validate before publishing
        List<String> errors = validateWorkflow(draft);
        if (!errors.isEmpty()) {
            throw new RuntimeException("Validation failed: " + String.join(", ", errors));
        }

        // 1. Mark current draft as not draft
        draft.setDraft(false);
        
        // 2. Update definition status
        WorkflowDefinitionEntity definition = draft.getDefinition();
        definition.setStatus(WorkflowStatus.PUBLISHED);
        definitionRepository.save(definition);
        
        // 3. Create a NEW draft by duplicating this published version
        duplicateToNewDraft(draft);
    }

    private void duplicateToNewDraft(WorkflowVersionEntity source) {
        WorkflowDefinitionEntity definition = source.getDefinition();
        int newVersionNumber = definition.getCurrentVersion() + 1;
        definition.setCurrentVersion(newVersionNumber);
        definitionRepository.save(definition);

        WorkflowVersionEntity newDraft = WorkflowVersionEntity.builder()
                .definition(definition)
                .versionNumber(newVersionNumber)
                .isDraft(true)
                .build();
        
        newDraft = versionRepository.save(newDraft);

        // Duplicate nodes
        List<WorkflowNodeEntity> newNodes = new ArrayList<>();
        for (WorkflowNodeEntity node : source.getNodes()) {
            newNodes.add(WorkflowNodeEntity.builder()
                    .version(newDraft)
                    .visualId(node.getVisualId())
                    .type(node.getType())
                    .label(node.getLabel())
                    .positionX(node.getPositionX())
                    .positionY(node.getPositionY())
                    .configJson(node.getConfigJson())
                    .slaHours(node.getSlaHours())
                    .build());
        }
        newDraft.setNodes(newNodes);

        // Duplicate edges
        List<WorkflowEdgeEntity> newEdges = new ArrayList<>();
        for (WorkflowEdgeEntity edge : source.getEdges()) {
            newEdges.add(WorkflowEdgeEntity.builder()
                    .version(newDraft)
                    .sourceNodeId(edge.getSourceNodeId())
                    .targetNodeId(edge.getTargetNodeId())
                    .sourceOutput(edge.getSourceOutput())
                    .targetInput(edge.getTargetInput())
                    .label(edge.getLabel())
                    .conditionExpression(edge.getConditionExpression())
                    .build());
        }
        newDraft.setEdges(newEdges);

        versionRepository.save(newDraft);
    }

    public List<String> validateWorkflow(WorkflowVersionEntity version) {
        List<String> errors = new ArrayList<>();
        List<WorkflowNodeEntity> nodes = version.getNodes();
        List<WorkflowEdgeEntity> edges = version.getEdges();

        if (nodes.isEmpty()) {
            errors.add("Workflow has no nodes.");
            return errors;
        }

        // 1. Exactly one Start node
        long startNodes = nodes.stream().filter(n -> n.getType().name().equals("START")).count();
        if (startNodes != 1) errors.add("Must have exactly one Start node (found " + startNodes + ").");

        // 2. At least one End node
        long endNodes = nodes.stream().filter(n -> n.getType().name().equals("END")).count();
        if (endNodes < 1) errors.add("Must have at least one End node.");

        // 3. Check for orphans and connections
        for (WorkflowNodeEntity node : nodes) {
            boolean hasIncoming = edges.stream().anyMatch(e -> e.getTargetNodeId().equals(node.getVisualId()));
            boolean hasOutgoing = edges.stream().anyMatch(e -> e.getSourceNodeId().equals(node.getVisualId()));

            if (node.getType().name().equals("START")) {
                if (!hasOutgoing) errors.add("Start node must have outgoing connections.");
            } else if (node.getType().name().equals("END")) {
                if (!hasIncoming) errors.add("End node '" + node.getLabel() + "' must have incoming connections.");
            } else {
                if (!hasIncoming) errors.add("Node '" + node.getLabel() + "' has no incoming connections.");
                if (!hasOutgoing) errors.add("Node '" + node.getLabel() + "' has no outgoing connections.");
            }
        }

        return errors;
    }
    
    @Transactional
    public void deprecateWorkflow(Long id) {
        WorkflowDefinitionEntity definition = getDefinition(id);
        definition.setStatus(WorkflowStatus.DEPRECATED);
        definitionRepository.save(definition);
    }

    @Transactional
    public WorkflowDefinitionEntity duplicateWorkflow(Long id) {
        WorkflowDefinitionEntity source = getDefinition(id);
        WorkflowDefinitionEntity copy = createDefinition("Copy of " + source.getName(), source.getDescription());
        
        // Find the latest version (not draft if possible, or current draft)
        WorkflowVersionEntity latestVersion = source.getVersions().stream()
                .filter(v -> !v.isDraft())
                .findFirst()
                .orElse(getDraft(id));
        
        WorkflowVersionEntity newDraft = getDraft(copy.getId());
        
        // Copy nodes and edges to the new draft
        List<WorkflowNodeEntity> newNodes = new ArrayList<>();
        for (WorkflowNodeEntity node : latestVersion.getNodes()) {
            newNodes.add(WorkflowNodeEntity.builder()
                    .version(newDraft)
                    .visualId(node.getVisualId())
                    .type(node.getType())
                    .label(node.getLabel())
                    .positionX(node.getPositionX())
                    .positionY(node.getPositionY())
                    .configJson(node.getConfigJson())
                    .slaHours(node.getSlaHours())
                    .build());
        }
        newDraft.setNodes(newNodes);

        List<WorkflowEdgeEntity> newEdges = new ArrayList<>();
        for (WorkflowEdgeEntity edge : latestVersion.getEdges()) {
            newEdges.add(WorkflowEdgeEntity.builder()
                    .version(newDraft)
                    .sourceNodeId(edge.getSourceNodeId())
                    .targetNodeId(edge.getTargetNodeId())
                    .sourceOutput(edge.getSourceOutput())
                    .targetInput(edge.getTargetInput())
                    .label(edge.getLabel())
                    .conditionExpression(edge.getConditionExpression())
                    .build());
        }
        newDraft.setEdges(newEdges);
        
        versionRepository.save(newDraft);
        return copy;
    }
}
