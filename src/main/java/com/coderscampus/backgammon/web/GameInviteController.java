package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.domain.Game;
import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.dto.GameInvite;
import com.coderscampus.backgammon.dto.GameInviteResponse;
import com.coderscampus.backgammon.dto.GameInviteStatus;
import com.coderscampus.backgammon.service.AuthUserHelper;
import com.coderscampus.backgammon.service.GameService;
import com.coderscampus.backgammon.service.GameRuntimeService;
import com.coderscampus.backgammon.service.PendingGameInviteService;
import com.coderscampus.backgammon.service.PresenceService;
import com.coderscampus.backgammon.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class GameInviteController {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final AuthUserHelper authUserHelper;
    private final GameService gameService;
    private final PendingGameInviteService pendingGameInviteService;
    private final PresenceService presenceService;
    private final GameRuntimeService gameRuntimeService;

    public GameInviteController(SimpMessagingTemplate messagingTemplate,
                                UserService userService,
                                AuthUserHelper authUserHelper,
                                GameService gameService,
                                PendingGameInviteService pendingGameInviteService,
                                PresenceService presenceService,
                                GameRuntimeService gameRuntimeService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.authUserHelper = authUserHelper;
        this.gameService = gameService;
        this.pendingGameInviteService = pendingGameInviteService;
        this.presenceService = presenceService;
        this.gameRuntimeService = gameRuntimeService;
    }

    @MessageMapping("/invite")
    public void sendInvite(GameInvite invite, Authentication authentication) {
        User user = authUserHelper.resolveUser(authentication, userService);
        if (user == null) {
            return;
        }
        invite.setFromUserId(user.getUserId());
        invite.setFromUserName(user.getName());

        if (!presenceService.isUserAvailableForInvite(invite.getToUserId())) {
            GameInviteStatus status = new GameInviteStatus();
            status.setToUserId(invite.getToUserId());
            status.setToUserName(invite.getToUserName());
            status.setDelivered(false);
            status.setReason("That user is not currently connected to the invite channel.");
            messagingTemplate.convertAndSend("/topic/invitations/status/" + invite.getFromUserId(), status);
            return;
        }

        pendingGameInviteService.createPendingInvite(invite);

        messagingTemplate.convertAndSend(
                "/topic/invitations/" + invite.getToUserId(),
                invite
        );
    }

    @MessageMapping("/invite/response")
    public void responseToInvite(GameInviteResponse response, Authentication authentication) {
        User user = authUserHelper.resolveUser(authentication, userService);
        if(user == null) {
            return;
        }
        GameInvite pendingInvite = pendingGameInviteService.findByInviteId(response.getInviteId());
        if (pendingInvite == null) {
            return;
        }
        if (!user.getUserId().equals(pendingInvite.getToUserId())) {
            return;
        }

        response.setFromUserId(user.getUserId());
        response.setFromUserName(user.getName());
        response.setToUserId(pendingInvite.getFromUserId());
        response.setToUserName(pendingInvite.getFromUserName());

        if (response.isAccepted()) {
            Game game = gameService.createGame(
                    pendingInvite.getFromUserId(),
                    user.getUserId(),
                    pendingInvite.getFromUserName(),
                    user.getName()
            );
            gameRuntimeService.registerGame(game);
            presenceService.registerGame(game);
            response.setGameId(game.getGameId());
        }

        pendingGameInviteService.consume(response.getInviteId());

        messagingTemplate.convertAndSend(
                "/topic/invitations/responses/" + pendingInvite.getFromUserId(),
                response
        );
    }
}
