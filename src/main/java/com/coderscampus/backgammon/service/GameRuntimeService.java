package com.coderscampus.backgammon.service;

import com.coderscampus.backgammon.domain.BoardStatus;
import com.coderscampus.backgammon.domain.Game;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameRuntimeService {
    private final Map<Long, BoardStatus> boardStatesByGameId = new ConcurrentHashMap<>();
    private final GameService gameService;

    public GameRuntimeService(GameService gameService) {
        this.gameService = gameService;
    }

    public void registerGame(Game game) {
        if (game == null || game.getGameId() == null) {
            return;
        }
        boardStatesByGameId.computeIfAbsent(game.getGameId(), ignored -> gameService.loadBoardState(game));
    }

    public BoardStatus getBoardState(Long gameId) {
        if (gameId == null) {
            return new BoardStatus();
        }
        return boardStatesByGameId.computeIfAbsent(gameId, ignored -> new BoardStatus());
    }

    public void updateBoardState(Long gameId, BoardStatus boardStatus) {
        if (gameId == null || boardStatus == null) {
            return;
        }
        boardStatesByGameId.put(gameId, boardStatus);
        gameService.saveBoardState(gameId, boardStatus);
    }
}
