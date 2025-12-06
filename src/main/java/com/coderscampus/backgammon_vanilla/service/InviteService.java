package com.coderscampus.backgammon_vanilla.service;

import com.coderscampus.backgammon_vanilla.domain.InviteMessage;
import com.coderscampus.backgammon_vanilla.domain.InviteResponseMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class InviteService {
    private final SimpMessagingTemplate messagingTemplate;

    public InviteService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendInvite(InviteMessage msg) {
        if (msg.inviteeEmail() != null) {
            messagingTemplate.convertAndSend("/topic/invites/" + msg.inviteeEmail(), msg);
        }
    }

    public void sendInviteResponse(InviteResponseMessage resp) {
        if (resp != null && resp.inviterEmail() != null) {
            messagingTemplate.convertAndSend("/topic/invites/" + resp.inviterEmail(), resp);
        }
    }

    public void sendInviteResponseTo(String email, InviteResponseMessage resp) {
        if (resp != null && email != null) {
            messagingTemplate.convertAndSend("/topic/invites/" + email, resp);
        }
    }
}
