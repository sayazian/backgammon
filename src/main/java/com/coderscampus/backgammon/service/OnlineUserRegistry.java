package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.web.dto.OnlineUserView;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class OnlineUserRegistry {

    private static class Entry {
        private volatile OnlineUserView view;
        private volatile Instant lastActive;
        private volatile HttpSession session;
        private volatile String sessionId;

        Entry(OnlineUserView view, HttpSession session) {
            this.view = view;
            this.session = session;
            this.sessionId = (session != null) ? session.getId() : null;
            this.lastActive = Instant.now();
        }

        void updateActivity(HttpSession session) {
            this.lastActive = Instant.now();
            if (session != null) {
                this.session = session;
                this.sessionId = session.getId();
            }
        }

        void updateDisplayName(String username) {
            String displayName = (username == null || username.isBlank()) ? this.view.email() : username;
            this.view = new OnlineUserView(this.view.userId(), displayName, this.view.email(), this.view.inGame());
        }

        void invalidateSession() {
            if (session != null) {
                try {
                    session.invalidate();
                } catch (IllegalStateException ignored) {
                    // already invalidated
                }
            }
        }
    }

    private final ConcurrentMap<String, Entry> entriesByEmail = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> emailBySessionId = new ConcurrentHashMap<>();

    public void markOnline(Long userId, String username, String email, HttpSession session) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.toLowerCase();
        String displayName = (username == null || username.isBlank()) ? email : username;
        OnlineUserView view = new OnlineUserView(userId, displayName, email, false);
        Entry entry = new Entry(view, session);
        entriesByEmail.put(normalizedEmail, entry);
        if (session != null) {
            emailBySessionId.put(session.getId(), normalizedEmail);
        }
    }

    public void updateActivity(String email, HttpSession session) {
        if (email == null || email.isBlank()) {
            return;
        }
        Entry entry = entriesByEmail.get(email.toLowerCase());
        if (entry != null) {
            entry.updateActivity(session);
            if (session != null) {
                emailBySessionId.put(session.getId(), email.toLowerCase());
            }
        }
    }

    public void markOffline(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        Entry entry = entriesByEmail.remove(email.toLowerCase());
        if (entry != null && entry.sessionId != null) {
            emailBySessionId.remove(entry.sessionId);
            entry.invalidateSession();
        }
    }

    public void updateDisplayName(String email, String username) {
        if (email == null || email.isBlank()) {
            return;
        }
        Entry entry = entriesByEmail.get(email.toLowerCase());
        if (entry != null) {
            entry.updateDisplayName(username);
        }
    }

    public void removeBySessionId(String sessionId) {
        if (sessionId == null) {
            return;
        }
        String email = emailBySessionId.remove(sessionId);
        if (email != null) {
            Entry entry = entriesByEmail.remove(email);
            if (entry != null) {
                entry.invalidateSession();
            }
        }
    }

    public List<OnlineUserView> listOthers(String excludeEmail) {
        String normalizedExclude = excludeEmail == null ? null : excludeEmail.toLowerCase();
        List<OnlineUserView> result = new ArrayList<>();
        for (Entry entry : entriesByEmail.values()) {
            if (normalizedExclude != null && Objects.equals(entry.view.email().toLowerCase(), normalizedExclude)) {
                continue;
            }
            result.add(entry.view);
        }
        return result;
    }

    public void cleanupExpired(Duration maxIdle) {
        Instant cutoff = Instant.now().minus(maxIdle);
        for (Map.Entry<String, Entry> entry : entriesByEmail.entrySet()) {
            Entry value = entry.getValue();
            if (value.lastActive.isBefore(cutoff)) {
                if (entriesByEmail.remove(entry.getKey(), value)) {
                    if (value.sessionId != null) {
                        emailBySessionId.remove(value.sessionId);
                    }
                    value.invalidateSession();
                }
            }
        }
    }

    public Optional<String> findEmailBySessionId(String sessionId) {
        return Optional.ofNullable(emailBySessionId.get(sessionId));
    }
}
