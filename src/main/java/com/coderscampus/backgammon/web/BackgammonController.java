package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.domain.Game;
import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.service.OnlineUserRegistry;
import com.coderscampus.backgammon.service.UserService;
import com.coderscampus.backgammon.web.dto.OnlineUserView;
import com.coderscampus.backgammon.web.dto.PointView;
import com.coderscampus.backgammon.repository.GameRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BackgammonController {

    private final UserService userService;
    private final OnlineUserRegistry onlineUserRegistry;
    private final GameRepository gameRepository;

    public BackgammonController(UserService userService,
                                OnlineUserRegistry onlineUserRegistry,
                                GameRepository gameRepository) {
        this.userService = userService;
        this.onlineUserRegistry = onlineUserRegistry;
        this.gameRepository = gameRepository;
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
    public String game(@RequestParam(name = "gameId", required = false) Long gameId,
                       Authentication authentication,
                       Model model) {
        if (isAnonymous(authentication)) {
            return "redirect:/";
        }
        String viewerEmail = extractEmail(authentication);
        String fallbackName = extractName(authentication);
        String viewerDisplayName = userService.resolveDisplayName(viewerEmail, fallbackName);
        if (viewerDisplayName == null || viewerDisplayName.isBlank()) {
            viewerDisplayName = fallbackName != null && !fallbackName.isBlank() ? fallbackName : "You";
        }

        User viewerUser = null;
        User opponentUser = null;
        String opponentDisplayName = "Opponent";
        if (gameId != null) {
            Optional<Game> maybeGame = gameRepository.findById(gameId);
            if (maybeGame.isPresent()) {
                Game game = maybeGame.get();
                viewerUser = resolveViewer(game, viewerEmail);
                opponentUser = resolveOpponent(game, viewerUser, viewerEmail);
                if (viewerUser != null) {
                    viewerDisplayName = userService.resolveDisplayName(viewerUser.getEmail(), viewerUser.getUsername());
                }
                if (opponentUser != null) {
                    opponentDisplayName = userService.resolveDisplayName(opponentUser.getEmail(), opponentUser.getUsername());
                }
            }
        }
        if (opponentDisplayName == null || opponentDisplayName.isBlank()) {
            opponentDisplayName = "Opponent";
        }
        List<String> players = List.of(viewerDisplayName, opponentDisplayName);

        String viewerStateKey = resolveStateKey(viewerUser, viewerDisplayName, viewerEmail);
        String opponentEmail = opponentUser != null ? opponentUser.getEmail() : null;
        String opponentStateKey = resolveStateKey(opponentUser, opponentDisplayName, opponentEmail);

        model.addAttribute("userName", viewerDisplayName);
        model.addAttribute("viewerName", viewerDisplayName);
        model.addAttribute("opponentName", opponentDisplayName);
        model.addAttribute("viewerStateKey", viewerStateKey);
        model.addAttribute("opponentStateKey", opponentStateKey);
        model.addAttribute("topPoints", sampleTopPoints());
        model.addAttribute("bottomPoints", sampleBottomPoints());
        model.addAttribute("dice", List.of(0, 0));
        model.addAttribute("players", players);
        model.addAttribute("currentPlayer", null);
        model.addAttribute("previewMode", false);
        model.addAttribute("gamePreviewKey", determineGameKey(gameId, viewerUser, opponentUser, viewerEmail, opponentDisplayName));
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

    private User resolveViewer(Game game, String viewerEmail) {
        if (viewerEmail == null || viewerEmail.isBlank()) {
            return null;
        }
        if (game.getUser1() != null && viewerEmail.equalsIgnoreCase(game.getUser1().getEmail())) {
            return game.getUser1();
        }
        if (game.getUser2() != null && viewerEmail.equalsIgnoreCase(game.getUser2().getEmail())) {
            return game.getUser2();
        }
        return null;
    }

    private User resolveOpponent(Game game, User viewerUser, String viewerEmail) {
        if (viewerUser != null) {
            return viewerUser == game.getUser1() ? game.getUser2() : game.getUser1();
        }
        if (viewerEmail != null && !viewerEmail.isBlank()) {
            if (game.getUser1() != null && !viewerEmail.equalsIgnoreCase(game.getUser1().getEmail())) {
                return game.getUser1();
            }
            if (game.getUser2() != null && !viewerEmail.equalsIgnoreCase(game.getUser2().getEmail())) {
                return game.getUser2();
            }
        }
        return null;
    }

    private String resolveStateKey(User user, String displayName, String fallbackEmail) {
        if (user != null) {
            if (user.getUserId() != null) {
                return "user#" + user.getUserId();
            }
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                return "email#" + user.getEmail().toLowerCase(Locale.ENGLISH);
            }
        }
        if (fallbackEmail != null && !fallbackEmail.isBlank()) {
            return "email#" + fallbackEmail.toLowerCase(Locale.ENGLISH);
        }
        if (displayName != null && !displayName.isBlank()) {
            return "name#" + displayName.trim().toLowerCase(Locale.ENGLISH);
        }
        return null;
    }

    private String determineGameKey(Long gameId,
                                    User viewerUser,
                                    User opponentUser,
                                    String viewerEmail,
                                    String opponentDisplayName) {
        if (gameId == null) {
            return null;
        }
        String viewerKey = (viewerUser != null && viewerUser.getEmail() != null)
                ? viewerUser.getEmail().toLowerCase()
                : viewerEmail != null ? viewerEmail.toLowerCase() : null;
        String opponentKey = (opponentUser != null && opponentUser.getEmail() != null)
                ? opponentUser.getEmail().toLowerCase()
                : opponentDisplayName != null ? opponentDisplayName.toLowerCase() : null;

        if (viewerKey == null && opponentKey == null) {
            return "game-" + gameId;
        }

        if (viewerKey != null && opponentKey != null) {
            if (viewerKey.compareTo(opponentKey) <= 0) {
                return "game-" + gameId + ":" + viewerKey + "|" + opponentKey;
            } else {
                return "game-" + gameId + ":" + opponentKey + "|" + viewerKey;
            }
        }

        String singleKey = viewerKey != null ? viewerKey : opponentKey;
        return "game-" + gameId + ":" + singleKey;
    }
}
