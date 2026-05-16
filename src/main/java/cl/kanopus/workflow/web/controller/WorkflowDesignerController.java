package cl.kanopus.workflow.web.controller;

import cl.kanopus.workflow.data.entity.WorkflowDefinitionEntity;
import cl.kanopus.workflow.data.entity.WorkflowVersionEntity;
import cl.kanopus.workflow.service.WorkflowDesignerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowDesignerController {

    private final WorkflowDesignerService designerService;

    @GetMapping("/new")
    public String createNew(Model model) {
        WorkflowDefinitionEntity definition = designerService.createDefinition("New Workflow", "Description here...");
        return "redirect:/workflows/" + definition.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editWorkflow(@PathVariable Long id, Model model) {
        WorkflowDefinitionEntity definition = designerService.getDefinition(id);
        WorkflowVersionEntity draft = designerService.getDraft(id);
        
        model.addAttribute("workflow", definition);
        model.addAttribute("version", draft);
        model.addAttribute("pageTitle", "Editing: " + definition.getName());
        return "workflow/designer";
    }

    @PostMapping("/{id}/save")
    public String saveMetadata(@PathVariable Long id, @RequestParam String name, @RequestParam String description) {
        WorkflowDefinitionEntity definition = designerService.getDefinition(id);
        definition.setName(name);
        definition.setDescription(description);
        // Persist via save if needed or just let the service handle it
        return "redirect:/workflows/" + id + "/edit";
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id) {
        designerService.publishWorkflow(id);
        return "redirect:/workflows/" + id + "/edit";
    }

    @PostMapping("/{id}/duplicate")
    public String duplicate(@PathVariable Long id) {
        WorkflowDefinitionEntity copy = designerService.duplicateWorkflow(id);
        return "redirect:/workflows/" + copy.getId() + "/edit";
    }

    @PostMapping("/{id}/deprecate")
    public String deprecate(@PathVariable Long id) {
        designerService.deprecateWorkflow(id);
        return "redirect:/workflows";
    }
}
