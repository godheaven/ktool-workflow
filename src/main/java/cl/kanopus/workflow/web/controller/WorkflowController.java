package cl.kanopus.workflow.web.controller;

import cl.kanopus.workflow.data.entity.WorkflowDefinition;
import cl.kanopus.workflow.data.entity.WorkflowInstance;
import cl.kanopus.workflow.data.entity.WorkflowVersion;
import cl.kanopus.workflow.data.repository.WorkflowVersionRepository;
import cl.kanopus.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowVersionRepository versionRepository;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String complexity,
            @RequestParam(required = false) Boolean enabled,
            Model model) {
        
        org.springframework.data.domain.Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? 
                org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sort[0]));
        
        org.springframework.data.domain.Page<WorkflowDefinition> workflowPage = workflowService.findAllDefinitions(search, complexity, enabled, pageable);
        
        model.addAttribute("pageTitle", "Workflow Models");
        model.addAttribute("workflowPage", workflowPage);
        model.addAttribute("workflows", workflowPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sort[0]);
        model.addAttribute("sortDir", sort[1]);
        
        // Filter state
        model.addAttribute("filterSearch", search);
        model.addAttribute("filterComplexity", complexity);
        model.addAttribute("filterEnabled", enabled);
        
        return "workflow/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        WorkflowDefinition definition = workflowService.findAllDefinitions().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow();
        
        WorkflowVersion activeVersion = versionRepository.findByDefinitionIdAndActiveTrue(id)
                .orElse(null);

        model.addAttribute("pageTitle", definition.getName());
        model.addAttribute("workflow", definition);
        model.addAttribute("activeVersion", activeVersion);
        return "workflow/view";
    }
}
