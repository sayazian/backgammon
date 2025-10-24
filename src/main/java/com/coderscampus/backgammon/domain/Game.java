package com.coderscampus.backgammon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id")
    private User user2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "starter_user_id")
    private User starter;

    @Lob
    @Column(name = "board_status")
    private String boardStatus;

    @Column(name = "user1_score")
    private Integer user1Score;

    @Column(name = "user2_score")
    private Integer user2Score;

    @Column(name = "completed")
    private Boolean completed = Boolean.FALSE;

    @Column(name = "winner_user_id")
    private Long winnerUserId;

    @OneToMany(mappedBy = "game")
    private List<Turn> turns = new ArrayList<>();

    public Long getGameId() {
        return gameId;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public User getStarter() {
        return starter;
    }

    public void setStarter(User starter) {
        this.starter = starter;
    }

    public String getBoardStatus() {
        return boardStatus;
    }

    public void setBoardStatus(String boardStatus) {
        this.boardStatus = boardStatus;
    }

    public Integer getUser1Score() {
        return user1Score;
    }

    public void setUser1Score(Integer user1Score) {
        this.user1Score = user1Score;
    }

    public Integer getUser2Score() {
        return user2Score;
    }

    public void setUser2Score(Integer user2Score) {
        this.user2Score = user2Score;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Long getWinnerUserId() {
        return winnerUserId;
    }

    public void setWinnerUserId(Long winnerUserId) {
        this.winnerUserId = winnerUserId;
    }

    public List<Turn> getTurns() {
        return turns;
    }

    public void setTurns(List<Turn> turns) {
        this.turns = turns;
    }
}
