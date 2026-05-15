package cl.kanopus.workflow.engine.execution;

import cl.kanopus.workflow.data.entity.*;
import cl.kanopus.workflow.data.repository.*;
import cl.kanopus.workflow.engine.rule.SpelRuleEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowEngine {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowTaskRepository taskRepository;
    private final WorkflowAuditLogRepository auditLogRepository;
    private final SpelRuleEvaluator ruleEvaluator;

    @Transactional
    public WorkflowInstance startWorkflow(WorkflowVersion version, User creator, Map<String, String> initialData) {
        WorkflowState initialState = version.getStates().stream()
                .filter(WorkflowState::isInitial)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No initial state found for workflow version"));

        WorkflowInstance instance = WorkflowInstance.builder()
                .version(version)
                .currentState(initialState)
                .creator(creator)
                .createdAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();

        final WorkflowInstance finalInstance = instance;

        // Save initial data
        if (initialData != null) {
            List<WorkflowInstanceData> data = initialData.entrySet().stream()
                    .map(entry -> WorkflowInstanceData.builder()
                            .instance(finalInstance)
                            .fieldName(entry.getKey())
                            .fieldValue(entry.getValue())
                            .build())
                    .collect(Collectors.toList());
            instance.setData(data);
        }

        instance = instanceRepository.save(instance);

        // Audit log
        auditLogRepository.save(WorkflowAuditLog.builder()
                .instance(instance)
                .user(creator)
                .action("START_WORKFLOW")
                .toState(initialState)
                .createdAt(LocalDateTime.now())
                .build());

        // Create initial tasks
        createTasksForState(instance, initialState);

        return instance;
    }

    @Transactional
    public void executeTransition(WorkflowInstance instance, WorkflowTransition transition, User user, String comment) {
        WorkflowState fromState = instance.getCurrentState();
        WorkflowState toState = transition.getToState();

        // Validate transition
        if (!fromState.getId().equals(transition.getFromState().getId())) {
            throw new RuntimeException("Invalid transition for current state");
        }

        // Evaluate rules
        Map<String, Object> variables = getVariables(instance);
        if (!ruleEvaluator.evaluate(transition.getRuleExpression(), variables)) {
            throw new RuntimeException("Transition rules not met");
        }

        // Complete pending tasks for current state
        // In a real scenario, we would find tasks for THIS instance and THIS state
        
        // Update instance
        instance.setCurrentState(toState);
        if (toState.isFinal()) {
            instance.setStatus("COMPLETED");
        }
        instanceRepository.save(instance);

        // Audit log
        auditLogRepository.save(WorkflowAuditLog.builder()
                .instance(instance)
                .user(user)
                .action("EXECUTE_TRANSITION: " + transition.getName())
                .fromState(fromState)
                .toState(toState)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build());

        // Create tasks for new state
        if (!toState.isFinal()) {
            createTasksForState(instance, toState);
        }
    }

    private void createTasksForState(WorkflowInstance instance, WorkflowState state) {
        WorkflowTask task = WorkflowTask.builder()
                .instance(instance)
                .state(state)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        
        taskRepository.save(task);
    }

    private Map<String, Object> getVariables(WorkflowInstance instance) {
        Map<String, Object> variables = new HashMap<>();
        instance.getData().forEach(d -> {
            try {
                variables.put(d.getFieldName(), Double.parseDouble(d.getFieldValue()));
            } catch (Exception e) {
                variables.put(d.getFieldName(), d.getFieldValue());
            }
        });
        variables.put("creator", instance.getCreator().getUsername());
        variables.put("status", instance.getStatus());
        return variables;
    }
}
