package com.coderscampus.backgammon_vanilla.service;

import com.coderscampus.backgammon_vanilla.domain.Invite;
import com.coderscampus.backgammon_vanilla.repository.InviteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InviteService {
    private final InviteRepository inviteRepository;

    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    public Invite createInvite(Long inviterId, Long inviteeId) {
        Invite invite = new Invite(inviterId, inviteeId);
        invite = inviteRepository.save(invite);
        return invite;
    }

    public List<Invite> getInvites(Long inviteeId) {
        return inviteRepository.findByInviteeId(inviteeId);
    }
}
