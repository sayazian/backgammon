package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.service.OnlineUserRegistry;
import com.coderscampus.backgammon.service.UserService;
import com.coderscampus.backgammon.web.dto.OnlineUserView;
import com.coderscampus.backgammon.web.dto.PointView;
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
    private final OnlineUserRegistry onlineUserRegistry;

    public BackgammonController(UserService userService, OnlineUserRegistry onlineUserRegistry) {
        this.userService = userService;
        this.onlineUserRegistry = onlineUserRegistry;
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
        List<OnlineUserView> onlineUsers = onlineUserRegistry.listOthers(email);
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
        model.addAttribute("topPoints", sampleTopPoints());
        model.addAttribute("bottomPoints", sampleBottomPoints());
        model.addAttribute("dice", List.of(6, 6));
        model.addAttribute("players", List.of("Player 1", "Player 2"));
        model.addAttribute("currentPlayer", "Player 1");
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

    private List<PointView> sampleTopPoints() {
        return List.of(
                new PointView("PLAYER1", 0),
                new PointView("PLAYER2", 2),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("PLAYER1", 5),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("PLAYER2", 3),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("PLAYER1", 5)
        );
    }

    private List<PointView> sampleBottomPoints() {
        return List.of(
                new PointView("PLAYER2", 0),
                new PointView("PLAYER1", 2),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("PLAYER2", 5),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("PLAYER1", 3),
                new PointView("NONE", 0),
                new PointView("NONE", 0),
                new PointView("PLAYER2", 5)
        );
    }
}
