package com.coderscampus.backgammon.web.dto;

import java.util.Map;
import java.util.Objects;

public record DiceEventMessage(
        String gameKey,
        String type,
        String playerKey,
        String playerName,
        Integer value,
        Map<String, Integer> rolls,
        String starterKey,
        String starterName,
        String message,
        Long timestamp
) {

    public static DiceEventMessage roll(String gameKey,
                                        String playerKey,
                                        String playerName,
                                        Integer value,
                                        Map<String, Integer> rolls,
                                        String message) {
        return new DiceEventMessage(
                Objects.requireNonNullElse(gameKey, ""),
                "ROLL",
                playerKey,
                playerName,
                value,
                copyOrNull(rolls),
                null,
                null,
                message,
                System.currentTimeMillis());
    }

    public static DiceEventMessage tie(String gameKey,
                                       Map<String, Integer> rolls,
                                       String message) {
        return new DiceEventMessage(
                Objects.requireNonNullElse(gameKey, ""),
                "TIE",
                null,
                null,
                null,
                copyOrNull(rolls),
                null,
                null,
                message,
                System.currentTimeMillis());
    }

    public static DiceEventMessage result(String gameKey,
                                          String starterKey,
                                          String starterName,
                                          Map<String, Integer> rolls,
                                          String message) {
        return new DiceEventMessage(
                Objects.requireNonNullElse(gameKey, ""),
                "RESULT",
                null,
                null,
                null,
                copyOrNull(rolls),
                starterKey,
                starterName,
                message,
                System.currentTimeMillis());
    }

    public static DiceEventMessage state(String gameKey,
                                         Map<String, Integer> rolls,
                                         String starterKey,
                                         String starterName,
                                         String message) {
        return new DiceEventMessage(
                Objects.requireNonNullElse(gameKey, ""),
                "STATE",
                null,
                null,
                null,
                copyOrNull(rolls),
                starterKey,
                starterName,
                message,
                System.currentTimeMillis());
    }

    public static DiceEventMessage reset(String gameKey, String message) {
        return new DiceEventMessage(
                Objects.requireNonNullElse(gameKey, ""),
                "RESET",
                null,
                null,
                null,
                null,
                null,
                null,
                message,
                System.currentTimeMillis());
    }

    public static DiceEventMessage timeout(String gameKey, String message) {
        return new DiceEventMessage(
                Objects.requireNonNullElse(gameKey, ""),
                "TIMEOUT",
                null,
                null,
                null,
                null,
                null,
                null,
                message,
                System.currentTimeMillis());
    }

    private static Map<String, Integer> copyOrNull(Map<String, Integer> rolls) {
        if (rolls == null) {
            return null;
        }
        return Map.copyOf(rolls);
    }
}
