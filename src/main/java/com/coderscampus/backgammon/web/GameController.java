package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.domain.BoardStatus;
import com.coderscampus.backgammon.domain.Game;
import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.service.AuthUserHelper;
import com.coderscampus.backgammon.service.GameService;
import com.coderscampus.backgammon.service.GameRuntimeService;
import com.coderscampus.backgammon.service.PresenceService;
import com.coderscampus.backgammon.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class GameController {

    private final UserService userService;
    private final AuthUserHelper authUserHelper;
    private final GameService gameService;
    private final PresenceService presenceService;
    private final GameRuntimeService gameRuntimeService;

    public GameController(UserService userService,
                          AuthUserHelper authUserHelper,
                          GameService gameService,
                          PresenceService presenceService,
                          GameRuntimeService gameRuntimeService) {
        this.userService = userService;
        this.authUserHelper = authUserHelper;
        this.gameService = gameService;
        this.presenceService = presenceService;
        this.gameRuntimeService = gameRuntimeService;
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
        String name = authUserHelper.extractName(authentication);
        String email = authUserHelper.extractEmail(authentication);
        User user = userService.findUser(name, email);
        Long reconnectableGameId = presenceService.findReconnectableGameId(user.getUserId());
        if (reconnectableGameId != null) {
            return "redirect:/games/" + reconnectableGameId;
        }
        List<User> onlineUsers = presenceService.getOnlineUsers();
        model.put("user", user);
        model.put("onlineUsers", onlineUsers);
        return "dashboard";
    }


    @GetMapping("/profile")
    public String profile(Authentication authentication, ModelMap model) {
        String name = authUserHelper.extractName(authentication);
        String email = authUserHelper.extractEmail(authentication);
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
        if (user != null && user.getUserId() != null) {
            presenceService.clearUserPresence(user.getUserId());
        }
        return "redirect:/login?logout";
    }

    @GetMapping("/games/{gameId}")
    public String game(Authentication authentication, ModelMap model, @PathVariable int gameId) {
        if (isAnonymous(authentication)) {
            return "redirect:/";
        }
        Game game = gameService.findById((long) gameId);
        User user = getUser(authentication);
        gameRuntimeService.registerGame(game);
        presenceService.registerGame(game);
        BoardStatus boardStatus = gameRuntimeService.getBoardState(game.getGameId());
        model.put("game", game);
        model.put("boardStatus", boardStatus);
        model.put("user", user);
        model.put("presenceStatus", presenceService.buildGamePresenceStatus(game, user.getUserId()));
        return "game";
    }

    @GetMapping("/game")
    public String game(Authentication authentication, ModelMap model) {
        if (isAnonymous(authentication)) {
            return "redirect:/";
        }
        User user = getUser(authentication);
        Game game = gameService.createTestGame(user);
        gameRuntimeService.registerGame(game);
        presenceService.registerGame(game);
        return "redirect:/games/" + game.getGameId();
    }

    private boolean isAnonymous(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }


    private User getUser(Authentication authentication) {
        User user = new User(authUserHelper.extractName(authentication), authUserHelper.extractEmail(authentication));
        user = userService.updateUser(user);
        return user;
    }

}
