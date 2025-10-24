package com.coderscampus.backgammon.web;

import com.coderscampus.backgammon.web.dto.InviteMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class InviteWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public InviteWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/invite.send")  // Client sends to /app/invite.send
    public void sendInvite(@Payload InviteMessage invite) {
        messagingTemplate.convertAndSend(
                destinationForEmail(invite.inviteeEmail()),
                invite);
    }

    @MessageMapping("/invite.response")
    public void handleInviteResponse(@Payload InviteMessage invite) {
        messagingTemplate.convertAndSend(
                destinationForEmail(invite.inviterEmail()),
                invite);
    }

    private String destinationForEmail(String email) {
        return "/topic/invites/" + email;
    }
}
