package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.domain.Game;
import com.coderscampus.backgammon.domain.User;
import com.coderscampus.backgammon.domain.BoardStatus;
import com.coderscampus.backgammon.repository.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;

    public GameService(GameRepository gameRepository, ObjectMapper objectMapper) {
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
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

    public BoardStatus loadBoardState(Game game) {
        if (game == null || game.getBoardStateJson() == null || game.getBoardStateJson().isBlank()) {
            return new BoardStatus();
        }
        try {
            return objectMapper.readValue(game.getBoardStateJson(), BoardStatus.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not read persisted board state for game " + game.getGameId(), exception);
        }
    }

    public void saveBoardState(Long gameId, BoardStatus boardStatus) {
        Game game = findById(gameId);
        if (game == null || boardStatus == null) {
            return;
        }
        try {
            game.setBoardStateJson(objectMapper.writeValueAsString(boardStatus));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not persist board state for game " + gameId, exception);
        }
        gameRepository.save(game);
    }
}
