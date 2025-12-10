package com.coderscampus.backgammon_vanilla.service;

import com.coderscampus.backgammon_vanilla.domain.User;
import com.coderscampus.backgammon_vanilla.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User updateUser(User user) {
        boolean newUser = userRepository.findByEmail(user.getEmail()) == null;
        User managedUser;
        if (newUser) {
            managedUser = createNewUser(user);
        } else {
            managedUser = userRepository.findByEmail(user.getEmail());
            managedUser.setName(user.getName());
        }

        return userRepository.save(managedUser);
    }

    private User createNewUser(User user) {
        user.setJoinDate(LocalDate.now());
        user.setNumberOfGames(0);
        userRepository.save(user);
        return user;
    }

    public User findUser(String name, String email) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findByEmail(email));
        return userOpt.orElseGet(() -> createNewUser(new User(name, email)));
    }

    public void logUserIn(User user) {
        user.setOnline(true);
        user.setFree(true);
        updateUser(user);
    }

    public List<User> extractOnlineUsers() {
        return userRepository.findByOnline(true);
    }

    public void logUserOut(User user) {
        user.setOnline(false);
        user.setFree(false);
        saveUser(user);
    }

    public User findById(Long inviteeId) {
        return userRepository.findById(inviteeId).orElse(null);
    }
}
