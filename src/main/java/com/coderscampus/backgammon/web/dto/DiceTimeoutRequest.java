package com.coderscampus.backgammon.web.dto;

public record DiceTimeoutRequest(
        String gameKey,
        String playerKey,
        String playerName
) {
}
