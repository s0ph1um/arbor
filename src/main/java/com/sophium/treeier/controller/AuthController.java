package com.sophium.treeier.controller;

import com.sophium.treeier.entity.User;
import com.sophium.treeier.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private final UserRepository userRepository;

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof OAuth2User oauth2User) {

            Map<String, Object> userInfo = new HashMap<>();
            String userEmail = oauth2User.getAttribute("email");
            Optional<User> user = userRepository.findByEmail(userEmail);

            user.ifPresent(value -> userInfo.put("id", value.getId()));
            userInfo.put("authenticated", true);
            userInfo.put("name", oauth2User.getAttribute("name"));
            userInfo.put("email", userEmail);
            userInfo.put("picture", oauth2User.getAttribute("picture"));

            return ResponseEntity.ok(userInfo);
        }
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, null);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
