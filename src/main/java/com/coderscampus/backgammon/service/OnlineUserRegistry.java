package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.web.dto.OnlineUserView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class OnlineUserRegistry {

    private final ConcurrentMap<String, OnlineUserView> onlineUsersByEmail = new ConcurrentHashMap<>();

    public void markOnline(Long userId, String username, String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.toLowerCase();
        String displayName = (username == null || username.isBlank()) ? email : username;
        OnlineUserView view = new OnlineUserView(userId, displayName, email, false);
        onlineUsersByEmail.put(normalizedEmail, view);
    }

    public void markOffline(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        onlineUsersByEmail.remove(email.toLowerCase());
    }

    public List<OnlineUserView> listOthers(String excludeEmail) {
        String normalizedExclude = excludeEmail == null ? null : excludeEmail.toLowerCase();
        List<OnlineUserView> result = new ArrayList<>();
        for (OnlineUserView view : onlineUsersByEmail.values()) {
            if (normalizedExclude != null && Objects.equals(view.email().toLowerCase(), normalizedExclude)) {
                continue;
            }
            result.add(view);
        }
        return result;
    }
}
