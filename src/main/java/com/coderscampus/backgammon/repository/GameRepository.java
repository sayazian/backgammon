package com.coderscampus.backgammon.repository;

import com.coderscampus.backgammon.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    @Override
    public Game save(Game game);



}
