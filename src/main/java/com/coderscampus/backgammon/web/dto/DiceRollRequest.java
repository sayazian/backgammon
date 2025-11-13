package com.coderscampus.backgammon.web.dto;

public record DiceRollRequest(
        String gameKey,
        String playerKey,
        String playerName
) {
}
