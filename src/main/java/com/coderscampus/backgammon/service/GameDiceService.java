package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.util.ChannelKeyUtil;
import com.coderscampus.backgammon.web.dto.DiceEventMessage;
import com.coderscampus.backgammon.web.dto.DiceRollRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GameDiceService {

    private final ConcurrentMap<String, DiceSession> sessions = new ConcurrentHashMap<>();

    public List<DiceEventMessage> registerRoll(DiceRollRequest request) {
        if (request == null || !StringUtils.hasText(request.playerKey())) {
            return List.of();
        }
        String sanitizedGameKey = ChannelKeyUtil.sanitize(request.gameKey());
        if (sanitizedGameKey == null) {
            return List.of();
        }

        DiceSession session = sessions.computeIfAbsent(sanitizedGameKey, key -> new DiceSession());
        return session.registerRoll(sanitizedGameKey, safeName(request.playerName()), request.playerKey());
    }

    public Optional<DiceEventMessage> currentState(String gameKey) {
        String sanitized = ChannelKeyUtil.sanitize(gameKey);
        if (sanitized == null) {
            return Optional.empty();
        }
        DiceSession session = sessions.get(sanitized);
        if (session == null) {
            return Optional.empty();
        }
        return session.snapshot(sanitized);
    }

    public void reset(String gameKey) {
        String sanitized = ChannelKeyUtil.sanitize(gameKey);
        if (sanitized != null) {
            sessions.remove(sanitized);
        }
    }

    private String safeName(String name) {
        if (!StringUtils.hasText(name)) {
            return "Player";
        }
        return name.trim();
    }

    private static final class DiceSession {
        private final Map<String, DicePlayerRoll> rolls = new LinkedHashMap<>();
        private DiceResult result;

        private synchronized List<DiceEventMessage> registerRoll(String gameKey,
                                                                 String playerName,
                                                                 String playerKey) {
            List<DiceEventMessage> events = new ArrayList<>();

            if (result != null) {
                events.add(result.toStateEvent(gameKey));
                return events;
            }

            DicePlayerRoll existing = rolls.get(playerKey);
            if (existing != null) {
                events.add(DiceEventMessage.roll(
                        gameKey,
                        existing.playerKey(),
                        existing.playerName(),
                        existing.value(),
                        valueSnapshot(),
                        null));
                return events;
            }

            int value = ThreadLocalRandom.current().nextInt(1, 7);
            DicePlayerRoll rolled = new DicePlayerRoll(playerKey, playerName, value);
            rolls.put(playerKey, rolled);

            events.add(DiceEventMessage.roll(
                    gameKey,
                    rolled.playerKey(),
                    rolled.playerName(),
                    rolled.value(),
                    valueSnapshot(),
                    rolled.playerName() + " rolled a " + rolled.value() + "."));

            if (rolls.size() < 2) {
                return events;
            }

            Collection<DicePlayerRoll> allRolls = new ArrayList<>(rolls.values());
            if (allRolls.size() < 2) {
                return events;
            }

            List<DicePlayerRoll> sorted = new ArrayList<>(allRolls);
            sorted.sort(Comparator.comparingInt(DicePlayerRoll::value).reversed());
            DicePlayerRoll top = sorted.get(0);
            DicePlayerRoll second = sorted.get(1);

            if (top.value() == second.value()) {
                int tieValue = top.value();
                Map<String, Integer> values = valueSnapshot();
                events.add(DiceEventMessage.tie(
                        gameKey,
                        values,
                        "Tie! Both players rolled " + tieValue + ". Roll again."));
                rolls.clear();
                result = null;
                return events;
            }

            result = new DiceResult(top.playerKey(), top.playerName(), valueSnapshot());
            events.add(result.toResultEvent(gameKey));
            return events;
        }

        private synchronized Optional<DiceEventMessage> snapshot(String gameKey) {
            if (result != null) {
                return Optional.of(result.toStateEvent(gameKey));
            }
            if (rolls.isEmpty()) {
                return Optional.empty();
            }
            Map<String, Integer> snapshot = valueSnapshot();
            String message;
            if (rolls.size() == 1) {
                DicePlayerRoll onlyRoll = rolls.values().iterator().next();
                message = onlyRoll.playerName() + " rolled " + onlyRoll.value() + ". Waiting for the opponent.";
            } else {
                message = "Waiting for both players to roll.";
            }
            return Optional.of(DiceEventMessage.state(
                    gameKey,
                    snapshot,
                    null,
                    null,
                    message));
        }

        private Map<String, Integer> valueSnapshot() {
            if (rolls.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, Integer> snapshot = new LinkedHashMap<>();
            rolls.values().forEach(roll -> snapshot.put(roll.playerKey(), roll.value()));
            return snapshot;
        }
    }

    private record DicePlayerRoll(String playerKey, String playerName, int value) {
    }

    private static final class DiceResult {
        private final String starterKey;
        private final String starterName;
        private final Map<String, Integer> finalRolls;

        private DiceResult(String starterKey, String starterName, Map<String, Integer> finalRolls) {
            this.starterKey = starterKey;
            this.starterName = starterName;
            this.finalRolls = Map.copyOf(finalRolls);
        }

        private DiceEventMessage toResultEvent(String gameKey) {
            String message = starterDisplayName() + " starts the game.";
            return DiceEventMessage.result(
                    gameKey,
                    starterKey,
                    starterName,
                    finalRolls,
                    message);
        }

        private DiceEventMessage toStateEvent(String gameKey) {
            String message = starterDisplayName() + " starts the game.";
            return DiceEventMessage.state(
                    gameKey,
                    finalRolls,
                    starterKey,
                    starterName,
                    message);
        }

        private String starterDisplayName() {
            return (starterName == null || starterName.isBlank()) ? "Player" : starterName;
        }
    }
}
