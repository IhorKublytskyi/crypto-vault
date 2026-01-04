package com.cryptovault.services;

import com.cryptovault.models.User;
import com.cryptovault.repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (!user.getPasswordHash().startsWith("$2a$") && !user.getPasswordHash().startsWith("$2b$")) {
            String hashedPassword = passwordEncoder.encode(user.getPasswordHash());
            user.setPasswordHash(hashedPassword);
        } else {
            logger.warn("Password already hashed for user: {}", user.getUsername());
        }

        return userRepository.save(user);
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("User with id " + id + " not found");
        }
    }
}