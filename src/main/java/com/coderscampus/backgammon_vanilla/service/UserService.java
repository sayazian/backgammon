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
            managedUser.setOnline(user.isOnline());
            managedUser.setFree(user.isFree());
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

    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
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

    public void markOnline(String name, String email, boolean online, boolean free) {
        User user = Optional.ofNullable(userRepository.findByEmail(email))
                .orElseGet(()-> createNewUser(new User(name, email)));
        user.setOnline(online);
        user.setFree(free);
        updateUser(user);
    }
}
