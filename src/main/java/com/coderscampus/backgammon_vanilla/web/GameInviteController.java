package com.coderscampus.backgammon_vanilla.web;

import com.coderscampus.backgammon_vanilla.domain.Game;
import com.coderscampus.backgammon_vanilla.domain.User;
import com.coderscampus.backgammon_vanilla.dto.GameInvite;
import com.coderscampus.backgammon_vanilla.dto.GameInviteResponse;
import com.coderscampus.backgammon_vanilla.service.AuthUserHelper;
import com.coderscampus.backgammon_vanilla.service.GameService;
import com.coderscampus.backgammon_vanilla.service.UserService;
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

    public GameInviteController(SimpMessagingTemplate messagingTemplate,
                                UserService userService,
                                AuthUserHelper authUserHelper,
                                GameService gameService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.authUserHelper = authUserHelper;
        this.gameService = gameService;
    }

    @MessageMapping("/invite")
    public void sendInvite(GameInvite invite, Authentication authentication) {
        User user = authUserHelper.resolveUser(authentication, userService);
        if (user == null) {
            return;
        }
        invite.setFromUserId(user.getUserId());
        invite.setFromUserName(user.getName());

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
        response.setFromUserId(user.getUserId());
        response.setFromUserName(user.getName());
        Game game = gameService.createGame(response.getToUserId(), user.getUserId());
        response.setGameId(game.getGameId());

        messagingTemplate.convertAndSend(
                "/topic/invitations/responses/" + response.getToUserId(),
                response
        );
    }
}
