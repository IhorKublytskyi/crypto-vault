package com.cryptovault.config;

import com.cryptovault.models.User;
import com.cryptovault.models.Key;
import com.cryptovault.repositories.UserRepository;
import com.cryptovault.services.KeyService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final KeyService keyService;

    public DataInitializer(UserRepository userRepository, KeyService keyService) {
        this.userRepository = userRepository;
        this.keyService = keyService;
    }

    @PostConstruct
    @Transactional
    public void initializeTestData() {
        if (userRepository.count() > 0) {
            return;
        }

        try {
            User user1 = createUser("Alice", "alice@cryptovault.com", "password123");
            User user2 = createUser("Bob", "bob@cryptovault.com", "password123");
            User user3 = createUser("Charlie", "charlie@cryptovault.com", "password123");

            logger.info("✓ Created {} users", 3);

            // Alice: 2 RSA, 1 AES
            Key aliceRsa1 = keyService.generateAndSaveRsaKey(user1.getId(), Key.Algorithm.RSA_3072_OAEP);
            Key aliceRsa2 = keyService.generateAndSaveRsaKey(user1.getId(), Key.Algorithm.RSA_2048_OAEP);
            Key aliceAes = keyService.generateAndSaveAesKey(user1.getId());

            logger.info("✓ Created {} keys for Alice (ID: {})", 3, user1.getId());

            // Bob: 1 RSA , 2 AES
            Key bobRsa = keyService.generateAndSaveRsaKey(user2.getId(), Key.Algorithm.RSA_3072_OAEP);
            Key bobAes1 = keyService.generateAndSaveAesKey(user2.getId());
            Key bobAes2 = keyService.generateAndSaveAesKey(user2.getId());

            logger.info("✓ Created {} keys for Bob (ID: {})", 3, user2.getId());

            // Charlie: 1 RSA
            Key charlieRsa = keyService.generateAndSaveRsaKey(user3.getId(), Key.Algorithm.RSA_3072_OAEP);

            logger.info("✓ Created {} keys for Charlie (ID: {})", 1, user3.getId());

            logger.info("User 1: Alice (ID: {}) - email: alice@cryptovault.com", user1.getId());
            logger.info("  - RSA Key ID: {} (RSA_3072_OAEP)", aliceRsa1.getId());
            logger.info("  - RSA Key ID: {} (RSA_2048_OAEP)", aliceRsa2.getId());
            logger.info("  - AES Key ID: {} (AES_256_GCM)", aliceAes.getId());
            logger.info("");
            logger.info("User 2: Bob (ID: {}) - email: bob@cryptovault.com", user2.getId());
            logger.info("  - RSA Key ID: {} (RSA_3072_OAEP)", bobRsa.getId());
            logger.info("  - AES Key ID: {} (AES_256_GCM)", bobAes1.getId());
            logger.info("  - AES Key ID: {} (AES_256_GCM)", bobAes2.getId());
            logger.info("");
            logger.info("User 3: Charlie (ID: {}) - email: charlie@cryptovault.com", user3.getId());
            logger.info("  - RSA Key ID: {} (RSA_3072_OAEP)", charlieRsa.getId());
        } catch (Exception e) {
            logger.error("Error while initializing", e);
        }
    }

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        return userRepository.save(user);
    }
}

