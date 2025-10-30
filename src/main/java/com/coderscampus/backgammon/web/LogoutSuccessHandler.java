package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.service.OnlineUserRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private final OnlineUserRegistry onlineUserRegistry;

    public LogoutSuccessHandler(OnlineUserRegistry onlineUserRegistry) {
        this.onlineUserRegistry = onlineUserRegistry;
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        String email = extractEmail(authentication);
        onlineUserRegistry.markOffline(email);
        if (request.getSession(false) != null) {
            onlineUserRegistry.removeBySessionId(request.getSession(false).getId());
        }
        super.onLogoutSuccess(request, response, authentication);
    }

    private String extractEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
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
}
