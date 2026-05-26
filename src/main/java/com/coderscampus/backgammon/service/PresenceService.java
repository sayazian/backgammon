package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.domain.Game;
import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.dto.GamePresenceStatus;
import com.coderscampus.backgammon.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {
    private static final Duration ONLINE_WINDOW = Duration.ofSeconds(30);
    private static final Duration GAME_RECONNECT_GRACE = Duration.ofMinutes(3);

    private final UserRepository userRepository;
    private final Map<Long, Instant> dashboardHeartbeats = new ConcurrentHashMap<>();
    private final Map<Long, GamePresenceRecord> gamePresenceByGameId = new ConcurrentHashMap<>();
    private final Map<Long, Long> activeGameByUserId = new ConcurrentHashMap<>();

    public PresenceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void touchDashboard(Long userId) {
        if (userId == null) {
            return;
        }
        dashboardHeartbeats.put(userId, Instant.now());
    }

    public List<User> getOnlineUsers() {
        List<User> users = userRepository.findAll();
        List<User> onlineUsers = new ArrayList<>();
        for (User user : users) {
            if (!isUserOnline(user.getUserId())) {
                continue;
            }
            user.setOnline(true);
            user.setFree(isUserAvailableForInvite(user.getUserId()));
            onlineUsers.add(user);
        }
        return onlineUsers;
    }

    public boolean isUserOnline(Long userId) {
        Instant dashboardSeen = dashboardHeartbeats.get(userId);
        if (isFresh(dashboardSeen, ONLINE_WINDOW)) {
            return true;
        }
        Long activeGameId = activeGameByUserId.get(userId);
        if (activeGameId == null) {
            return false;
        }
        GamePresenceRecord record = gamePresenceByGameId.get(activeGameId);
        if (record == null) {
            return false;
        }
        Instant gameSeen = record.lastSeenByUserId.get(userId);
        return isFresh(gameSeen, ONLINE_WINDOW);
    }

    public boolean isUserAvailableForInvite(Long userId) {
        return isFresh(dashboardHeartbeats.get(userId), ONLINE_WINDOW) && activeGameByUserId.get(userId) == null;
    }

    public void clearUserPresence(Long userId) {
        if (userId == null) {
            return;
        }
        dashboardHeartbeats.remove(userId);
    }

    public void registerGame(Game game) {
        if (game == null || game.getGameId() == null) {
            return;
        }
        gamePresenceByGameId.computeIfAbsent(game.getGameId(), ignored -> new GamePresenceRecord(
                game.getUser1Id(),
                game.getUser1Name(),
                game.getUser2Id(),
                game.getUser2Name()
        ));
        if (game.getUser1Id() != null) {
            activeGameByUserId.put(game.getUser1Id(), game.getGameId());
        }
        if (game.getUser2Id() != null) {
            activeGameByUserId.put(game.getUser2Id(), game.getGameId());
        }
    }

    public void touchGame(Long userId, Long gameId) {
        if (userId == null || gameId == null) {
            return;
        }
        GamePresenceRecord record = gamePresenceByGameId.get(gameId);
        if (record == null) {
            return;
        }
        record.lastSeenByUserId.put(userId, Instant.now());
        activeGameByUserId.put(userId, gameId);
    }

    public Long findReconnectableGameId(Long userId) {
        Long gameId = activeGameByUserId.get(userId);
        if (gameId == null) {
            return null;
        }
        GamePresenceRecord record = gamePresenceByGameId.get(gameId);
        if (record == null) {
            return null;
        }
        Instant lastSeen = record.lastSeenByUserId.get(userId);
        if (lastSeen == null) {
            return null;
        }
        if (isFresh(lastSeen, ONLINE_WINDOW.plus(GAME_RECONNECT_GRACE))) {
            return gameId;
        }
        return null;
    }

    public GamePresenceStatus buildGamePresenceStatus(Game game, Long currentUserId) {
        registerGame(game);
        GamePresenceRecord record = gamePresenceByGameId.get(game.getGameId());
        boolean user1Connected = isFresh(record.lastSeenByUserId.get(record.user1Id), ONLINE_WINDOW);
        boolean user2Connected = isFresh(record.lastSeenByUserId.get(record.user2Id), ONLINE_WINDOW);

        GamePresenceStatus status = new GamePresenceStatus();
        status.setGameId(game.getGameId());
        status.setUser1Id(record.user1Id);
        status.setUser2Id(record.user2Id);
        status.setAllConnected(user1Connected && user2Connected);
        status.setUser1Connected(user1Connected);
        status.setUser2Connected(user2Connected);

        if (!user1Connected) {
            status.setDisconnectedUserName(record.user1Name);
            status.setReconnectGraceSecondsRemaining(remainingGraceSeconds(record.lastSeenByUserId.get(record.user1Id)));
        } else if (!user2Connected) {
            status.setDisconnectedUserName(record.user2Name);
            status.setReconnectGraceSecondsRemaining(remainingGraceSeconds(record.lastSeenByUserId.get(record.user2Id)));
        }

        return status;
    }

    private long remainingGraceSeconds(Instant lastSeen) {
        if (lastSeen == null) {
            return 0;
        }
        Instant deadline = lastSeen.plus(ONLINE_WINDOW).plus(GAME_RECONNECT_GRACE);
        long seconds = Duration.between(Instant.now(), deadline).getSeconds();
        return Math.max(seconds, 0);
    }

    private boolean isFresh(Instant instant, Duration window) {
        return instant != null && instant.isAfter(Instant.now().minus(window));
    }

    private static class GamePresenceRecord {
        private final Long user1Id;
        private final String user1Name;
        private final Long user2Id;
        private final String user2Name;
        private final Map<Long, Instant> lastSeenByUserId = new ConcurrentHashMap<>();

        private GamePresenceRecord(Long user1Id, String user1Name, Long user2Id, String user2Name) {
            this.user1Id = user1Id;
            this.user1Name = user1Name;
            this.user2Id = user2Id;
            this.user2Name = user2Name;
        }
    }
}
