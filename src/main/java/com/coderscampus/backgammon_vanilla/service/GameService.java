package com.coderscampus.backgammon_vanilla.service;

import com.coderscampus.backgammon_vanilla.domain.Game;
import com.coderscampus.backgammon_vanilla.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game updateGame(Game game){
        boolean newGame = gameRepository.findById(game.getGameId()).isEmpty();
        Game managedGame;
        if (newGame) {
            managedGame = createNewGame(game.getUser1Id(), game.getUser2Id());
        } else {
            managedGame = gameRepository.findByGameId(game.getGameId()).orElseGet(
                    () -> createNewGame(game.getUser1Id(), game.getUser2Id()));

        }
        return gameRepository.save(managedGame);
    }

    public Game createNewGame(Long user1Id, Long user2Id) {
        Game game = new Game(user1Id, user2Id);
        game = gameRepository.save(game);
        return game;
    }

    public Game findById (Long gameId) {
        return gameRepository.findByGameId(gameId).orElse(null);
    }
}
