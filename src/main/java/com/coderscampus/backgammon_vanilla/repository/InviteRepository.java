package com.coderscampus.backgammon_vanilla.repository;

import com.coderscampus.backgammon_vanilla.domain.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    public List<Invite> findByInviteeId(Long inviteeId);
}
