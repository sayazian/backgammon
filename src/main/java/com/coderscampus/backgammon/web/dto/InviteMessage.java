package com.coderscampus.backgammon.web.dto;

import java.time.Instant;

public record InviteMessage(
        Long gameId,
        Long inviterUserId,
        Long inviteeUserId,
        String inviterEmail,
        String inviteeEmail,
        String inviterUsername,
        String inviteeUsername,
        InviteStatus status,
        Instant timestamp
) {
    public enum InviteStatus {
        SENT,
        ACCEPTED,
        DECLINED,
        CANCELLED,
        EXPIRED
    }
}
