package cl.kanopus.workflow.web.controller;

import cl.kanopus.workflow.data.entity.*;
import cl.kanopus.workflow.data.repository.UserRepository;
import cl.kanopus.workflow.data.repository.WorkflowAuditLogRepository;
import cl.kanopus.workflow.data.repository.WorkflowTransitionRepository;
import cl.kanopus.workflow.engine.execution.WorkflowEngine;
import cl.kanopus.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final WorkflowService workflowService;
    private final WorkflowEngine workflowEngine;
    private final UserRepository userRepository;
    private final WorkflowTransitionRepository transitionRepository;
    private final WorkflowAuditLogRepository auditLogRepository;

    @PostMapping("/start")
    public String startInstance(@RequestParam Long definitionId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        WorkflowInstance instance = workflowService.createInstance(definitionId, user, new HashMap<>());
        return "redirect:/instances/" + instance.getId();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        WorkflowInstance instance = workflowService.findInstanceById(id);
        List<WorkflowTransition> transitions = transitionRepository.findByFromStateId(instance.getCurrentState().getId());
        List<WorkflowAuditLog> auditLogs = auditLogRepository.findByInstanceIdOrderByCreatedAtDesc(id);

        model.addAttribute("pageTitle", "Instance #" + id);
        model.addAttribute("instance", instance);
        model.addAttribute("transitions", transitions);
        model.addAttribute("auditLogs", auditLogs);
        
        // Data map for easier rendering
        Map<String, String> dataMap = new HashMap<>();
        instance.getData().forEach(d -> dataMap.put(d.getFieldName(), d.getFieldValue()));
        model.addAttribute("dataMap", dataMap);

        return "instance/view";
    }

    @PostMapping("/{id}/transition")
    public String executeTransition(@PathVariable Long id, 
                                   @RequestParam Long transitionId, 
                                   @RequestParam(required = false) String comment,
                                   @RequestParam Map<String, String> allParams,
                                   Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        WorkflowInstance instance = workflowService.findInstanceById(id);
        WorkflowTransition transition = transitionRepository.findById(transitionId).orElseThrow();

        // Update instance data with form submission
        // We filter out transitionId and comment
        Map<String, String> formData = new HashMap<>(allParams);
        formData.remove("transitionId");
        formData.remove("comment");
        formData.remove("_csrf");

        // Logic to update instance data (simplified for MVP)
        // In a real engine, the engine handles data updates before/during transitions
        
        workflowEngine.executeTransition(instance, transition, user, comment);

        return "redirect:/instances/" + id;
    }
}
