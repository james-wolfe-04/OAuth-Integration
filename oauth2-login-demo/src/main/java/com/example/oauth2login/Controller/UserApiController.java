package com.example.oauth2login.Controller;

import com.example.oauth2login.Entity.User;
import com.example.oauth2login.DTO.ProfileForm;
import com.example.oauth2login.Repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;

@RestController
@RequestMapping("/api")
public class UserApiController {

    private final UserRepository userRepository;

    public UserApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ GET current user (for frontend)
    @GetMapping("/user")
    public Object currentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null)
            return Map.of("authenticated", false);

        Object idAttr = principal.getAttribute("app_user_id");
        if (idAttr == null)
            return Map.of("error", "app_user_id not found", "authenticated", false);

        Long userId = ((Number) idAttr).longValue();

        return userRepository.findById(userId).map(user -> {
            Map<String, Object> result = new HashMap<>();
            result.put("authenticated", true);
            result.put("id", user.getId());
            result.put("email", user.getEmail());
            result.put("displayName", user.getDisplayName());
            result.put("avatarUrl", user.getAvatarUrl());
            result.put("bio", user.getBio());
            return result;
        }).orElse(Map.of("authenticated", false));
    }

    // ✅ POST update profile
    @PostMapping("/profile")
    public Object updateProfile(@AuthenticationPrincipal OAuth2User principal,
                                @RequestBody ProfileForm form) {
        if (principal == null)
            return Map.of("error", "Not authenticated");

        Object idAttr = principal.getAttribute("app_user_id");
        if (idAttr == null)
            return Map.of("error", "app_user_id not found");

        Long userId = ((Number) idAttr).longValue();

        return userRepository.findById(userId).map(user -> {
            if (form.getDisplayName() != null)
                user.setDisplayName(form.getDisplayName());
            user.setBio(form.getBio());
            userRepository.save(user);
            return Map.of("ok", true);
        }).orElse(Map.of("ok", false));
    }

    // ✅ GET logout — invalidates session and redirects to home
    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.logout();
        response.sendRedirect("/");
    }
}
