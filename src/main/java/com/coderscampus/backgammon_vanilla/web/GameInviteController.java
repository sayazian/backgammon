package com.coderscampus.backgammon_vanilla.web;

import com.coderscampus.backgammon_vanilla.domain.User;
import com.coderscampus.backgammon_vanilla.dto.GameInvite;
import com.coderscampus.backgammon_vanilla.dto.GameInviteResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class GameInviteController {
    private final SimpMessagingTemplate messagingTemplate;

    public GameInviteController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping
    public void sendInvite(GameInvite invite, User user) {
        invite.setFromUserId(user.getUserId());

        messagingTemplate.convertAndSendToUser(
                String.valueOf(invite.getToUserId()),
                "/queue/invitations",
                invite
        );
    }

    @MessageMapping("/invite/response")
    public void responseToInvite(GameInviteResponse response, User user) {
        response.setFromUserId(user.getUserId());

        messagingTemplate.convertAndSendToUser(
                String.valueOf(response.getToUserId()),
                "queue/invitations/responses",
                response
        );
    }
}
