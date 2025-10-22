package com.example.oauth2login.Controller;

import com.example.oauth2login.Entity.User;
import com.example.oauth2login.Repository.UserRepository;
import com.example.oauth2login.DTO.ProfileForm;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OAuth2User principal) {
        model.addAttribute("isAuthenticated", principal != null);
        if (principal != null) model.addAttribute("name", principal.getAttribute("name"));
        return "home";
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        Long userId = ((Number) principal.getAttribute("app_user_id")).longValue();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/";
        model.addAttribute("user", user);
        model.addAttribute("profileForm", new ProfileForm());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute ProfileForm form,
                                @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        Long userId = ((Number) principal.getAttribute("app_user_id")).longValue();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/";

        if (form.getDisplayName() != null) user.setDisplayName((String) form.getDisplayName());
        user.setBio(form.getBio());
        userRepository.save(user);
        return "redirect:/profile?success";
    }
}