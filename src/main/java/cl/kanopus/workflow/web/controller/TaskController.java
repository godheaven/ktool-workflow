package cl.kanopus.workflow.web.controller;

import cl.kanopus.workflow.data.entity.User;
import cl.kanopus.workflow.data.entity.WorkflowTask;
import cl.kanopus.workflow.data.repository.UserRepository;
import cl.kanopus.workflow.data.repository.WorkflowTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final WorkflowTaskRepository taskRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        
        // Simple logic for MVP: tasks where status is PENDING
        // In a real scenario, we would filter by assignee or role
        List<WorkflowTask> tasks = taskRepository.findAll().stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .toList();

        model.addAttribute("pageTitle", "My Tasks");
        model.addAttribute("tasks", tasks);
        return "tasks/list";
    }
}
