package com.coderscampus.backgammon.web.dto;

import java.util.List;
import java.util.Objects;

public record TurnRollMessage(
        String gameKey,
        String type,
        String rollerKey,
        String rollerName,
        List<Integer> diceValues,
        String nextPlayerKey,
        String nextPlayerName,
        String message,
        Long timestamp
) {

    public static TurnRollMessage from(String gameKey, TurnRollRequest request) {
        if (request == null) {
            return null;
        }
        List<Integer> dice = sanitizeDice(request.diceValues());
        if (dice == null || dice.size() != 2) {
            return null;
        }
        return new TurnRollMessage(
                Objects.requireNonNullElse(gameKey, ""),
                "TURN",
                request.rollerKey(),
                request.rollerName(),
                List.copyOf(dice),
                request.nextPlayerKey(),
                request.nextPlayerName(),
                request.message(),
                System.currentTimeMillis()
        );
    }

    private static List<Integer> sanitizeDice(List<Integer> diceValues) {
        if (diceValues == null || diceValues.size() != 2) {
            return null;
        }
        Integer first = safeDieValue(diceValues.get(0));
        Integer second = safeDieValue(diceValues.get(1));
        if (first == null || second == null) {
            return null;
        }
        return List.of(first, second);
    }

    private static Integer safeDieValue(Integer value) {
        if (value == null) {
            return null;
        }
        int sanitized = Math.max(1, Math.min(6, value));
        return sanitized;
    }
}
