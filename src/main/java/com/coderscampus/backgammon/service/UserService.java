package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void recordLogin(Authentication authentication) {
        if (authentication == null) {
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            handleOAuth2User(oauth2User);
        } else if (principal instanceof UserDetails userDetails) {
            handleLocalUser(userDetails);
        }
    }

    private void handleOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            log.warn("OAuth2 user did not provide an email attribute; skipping persistence");
            return;
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(resolveDisplayName(oauth2User));
        user.setJoinDate(LocalDateTime.now());
        userRepository.save(user);
    }

    private void handleLocalUser(UserDetails userDetails) {
        String username = userDetails.getUsername();
        Optional<User> existing = userRepository.findByEmail(username);
        if (existing.isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(username);
        user.setJoinDate(LocalDateTime.now());
        userRepository.save(user);
    }

    private String resolveDisplayName(OAuth2User oauth2User) {
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
        return name;
    }

    @Transactional(readOnly = true)
    public String resolveDisplayName(String email, String fallback) {
        if (email == null || email.isBlank()) {
            return fallback;
        }
        return userRepository.findByEmail(email)
                .map(User::getUsername)
                .filter(username -> username != null && !username.isBlank())
                .orElseGet(() -> fallback != null && !fallback.isBlank() ? fallback : email);
    }
}
