package com.coderscampus.backgammon_vanilla.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long gameId;
    Long user1Id;
    Long user2Id;
    int user1Score;
    int user2Score;
    boolean starter;

    public Game() {
        new Game(null, null);
    }
    public Game(Long user1Id, Long user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }
    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }

    public int getUser1Score() {
        return user1Score;
    }

    public void setUser1Score(int user1Score) {
        this.user1Score = user1Score;
    }

    public int getUser2Score() {
        return user2Score;
    }

    public void setUser2Score(int user2Score) {
        this.user2Score = user2Score;
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }
}
