package cl.kanopus.workflow.data.seed;

import cl.kanopus.workflow.data.entity.WorkflowDefinition;
import cl.kanopus.workflow.data.entity.WorkflowVersion;
import cl.kanopus.workflow.data.repository.WorkflowDefinitionRepository;
import cl.kanopus.workflow.data.repository.WorkflowVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowVersionRepository versionRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (definitionRepository.count() < 10) {
            log.info("Seeding 200 workflows for testing...");
            
            String[] names = {
                "Approval Process", "Expense Report", "New Hire Onboarding", 
                "Vacation Request", "Document Review", "IT Support Ticket", 
                "Purchase Order", "Inventory Update", "Customer Feedback", 
                "Marketing Campaign", "Security Audit", "Project Kickoff", 
                "Budget Approval", "Legal Review", "Payroll Processing",
                "Quality Assurance", "Customer Support", "Maintenance Request",
                "Training Session", "Event Planning"
            };
            
            String[] complexities = {"Low", "Medium", "High", "Critical"};
            String[] owners = {
                "Ioni Owcher", "John Doe", "Jane Smith", "Alice Johnson", 
                "Bob Brown", "Charlie Davis", "Emma Wilson", "Liam Miller",
                "Sophia Garcia", "Mason Martinez"
            };

            for (int i = 1; i <= 200; i++) {
                String baseName = names[random.nextInt(names.length)];
                String owner = owners[random.nextInt(owners.length)];
                
                WorkflowDefinition definition = WorkflowDefinition.builder()
                        .name(baseName + " " + i)
                        .description("Automated description for " + baseName + " number " + i + ". This workflow is designed to streamline the " + baseName.toLowerCase() + " process within the organization.")
                        .active(true)
                        .complexity(complexities[random.nextInt(complexities.length)])
                        .rating(random.nextInt(5) + 1)
                        .ownerName(owner)
                        .ownerAvatarUrl("https://ui-avatars.com/api/?name=" + owner.replace(" ", "+") + "&background=random")
                        .lastUpdated("05/" + String.format("%02d", (random.nextInt(14) + 1)) + "/2026")
                        .versionLabel("v " + (random.nextInt(5) + 1) + "." + random.nextInt(10))
                        .enabled(random.nextDouble() > 0.1) // 90% enabled
                        .build();
                
                definition = definitionRepository.save(definition);
                
                // Create an active version for each
                WorkflowVersion version = WorkflowVersion.builder()
                        .definition(definition)
                        .versionNumber(1)
                        .active(true)
                        .build();
                versionRepository.save(version);
            }
            log.info("Seeded 200 workflows successfully.");
        } else {
            log.info("Database already contains workflows. Skipping seeding.");
        }
    }
}
