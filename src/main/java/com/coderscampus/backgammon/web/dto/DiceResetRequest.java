package com.coderscampus.backgammon.web.dto;

public record DiceResetRequest(
        String gameKey,
        String playerKey,
        String playerName
) {
}
