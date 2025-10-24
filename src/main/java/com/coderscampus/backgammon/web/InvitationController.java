package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.domain.Game;
import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.repository.GameRepository;
import com.coderscampus.backgammon.service.UserService;
import com.coderscampus.backgammon.web.dto.InviteMessage;
import com.coderscampus.backgammon.web.dto.InviteMessage.InviteStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/invitations")
public class InvitationController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final GameRepository gameRepository;

    public InvitationController(SimpMessagingTemplate messagingTemplate,
                                UserService userService,
                                GameRepository gameRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.gameRepository = gameRepository;
    }

    @PostMapping("/accept")
    public String acceptInvite(@RequestParam("inviter") String inviterEmail,
                               Authentication authentication) {
        String inviteeEmail = extractEmail(authentication);
        Optional<User> inviterOpt = userService.findByEmail(inviterEmail);
        Optional<User> inviteeOpt = userService.findByEmail(inviteeEmail);
        Long gameId = null;
        Long inviterUserId = inviterOpt.map(User::getUserId).orElse(null);
        Long inviteeUserId = inviteeOpt.map(User::getUserId).orElse(null);

        if (inviterOpt.isPresent() && inviteeOpt.isPresent()) {
            Game game = new Game();
            game.setUser1(inviterOpt.get());
            game.setUser2(inviteeOpt.get());
            game.setStarter(inviterOpt.get());
            game = gameRepository.save(game);
            gameId = game.getGameId();
        }

        String inviteeName = userService.resolveDisplayName(inviteeEmail, extractName(authentication));
        String inviterName = userService.resolveDisplayName(inviterEmail, inviterEmail);

        InviteMessage message = new InviteMessage(
                gameId,
                inviterUserId,
                inviteeUserId,
                inviterEmail,
                inviteeEmail,
                inviterName,
                inviteeName,
                InviteStatus.ACCEPTED,
                Instant.now()
        );

        messagingTemplate.convertAndSend(destinationForEmail(inviterEmail), message);
        if (gameId != null) {
            return "redirect:/game?gameId=" + gameId;
        }
        return "redirect:/game";
    }

    @PostMapping("/decline")
    public String declineInvite(@RequestParam("inviter") String inviterEmail,
                                Authentication authentication) {
        String inviteeEmail = extractEmail(authentication);
        Optional<User> inviterOpt = userService.findByEmail(inviterEmail);
        Optional<User> inviteeOpt = userService.findByEmail(inviteeEmail);

        String inviteeName = userService.resolveDisplayName(inviteeEmail, extractName(authentication));
        String inviterName = userService.resolveDisplayName(inviterEmail, inviterEmail);

        InviteMessage message = new InviteMessage(
                null,
                inviterOpt.map(User::getUserId).orElse(null),
                inviteeOpt.map(User::getUserId).orElse(null),
                inviterEmail,
                inviteeEmail,
                inviterName,
                inviteeName,
                InviteStatus.DECLINED,
                Instant.now()
        );

        messagingTemplate.convertAndSend(destinationForEmail(inviterEmail), message);
        return "redirect:/dashboard";
    }

    private String destinationForEmail(String email) {
        return "/topic/invites/" + email;
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

    private String extractName(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
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
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }
}
