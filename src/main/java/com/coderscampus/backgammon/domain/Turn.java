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
@Table(name = "turns")
public class Turn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long turnId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "die1")
    private Integer die1;

    @Column(name = "die2")
    private Integer die2;

    @Lob
    @Column(name = "board_status_origin")
    private String boardStatusOrigin;

    @Lob
    @Column(name = "board_status_final")
    private String boardStatusFinal;

    @OneToMany(mappedBy = "turn")
    private List<Move> moves = new ArrayList<>();

    public Long getTurnId() {
        return turnId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Integer getDie1() {
        return die1;
    }

    public void setDie1(Integer die1) {
        this.die1 = die1;
    }

    public Integer getDie2() {
        return die2;
    }

    public void setDie2(Integer die2) {
        this.die2 = die2;
    }

    public String getBoardStatusOrigin() {
        return boardStatusOrigin;
    }

    public void setBoardStatusOrigin(String boardStatusOrigin) {
        this.boardStatusOrigin = boardStatusOrigin;
    }

    public String getBoardStatusFinal() {
        return boardStatusFinal;
    }

    public void setBoardStatusFinal(String boardStatusFinal) {
        this.boardStatusFinal = boardStatusFinal;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }
}
