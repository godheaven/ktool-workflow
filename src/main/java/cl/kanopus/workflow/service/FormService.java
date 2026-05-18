package cl.kanopus.workflow.service;

import cl.kanopus.workflow.data.entity.WorkflowForm;
import cl.kanopus.workflow.data.entity.WorkflowFormField;
import cl.kanopus.workflow.model.form.configs.FieldConfig;
import cl.kanopus.workflow.model.form.rules.FieldRules;
import cl.kanopus.workflow.web.dto.form.FieldDefinitionDTO;
import cl.kanopus.workflow.web.dto.form.WorkflowFormDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FormService {

    private final ObjectMapper objectMapper;

    public WorkflowFormDTO toDTO(WorkflowForm entity) {
        if (entity == null) return null;
        
        List<FieldDefinitionDTO> fieldDtos = entity.getFields().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        WorkflowFormDTO dto = WorkflowFormDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .fields(fieldDtos)
                .build();

        // Parse sections from layoutJson
        if (entity.getLayoutJson() != null) {
            try {
                Map<String, Object> layout = objectMapper.readValue(entity.getLayoutJson(), Map.class);
                if (layout.containsKey("sections")) {
                    dto.setSections(objectMapper.convertValue(layout.get("sections"), 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, WorkflowFormDTO.FormSectionDTO.class)));
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing form layout for id: {}", entity.getId(), e);
            }
        }

        return dto;
    }

    public FieldDefinitionDTO toDTO(WorkflowFormField entity) {
        FieldDefinitionDTO dto = FieldDefinitionDTO.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .label(entity.getLabel())
                .type(entity.getFieldType())
                .description(entity.getDescription())
                .helpText(entity.getHelpText())
                .placeholder(entity.getPlaceholder())
                .required(entity.isRequired())
                .readonly(entity.isReadonly())
                .hidden(entity.isHidden())
                .disabled(entity.isDisabled())
                .searchable(entity.isSearchable())
                .auditable(entity.isAuditable())
                .sensitive(entity.isSensitive())
                .orderIndex(entity.getOrderIndex())
                .sectionKey(entity.getSectionKey())
                .cssClass(entity.getCssClass())
                .icon(entity.getIcon())
                .build();

        try {
            if (entity.getConfigJson() != null) {
                dto.setConfig(objectMapper.readValue(entity.getConfigJson(), FieldConfig.class));
            }
            if (entity.getRulesJson() != null) {
                dto.setRules(objectMapper.readValue(entity.getRulesJson(), FieldRules.class));
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing form field metadata for key: {}", entity.getKey(), e);
        }

        return dto;
    }

    public void updateEntityFromDTO(WorkflowFormField entity, FieldDefinitionDTO dto) {
        entity.setKey(dto.getKey());
        entity.setLabel(dto.getLabel());
        entity.setFieldType(dto.getType());
        entity.setDescription(dto.getDescription());
        entity.setHelpText(dto.getHelpText());
        entity.setPlaceholder(dto.getPlaceholder());
        entity.setRequired(dto.isRequired());
        entity.setReadonly(dto.isReadonly());
        entity.setHidden(dto.isHidden());
        entity.setDisabled(dto.isDisabled());
        entity.setSearchable(dto.isSearchable());
        entity.setAuditable(dto.isAuditable());
        entity.setSensitive(dto.isSensitive());
        entity.setOrderIndex(dto.getOrderIndex());
        entity.setSectionKey(dto.getSectionKey());
        entity.setCssClass(dto.getCssClass());
        entity.setIcon(dto.getIcon());

        try {
            if (dto.getConfig() != null) {
                entity.setConfigJson(objectMapper.writeValueAsString(dto.getConfig()));
            }
            if (dto.getRules() != null) {
                entity.setRulesJson(objectMapper.writeValueAsString(dto.getRules()));
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing form field metadata for key: {}", dto.getKey(), e);
        }
    }
}
