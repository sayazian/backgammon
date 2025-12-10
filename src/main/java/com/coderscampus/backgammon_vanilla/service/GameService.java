package com.coderscampus.backgammon_vanilla.service;

import com.coderscampus.backgammon_vanilla.domain.Game;
import com.coderscampus.backgammon_vanilla.repository.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game createGame(Long inviterId, Long inviteeId) {
        return gameRepository.save(new Game(inviterId, inviteeId));
    }

    public Game findById(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }
}
