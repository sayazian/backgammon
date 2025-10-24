package com.coderscampus.backgammon.repository;

import com.coderscampus.backgammon.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
