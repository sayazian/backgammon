package com.coderscampus.backgammon.web.filter;

import com.coderscampus.backgammon.service.OnlineUserRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ActivityTrackingFilter extends OncePerRequestFilter {

    private final OnlineUserRegistry onlineUserRegistry;

    public ActivityTrackingFilter(OnlineUserRegistry onlineUserRegistry) {
        this.onlineUserRegistry = onlineUserRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String email = extractEmail(authentication);
            if (email != null && !email.isBlank()) {
                HttpSession session = request.getSession(false);
                onlineUserRegistry.updateActivity(email, session);
            }
        }
        filterChain.doFilter(request, response);
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
}
