package com.coderscampus.backgammon.service;

import java.time.Duration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionCleanupTask {

    private final OnlineUserRegistry onlineUserRegistry;
    private static final Duration MAX_IDLE = Duration.ofMinutes(1);

    public SessionCleanupTask(OnlineUserRegistry onlineUserRegistry) {
        this.onlineUserRegistry = onlineUserRegistry;
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanInactiveSessions() {
        onlineUserRegistry.cleanupExpired(MAX_IDLE);
    }
}
