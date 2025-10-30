package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.service.OnlineUserRegistry;
import com.coderscampus.backgammon.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final OnlineUserRegistry onlineUserRegistry;

    public LoginSuccessHandler(UserService userService, OnlineUserRegistry onlineUserRegistry) {
        this.userService = userService;
        this.onlineUserRegistry = onlineUserRegistry;
        setDefaultTargetUrl("/dashboard");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval((int) Duration.ofMinutes(1).getSeconds());
        userService.recordLogin(authentication);
        registerOnline(authentication, session);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void registerOnline(Authentication authentication, HttpSession session) {
        if (authentication == null) {
            return;
        }
        String email = extractEmail(authentication);
        if (email == null || email.isBlank()) {
            return;
        }
        if (session != null) {
            session.setAttribute("CURRENT_USER_EMAIL", email);
        }
        Optional<User> userOpt = userService.findByEmail(email);
        Long userId = userOpt.map(User::getUserId).orElse(null);
        String displayName = userService.resolveDisplayName(email, extractName(authentication));
        onlineUserRegistry.markOnline(userId, displayName, email, session);
    }

    private String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }

    private String extractName(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String name = oauth2User.getAttribute("name");
            if (name == null || name.isBlank()) {
                name = oauth2User.getAttribute("given_name");
            }
            if (name == null || name.isBlank()) {
                name = oauth2User.getAttribute("preferred_username");
            }
            if (name == null || name.isBlank()) {
                name = oauth2User.getAttribute("email");
            }
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }
}
