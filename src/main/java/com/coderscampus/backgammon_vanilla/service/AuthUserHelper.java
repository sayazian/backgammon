package com.coderscampus.backgammon_vanilla.service;

import com.coderscampus.backgammon_vanilla.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class AuthUserHelper {
    public String extractName(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            String name = oauth2User.getAttribute("name");
            if (name == null) {
                name = oauth2User.getAttribute("given_name");
            }
            if (name == null) {
                name = oauth2User.getAttribute("preferred_username");
            }
            if (name == null) {
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

    public String extractEmail(Authentication authentication) {
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
    public User resolveUser(Authentication authentication, UserService userService) {
        String name = extractName(authentication);
        String email = extractEmail(authentication);
        return userService.findUser(name, email);
    }

}
