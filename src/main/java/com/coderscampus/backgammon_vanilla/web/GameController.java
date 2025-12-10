package com.coderscampus.backgammon_vanilla.web;

import com.coderscampus.backgammon_vanilla.domain.Game;
import com.coderscampus.backgammon_vanilla.domain.Invite;
import com.coderscampus.backgammon_vanilla.domain.User;
import com.coderscampus.backgammon_vanilla.service.GameService;
import com.coderscampus.backgammon_vanilla.service.InviteService;
import com.coderscampus.backgammon_vanilla.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class GameController {

    private final UserService userService;
    private final InviteService inviteService;
    private final GameService gameService;

    public GameController(UserService userService, InviteService inviteService, GameService gameService) {
        this.userService = userService;
        this.inviteService = inviteService;
        this.gameService = gameService;
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

    @PostMapping("/invite")
    public String invite(Long inviteeId, Authentication authentication, RedirectAttributes redirectAttributes) {
        User inviter = userService.findUser(extractName(authentication), extractEmail(authentication));
        User invitee = userService.findById(inviteeId);
        Invite invite = inviteService.createInvite(inviter.getUserId(), invitee.getUserId());
        redirectAttributes.addFlashAttribute("inviteeName", invitee.getName());
        return "redirect:/dashboard";
    }

    @GetMapping("/invited/{userId}/getInvites")
    @ResponseBody
    public List<Invite> getInvites(@PathVariable("userId") Long userId) {
        return inviteService.getInvites(userId);
    }

    @PostMapping ("/createAGame")
    @ResponseBody
    public Map<String, Object> createAGame (@RequestBody Map<String, String> body) {
        Game game = gameService.createGame(Long.valueOf(body.get("inviterId")), Long.valueOf(body.get("inviteeId")));
        String url = "/games/" + game.getGameId();
        return Map.of("gameId", game.getGameId(), "url",url);
    }

    @GetMapping("/games/{gameId}")
    public String showGame(@PathVariable("gameId") Long gameId, ModelMap modelMap) {
        Game game = gameService.findById(gameId);
        modelMap.put("game", game);
        return("/game");
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

    private User getUser(Authentication authentication) {
        User user = new User(extractName(authentication), extractEmail(authentication));
        user = userService.updateUser(user);
        return user;
    }

}
