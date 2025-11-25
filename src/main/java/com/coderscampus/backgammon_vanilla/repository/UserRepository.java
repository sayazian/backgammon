package com.coderscampus.backgammon_vanilla.repository;

import com.coderscampus.backgammon_vanilla.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserId(int userId);

    User findByEmail(String email);

    List<User> findByOnline(boolean b);
}
