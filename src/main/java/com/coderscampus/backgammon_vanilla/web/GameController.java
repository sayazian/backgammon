package com.coderscampus.backgammon_vanilla.web;

import com.coderscampus.backgammon_vanilla.domain.Game;
import com.coderscampus.backgammon_vanilla.domain.InviteMessage;
import com.coderscampus.backgammon_vanilla.domain.InviteResponseMessage;
import com.coderscampus.backgammon_vanilla.domain.User;
import com.coderscampus.backgammon_vanilla.dto.InviteDecision;
import com.coderscampus.backgammon_vanilla.service.InviteService;
import com.coderscampus.backgammon_vanilla.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class GameController {

    private final UserService userService;
    private final InviteService inviteService;

    public GameController(UserService userService, InviteService inviteService) {
        this.userService = userService;
        this.inviteService = inviteService;
    }

    @GetMapping({"/", "/login"})
    public String login(Authentication authentication) {
        if (isAnonymous(authentication)) {
            return "login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, ModelMap model) {
        if (isAnonymous(authentication)) {
            return "redirect:/";
        }
        String name = extractName(authentication);
        String email = extractEmail(authentication);
        User user = userService.findUser(name, email);
        userService.logUserIn(user);
        List<User> onlineUsers = userService.extractOnlineUsers();
        model.put("user", user);
        model.put("onlineUsers", onlineUsers);
        return "dashboard";
    }

    @GetMapping("/game/{gameId}")
    public String game(User user1, User user2, Game game) {
        return "redirect:/game/ + game.getGameId() ";
    }

    @PostMapping("/invite")
    public String invite(@RequestParam Long inviteeId, Authentication auth) {
        User inviter = userService.findUser(extractName(auth), extractEmail(auth));
        User invitee = userService.findById(inviteeId);
        if (invitee != null) {
            InviteMessage invite = new InviteMessage(
                    inviter.getUserId(),
                    inviteeId,
                    inviter.getName(),
                    invitee.getEmail(),
                    inviter.getEmail()
            );
            inviteService.sendInvite(invite);
        }
        return "redirect:/dashboard?invited";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, ModelMap model) {
        String name = extractName(authentication);
        String email = extractEmail(authentication);
        User user = userService.findUser(name, email);
        model.put("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String saveProfile(@ModelAttribute("user") User user) {
        userService.updateUser(user);
        return "profile";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response, @ModelAttribute("user") User user) {
//        userService.logUserOut(user);
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null) {
//            new SecurityContextLogoutHandler().logout(request, response, auth);
//        }
        return "redirect:/login?logout";
    }

    @PostMapping("/invite/respond")
    public ResponseEntity<Void> respond(@RequestBody InviteDecision decision, Authentication auth){
        User invitee = userService.findUser(extractName(auth), extractEmail(auth));
        User inviter = userService.findById(decision.inviterId());
        if (inviter != null) {
            InviteResponseMessage response = new InviteResponseMessage(
                    invitee.getUserId(),
                    invitee.getName(),
                    inviter.getUserId(),
                    decision.accepted(),
                    inviter.getEmail()
            );
            inviteService.sendInviteResponse(response);
        }
        return ResponseEntity.ok().build();
    }
    public static String extractName(Authentication authentication) {
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

    public static String extractEmail(Authentication authentication) {
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


    private User getUser(Authentication authentication) {
        User user = new User(extractName(authentication), extractEmail(authentication));
        user = userService.updateUser(user);
        return user;
    }

}
