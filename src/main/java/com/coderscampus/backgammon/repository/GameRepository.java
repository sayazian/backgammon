package com.coderscampus.backgammon.repository;

import com.coderscampus.backgammon.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("select count(g) from Game g where g.completed = true and (g.user1.userId = :userId or g.user2.userId = :userId)")
    long countCompletedGamesForUser(@Param("userId") Long userId);

    @Query("select count(g) from Game g where g.completed = true and g.winnerUserId = :userId")
    long countWinsForUser(@Param("userId") Long userId);

    @Query("select count(g) from Game g where g.completed = true and (g.user1.userId = :userId or g.user2.userId = :userId) and g.winnerUserId is not null and g.winnerUserId <> :userId")
    long countLossesForUser(@Param("userId") Long userId);
}
