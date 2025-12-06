package com.coderscampus.backgammon_vanilla.repository;

import com.coderscampus.backgammon_vanilla.domain.Game;
import com.coderscampus.backgammon_vanilla.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameId(Long gameId);
}
