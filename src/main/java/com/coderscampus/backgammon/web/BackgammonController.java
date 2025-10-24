package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.service.UserService;
import com.coderscampus.backgammon.web.dto.OnlineUserView;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BackgammonController {

    private final UserService userService;

    public BackgammonController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String login(Authentication authentication) {
        if (isAnonymous(authentication)) {
            return "login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (isAnonymous(authentication)) {
            return "redirect:/";
        }
        String email = extractEmail(authentication);
        List<OnlineUserView> onlineUsers = email == null
                ? Collections.emptyList()
                : userService.getUsersForDashboard(email);
        String displayName = extractName(authentication);
        Long currentUserId = email == null
                ? null
                : userService.findByEmail(email)
                        .map(user -> user.getUserId())
                        .orElse(null);
        model.addAttribute("userName", displayName);
        model.addAttribute("currentUserEmail", email);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("onlineUsers", onlineUsers);
        model.addAttribute("outgoingInvitation", null);
        model.addAttribute("incomingInvitation", null);
        return "dashboard";
    }

    @GetMapping("/game")
    public String game(Authentication authentication, Model model) {
        if (isAnonymous(authentication)) {
            return "redirect:/";
        }
        model.addAttribute("userName", extractName(authentication));
        return "game";
    }

    @GetMapping("/secured")
    @ResponseBody
    public String secured() {
        return "Hello, Secured!";
    }

    private String extractName(Authentication authentication) {
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

    private boolean isAnonymous(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }
}
