package com.coderscampus.backgammon_vanilla.repository;

import com.coderscampus.backgammon_vanilla.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    @Override
    public Game save(Game game);



}
