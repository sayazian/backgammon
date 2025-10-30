package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.repository.GameRepository;
import com.coderscampus.backgammon.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final OnlineUserRegistry onlineUserRegistry;

    public ProfileService(UserRepository userRepository,
                          GameRepository gameRepository,
                          OnlineUserRegistry onlineUserRegistry) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.onlineUserRegistry = onlineUserRegistry;
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> findProfileByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    Long userId = user.getUserId();
                    long completed = gameRepository.countCompletedGamesForUser(userId);
                    long wins = gameRepository.countWinsForUser(userId);
                    long losses = gameRepository.countLossesForUser(userId);
                    return new UserProfile(
                            userId,
                            user.getUsername(),
                            user.getEmail(),
                            user.getJoinDate(),
                            completed,
                            wins,
                            losses
                    );
                });
    }

    @Transactional
    public void updateUsername(Long userId, String newUsername) {
        if (newUsername == null || newUsername.isBlank()) {
            return;
        }
        userRepository.findById(userId)
                .ifPresent(user -> {
                    user.setUsername(newUsername.trim());
                    userRepository.save(user);
                    onlineUserRegistry.updateDisplayName(user.getEmail(), user.getUsername());
                });
    }

    public record UserProfile(Long userId,
                              String username,
                              String email,
                              LocalDateTime joinDate,
                              long completedGames,
                              long wins,
                              long losses) {
    }
}
