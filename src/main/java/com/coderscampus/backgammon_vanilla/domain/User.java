package com.coderscampus.backgammon_vanilla.domain;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class User {
    String name;
    String email;
    LocalDate joinDate;
    int numberOfGames;
}


