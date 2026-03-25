package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.domain.Game;
import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.repository.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private final GameRepository gameRepository;
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    public Game createGame(Long user1Id, Long user2Id, String fromUserName, String toUserName) {
        Game game = new Game();
        game.setUser1Id(user1Id);
        game.setUser2Id(user2Id);
        game.setUser1Name(fromUserName);
        game.setUser2Name(toUserName);
        gameRepository.save(game);
        return game;
    }

    public Game findById(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    public Game createTestGame(User user) {
        Game game = new Game();
        game.setUser1Id(user.getUserId());
        game.setUser1Name(user.getName());
        game.setUser2Name("Test Opponent");
        game.setStarter(true);
        return gameRepository.save(game);
    }
}
