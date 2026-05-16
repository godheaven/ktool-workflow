package cl.kanopus.workflow.web.controller;

import cl.kanopus.workflow.data.entity.WorkflowDefinitionEntity;
import cl.kanopus.workflow.data.entity.WorkflowVersionEntity;
import cl.kanopus.workflow.data.repository.WorkflowDefinitionEntityRepository;
import cl.kanopus.workflow.data.repository.WorkflowVersionEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowDefinitionEntityRepository definitionRepository;
    private final WorkflowVersionEntityRepository versionRepository;

    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String complexity,
            @RequestParam(required = false) Boolean enabled,
            Pageable pageable,
            Model model) {
        
        // Treat empty strings as null for filters
        String finalSearch = (search != null && search.isEmpty()) ? null : search;
        String finalComplexity = (complexity != null && complexity.isEmpty()) ? null : complexity;
        
        Page<WorkflowDefinitionEntity> workflowPage = definitionRepository.findByFilters(finalSearch, finalComplexity, enabled, pageable);
        
        model.addAttribute("pageTitle", "Workflows");
        model.addAttribute("workflowPage", workflowPage);
        model.addAttribute("workflows", workflowPage.getContent());
        
        // Pagination & Sort state for the UI
        model.addAttribute("currentPage", pageable.getPageNumber());
        model.addAttribute("pageSize", pageable.getPageSize());
        
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            model.addAttribute("sortField", order.getProperty());
            model.addAttribute("sortDir", order.getDirection().name().toLowerCase());
        } else {
            model.addAttribute("sortField", "id");
            model.addAttribute("sortDir", "asc");
        }
        
        // Filter state
        model.addAttribute("filterSearch", search);
        model.addAttribute("filterComplexity", complexity);
        model.addAttribute("filterEnabled", enabled);
        
        return "workflow/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        WorkflowDefinitionEntity definition = definitionRepository.findById(id)
                .orElseThrow();
        
        WorkflowVersionEntity activeVersion = versionRepository.findByDefinitionIdAndIsDraftFalse(id)
                .stream().findFirst().orElse(null);

        model.addAttribute("pageTitle", definition.getName());
        model.addAttribute("workflow", definition);
        model.addAttribute("activeVersion", activeVersion);
        return "workflow/view";
    }
}
