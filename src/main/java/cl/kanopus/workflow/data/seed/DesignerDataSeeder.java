package cl.kanopus.workflow.data.seed;

import cl.kanopus.workflow.data.entity.WorkflowDefinitionEntity;
import cl.kanopus.workflow.data.entity.WorkflowVersionEntity;
import cl.kanopus.workflow.service.WorkflowDesignerService;
import cl.kanopus.workflow.web.dto.EdgeDTO;
import cl.kanopus.workflow.web.dto.NodeDTO;
import cl.kanopus.workflow.web.dto.WorkflowModelDTO;
import cl.kanopus.workflow.model.enums.NodeType;
import cl.kanopus.workflow.model.enums.WorkflowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after InitialDataConfig if needed
public class DesignerDataSeeder implements CommandLineRunner {

    private final WorkflowDesignerService designerService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (designerService.getAllDefinitions().size() < 10) {
            log.info("Seeding advanced Global Hiring Process...");

            // 1. Advanced Hiring Process
            WorkflowDefinitionEntity hiringWf = designerService.createDefinition(
                "Global Hiring Process", 
                "End-to-end recruitment workflow with technical evaluation and management approval."
            );
            
            hiringWf.setOwnerName("Héctor González");
            hiringWf.setOwnerAvatarUrl("https://ui-avatars.com/api/?name=Hector+Gonzalez&background=6366f1&color=fff");
            hiringWf.setComplexity("High");
            hiringWf.setRating(5);
            hiringWf = designerService.saveDefinition(hiringWf);
            
            Long hiringVersionId = designerService.getDraft(hiringWf.getId()).getId();
            
            NodeDTO hStart = NodeDTO.builder().id("1").type(NodeType.START).label("Start").x(50.0).y(250.0).build();
            NodeDTO hForm = NodeDTO.builder().id("2").type(NodeType.FORM).label("Candidate Info")
                    .x(250.0).y(250.0)
                    .config(Map.of("fields", List.of(
                        Map.of("label", "Full Name", "type", "TEXT", "required", true),
                        Map.of("label", "CV Link", "type", "URL", "required", true),
                        Map.of("label", "Expected Salary", "type", "NUMBER", "required", true)
                    ))).build();
            NodeDTO hHrApproval = NodeDTO.builder().id("3").type(NodeType.APPROVAL).label("HR Screening")
                    .x(450.0).y(250.0)
                    .config(Map.of("approvalMode", "SINGLE_APPROVER")).build();
            NodeDTO hTechTask = NodeDTO.builder().id("4").type(NodeType.TASK).label("Technical Interview")
                    .x(650.0).y(250.0)
                    .config(Map.of("instructions", "Perform a 60-min coding interview.")).build();
            NodeDTO hRule = NodeDTO.builder().id("5").type(NodeType.RULE).label("Final Decision")
                    .x(850.0).y(250.0).build();
            
            NodeDTO hMgmtApproval = NodeDTO.builder().id("6").type(NodeType.APPROVAL).label("Management Approval")
                    .x(1050.0).y(100.0)
                    .config(Map.of("approvalMode", "SINGLE_APPROVER")).build();
            
            NodeDTO hRejectNotify = NodeDTO.builder().id("7").type(NodeType.NOTIFICATION).label("Send Rejection")
                    .x(1050.0).y(400.0)
                    .config(Map.of("template", "REJECTION_EMAIL")).build();
            
            NodeDTO hOfferAuto = NodeDTO.builder().id("8").type(NodeType.AUTOMATION).label("Generate Offer")
                    .x(1250.0).y(250.0)
                    .config(Map.of("script", "generate_offer_doc.js")).build();
            
            NodeDTO hEnd = NodeDTO.builder().id("9").type(NodeType.END).label("Process Complete").x(1450.0).y(250.0).build();
            
            List<EdgeDTO> hiringEdges = List.of(
                EdgeDTO.builder().source("1").target("2").sourceOutput("output_1").targetInput("input_1").build(),
                EdgeDTO.builder().source("2").target("3").sourceOutput("output_1").targetInput("input_1").build(),
                EdgeDTO.builder().source("3").target("4").sourceOutput("output_1").targetInput("input_1").build(),
                EdgeDTO.builder().source("4").target("5").sourceOutput("output_1").targetInput("input_1").build(),
                // Decision branch: Pass -> Management
                EdgeDTO.builder().source("5").target("6").sourceOutput("output_1").targetInput("input_1").label("Pass").build(),
                // Decision branch: Fail -> Reject
                EdgeDTO.builder().source("5").target("7").sourceOutput("output_2").targetInput("input_1").label("Fail").build(),
                // From Mgmt -> Offer
                EdgeDTO.builder().source("6").target("8").sourceOutput("output_1").targetInput("input_1").build(),
                // From Reject -> End
                EdgeDTO.builder().source("7").target("9").sourceOutput("output_1").targetInput("input_1").build(),
                // From Offer -> End
                EdgeDTO.builder().source("8").target("9").sourceOutput("output_1").targetInput("input_1").build()
            );
            
            designerService.saveModel(hiringVersionId, WorkflowModelDTO.builder()
                    .nodes(List.of(hStart, hForm, hHrApproval, hTechTask, hRule, hMgmtApproval, hRejectNotify, hOfferAuto, hEnd))
                    .edges(hiringEdges)
                    .build());

            log.info("Seeding 199 more mock workflows...");
            
            String[] names = {"Approval", "Process", "Request", "System", "Flow", "Engine", "Service", "Manager"};
            String[] prefixes = {"Global", "Enterprise", "Quick", "Smart", "Secure", "Dynamic", "Internal", "Public"};
            String[] complexities = {"Low", "Medium", "High", "Critical"};
            String[] owners = {"Héctor González", "Ana Silva", "John Doe", "Maria Garcia", "Chen Wei", "Sarah Smith"};
            
            for (int i = 2; i <= 200; i++) {
                String name = prefixes[i % prefixes.length] + " " + names[i % names.length] + " #" + i;
                String desc = "Automated mock workflow description for " + name;
                
                WorkflowDefinitionEntity wf = designerService.createDefinition(name, desc);
                wf.setOwnerName(owners[i % owners.length]);
                wf.setComplexity(complexities[i % complexities.length]);
                wf.setRating(1 + (i % 5));
                wf.setEnabled(i % 5 != 0); // Disable every 5th workflow
                
                designerService.saveDefinition(wf);
                
                // Add a very simple model to each so they aren't empty
                Long draftId = designerService.getDraft(wf.getId()).getId();
                NodeDTO start = NodeDTO.builder()
                        .id("1").type(NodeType.START).label("Start")
                        .x(100.0 + (i % 10) * 10).y(200.0 + (i % 5) * 10)
                        .config(Map.of())
                        .build();
                NodeDTO end = NodeDTO.builder()
                        .id("2").type(NodeType.END).label("End")
                        .x(500.0 + (i % 10) * 10).y(200.0 + (i % 5) * 10)
                        .config(Map.of())
                        .build();
                EdgeDTO edge = EdgeDTO.builder().source("1").target("2").sourceOutput("output_1").targetInput("input_1").build();
                
                designerService.saveModel(draftId, WorkflowModelDTO.builder()
                        .nodes(List.of(start, end))
                        .edges(List.of(edge))
                        .build());
                
                if (i % 20 == 0) log.info("Seeded {}/200 workflows...", i);
            }
            
            log.info("Designer seed completed.");
        }
    }
}
