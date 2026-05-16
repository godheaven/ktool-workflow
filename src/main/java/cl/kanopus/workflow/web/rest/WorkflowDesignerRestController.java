package cl.kanopus.workflow.web.rest;

import cl.kanopus.workflow.service.WorkflowDesignerService;
import cl.kanopus.workflow.web.dto.WorkflowModelDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowDesignerRestController {

    private final WorkflowDesignerService designerService;

    @GetMapping("/{id}/model")
    public ResponseEntity<WorkflowModelDTO> getModel(@PathVariable Long id) {
        // Here id is the definition id, we get the draft version
        Long versionId = designerService.getDraft(id).getId();
        return ResponseEntity.ok(designerService.getModel(versionId));
    }

    @PostMapping("/{id}/model")
    public ResponseEntity<String> saveModel(@PathVariable Long id, @RequestBody WorkflowModelDTO model) {
        Long versionId = designerService.getDraft(id).getId();
        designerService.saveModel(versionId, model);
        return ResponseEntity.ok("Model saved successfully");
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<List<String>> validate(@PathVariable Long id) {
        return ResponseEntity.ok(designerService.validateWorkflow(designerService.getDraft(id)));
    }
}
