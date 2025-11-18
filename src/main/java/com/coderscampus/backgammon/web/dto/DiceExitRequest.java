package com.coderscampus.backgammon.web.dto;

public record DiceExitRequest(
        String gameKey,
        String playerKey,
        String playerName
) {
}
