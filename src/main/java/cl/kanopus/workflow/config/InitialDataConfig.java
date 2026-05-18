package cl.kanopus.workflow.config;

import cl.kanopus.workflow.data.entity.*;
import cl.kanopus.workflow.data.repository.*;
import cl.kanopus.workflow.model.form.FieldType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class InitialDataConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            WorkflowDefinitionRepository definitionRepository,
            WorkflowVersionRepository versionRepository,
            WorkflowStateRepository stateRepository,
            WorkflowTransitionRepository transitionRepository,
            WorkflowFormRepository formRepository,
            WorkflowFormFieldRepository fieldRepository) {
        
        return args -> {
            // Update existing users if they don't have avatarUrl or fullName
            userRepository.findByUsername("admin").ifPresent(admin -> {
                if (admin.getAvatarUrl() == null || admin.getFullName() == null) {
                    admin.setFullName("System Administrator");
                    admin.setEmail("admin@kanopus.cl");
                    admin.setAvatarUrl("https://ui-avatars.com/api/?name=System+Administrator&background=random");
                    userRepository.save(admin);
                }
            });
            userRepository.findByUsername("user").ifPresent(user -> {
                if (user.getAvatarUrl() == null || user.getFullName() == null) {
                    user.setFullName("Demo User");
                    user.setEmail("user@kanopus.cl");
                    user.setAvatarUrl("https://ui-avatars.com/api/?name=Demo+User&background=random");
                    userRepository.save(user);
                }
            });

            if (roleRepository.count() > 0) return;

            // 1. Roles
            Role adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
            Role userRole = roleRepository.save(Role.builder().name("ROLE_USER").build());

            // 2. Users
            User admin = userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .fullName("System Administrator")
                    .email("admin@kanopus.cl")
                    .avatarUrl("https://ui-avatars.com/api/?name=System+Administrator&background=random")
                    .active(true)
                    .roles(Set.of(adminRole))
                    .build());

            User user = userRepository.save(User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user"))
                    .fullName("Demo User")
                    .email("user@kanopus.cl")
                    .avatarUrl("https://ui-avatars.com/api/?name=Demo+User&background=random")
                    .active(true)
                    .roles(Set.of(userRole))
                    .build());

            // 3. Demo Workflow: Expense Report
            WorkflowDefinition expenseWf = definitionRepository.save(WorkflowDefinition.builder()
                    .name("Expense Report")
                    .description("Standard flow for reporting and approving business expenses.")
                    .active(true)
                    .complexity("Low")
                    .rating(4)
                    .ownerName("Ioni owcher")
                    .lastUpdated("05/10/2026")
                    .build());

            WorkflowDefinition purchaseWf = definitionRepository.save(WorkflowDefinition.builder()
                    .name("Purchase Request")
                    .description("Formal process for high-value equipment purchases.")
                    .active(true)
                    .complexity("Medium")
                    .rating(3)
                    .ownerName("Pablo Diaz")
                    .lastUpdated("05/12/2026")
                    .build());

            WorkflowDefinition hireWf = definitionRepository.save(WorkflowDefinition.builder()
                    .name("Hiring Process")
                    .description("End-to-end recruitment and onboarding workflow.")
                    .active(true)
                    .complexity("High")
                    .rating(5)
                    .ownerName("Ioni owcher")
                    .lastUpdated("05/14/2026")
                    .build());

            // 3.1. Seed 200 mock workflows
            if (definitionRepository.count() < 10) {
                java.util.Random random = new java.util.Random();
                String[] complexities = {"Low", "Medium", "High"};
                String[] owners = {"Ioni Owcher", "Ariel McArdle", "Dante Marone", "Selina Kyle", "Bruce Wayne"};
                java.util.List<WorkflowDefinition> mockWorkflows = new java.util.ArrayList<>();
                
                for (int i = 1; i <= 200; i++) {
                    mockWorkflows.add(WorkflowDefinition.builder()
                            .name("Workflow Process " + i)
                            .description("Automated business logic for process number " + i)
                            .complexity(complexities[random.nextInt(complexities.length)])
                            .rating(1 + random.nextInt(5))
                            .ownerName(owners[random.nextInt(owners.length)])
                            .ownerAvatarUrl("https://ui-avatars.com/api/?name=" + owners[random.nextInt(owners.length)].replace(" ", "+") + "&background=random")
                            .lastUpdated("2026-05-" + String.format("%02d", 1 + random.nextInt(14)))
                            .versionLabel("v " + (1 + random.nextInt(3)) + "." + random.nextInt(10))
                            .enabled(random.nextBoolean())
                            .active(true)
                            .build());
                }
                definitionRepository.saveAll(mockWorkflows);
            }

            WorkflowVersion v1 = versionRepository.save(WorkflowVersion.builder()
                    .definition(expenseWf)
                    .versionNumber(1)
                    .active(true)
                    .build());

            // 4. States
            WorkflowState draft = stateRepository.save(WorkflowState.builder()
                    .version(v1).name("Draft").code("DRAFT").isInitial(true).build());
            
            WorkflowState review = stateRepository.save(WorkflowState.builder()
                    .version(v1).name("Under Review").code("REVIEW").build());
            
            WorkflowState approved = stateRepository.save(WorkflowState.builder()
                    .version(v1).name("Approved").code("APPROVED").isFinal(true).build());
            
            WorkflowState rejected = stateRepository.save(WorkflowState.builder()
                    .version(v1).name("Rejected").code("REJECTED").isFinal(true).build());

            // 5. Transitions
            transitionRepository.save(WorkflowTransition.builder()
                    .version(v1).name("Submit for Review").fromState(draft).toState(review).build());
            
            transitionRepository.save(WorkflowTransition.builder()
                    .version(v1).name("Approve").fromState(review).toState(approved)
                    .ruleExpression("#amount < 5000").build());
            
            transitionRepository.save(WorkflowTransition.builder()
                    .version(v1).name("Reject").fromState(review).toState(rejected).build());

            // 6. Form
            WorkflowForm form = formRepository.save(WorkflowForm.builder()
                    .version(v1).name("Expense Details").description("Please fill in the expense information.").build());

            fieldRepository.save(WorkflowFormField.builder()
                    .form(form).key("description").label("Description").fieldType(FieldType.TEXT).required(true).orderIndex(0).build());
            
            fieldRepository.save(WorkflowFormField.builder()
                    .form(form).key("amount").label("Amount").fieldType(FieldType.NUMBER).required(true).orderIndex(1).build());
            
            fieldRepository.save(WorkflowFormField.builder()
                    .form(form).key("date").label("Expense Date").fieldType(FieldType.DATE).required(true).orderIndex(2).build());
        };
    }
}
