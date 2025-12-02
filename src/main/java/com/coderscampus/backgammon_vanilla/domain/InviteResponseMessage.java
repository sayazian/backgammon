package com.coderscampus.backgammon_vanilla.domain;

public record InviteResponseMessage(Long fromUserId,
                                    String fromName,
                                    Long toUserId,
                                    boolean accepted,
                                    String inviterEmail) {
}
