package com.coderscampus.backgammon.web.dto;

public record OnlineUserView(
        Long userId,
        String username,
        String email,
        boolean inGame
) {
}
