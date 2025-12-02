package com.coderscampus.backgammon_vanilla.domain;

public record InviteMessage(Long fromUserId,
                            Long toUserId,
                            String fromName,
                            String inviteeEmail,
                            String inviterEmail) {
}
