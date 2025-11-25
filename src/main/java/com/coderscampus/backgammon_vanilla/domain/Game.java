package com.coderscampus.backgammon_vanilla.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    int gameId;
    String user1Id;
    int user1Score;
    int user2Score;
    boolean starter;
}
