package cl.kanopus.workflow.web.controller;

import cl.kanopus.workflow.data.entity.User;
import cl.kanopus.workflow.data.entity.WorkflowInstance;
import cl.kanopus.workflow.data.repository.UserRepository;
import cl.kanopus.workflow.data.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/instances")
@RequiredArgsConstructor
public class InstanceHistoryController {

    private final WorkflowInstanceRepository instanceRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<WorkflowInstance> instances = instanceRepository.findAll();

        model.addAttribute("pageTitle", "Workflow History");
        model.addAttribute("instances", instances);
        return "instance/list";
    }
}
