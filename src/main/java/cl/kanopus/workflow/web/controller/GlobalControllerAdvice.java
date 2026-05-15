package cl.kanopus.workflow.web.controller;

import cl.kanopus.workflow.data.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @Value("${project.version:0.0.1}")
    private String appVersion;

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {
        model.addAttribute("appVersion", appVersion);
        if (authentication != null && authentication.isAuthenticated()) {
            userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
                model.addAttribute("displayName", user.getFullName());
                model.addAttribute("userEmail", user.getEmail());
                model.addAttribute("avatarUrl", user.getAvatarUrl());
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    String roleName = user.getRoles().iterator().next().getName().replace("ROLE_", "");
                    model.addAttribute("userRole", roleName);
                }
            });
        }
    }
}
