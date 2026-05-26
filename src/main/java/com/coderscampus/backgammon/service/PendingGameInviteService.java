package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.dto.GameInvite;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingGameInviteService {
    private final Map<String, GameInvite> pendingInvites = new ConcurrentHashMap<>();

    public GameInvite createPendingInvite(GameInvite invite) {
        invite.setInviteId(UUID.randomUUID().toString());
        pendingInvites.put(invite.getInviteId(), copy(invite));
        return invite;
    }

    public GameInvite findByInviteId(String inviteId) {
        if (inviteId == null) {
            return null;
        }
        return pendingInvites.get(inviteId);
    }

    public GameInvite consume(String inviteId) {
        if (inviteId == null) {
            return null;
        }
        return pendingInvites.remove(inviteId);
    }

    private GameInvite copy(GameInvite invite) {
        GameInvite stored = new GameInvite();
        stored.setInviteId(invite.getInviteId());
        stored.setFromUserId(invite.getFromUserId());
        stored.setFromUserName(invite.getFromUserName());
        stored.setToUserId(invite.getToUserId());
        stored.setToUserName(invite.getToUserName());
        stored.setGameId(invite.getGameId());
        stored.setMessage(invite.getMessage());
        return stored;
    }
}
