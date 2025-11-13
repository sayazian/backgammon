package com.coderscampus.backgammon.web.dto;

import java.util.List;

public record TurnRollRequest(
        String gameKey,
        String rollerKey,
        String rollerName,
        List<Integer> diceValues,
        String nextPlayerKey,
        String nextPlayerName,
        String message
) {
}
