package cl.kanopus.workflow.web.controller;

import cl.kanopus.workflow.data.entity.User;
import cl.kanopus.workflow.data.repository.UserRepository;
import cl.kanopus.workflow.data.repository.WorkflowDefinitionRepository;
import cl.kanopus.workflow.data.repository.WorkflowInstanceRepository;
import cl.kanopus.workflow.data.repository.WorkflowTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final WorkflowTaskRepository taskRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String dashboard(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("pendingTasksCount", taskRepository.findByAssigneeIdAndStatus(user.getId(), "PENDING").size());
        model.addAttribute("activeInstancesCount", instanceRepository.findAll().stream().filter(i -> "ACTIVE".equals(i.getStatus())).count());
        model.addAttribute("workflowModelsCount", definitionRepository.count());
        model.addAttribute("recentTasks", taskRepository.findByAssigneeIdAndStatus(user.getId(), "PENDING"));

        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
