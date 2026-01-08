package com.cryptovault.services;

import com.cryptovault.abstractions.IKeyRepository;
import com.cryptovault.abstractions.IKeyService;
import com.cryptovault.models.Document;
import com.cryptovault.models.Key;
import com.cryptovault.models.User;
import com.cryptovault.repositories.UserRepository;
import com.cryptovault.utils.CryptoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class KeyService implements IKeyService {
    private final IKeyRepository _repository;
    private final CryptoUtils _cryptoUtils;
    private final UserRepository userRepository;

    public KeyService(IKeyRepository repository, CryptoUtils cryptoUtils, UserRepository userRepository){
        _repository = repository;
        _cryptoUtils = cryptoUtils;
        this.userRepository = userRepository;
    }

    public SecretKey generateAesKey() throws NoSuchAlgorithmException {
        return _cryptoUtils.generateAesKey();
    }

    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        return _cryptoUtils.generateRsaKeyPair();
    }

    public Key createAesKeyModel(SecretKey secretKey, com.cryptovault.models.User user) {
        Key key = new Key();
        key.setUser(user);
        key.setType(Key.KeyType.AES);
        key.setAlgorithm(Key.Algorithm.AES_256_GCM);
        key.setKeyData(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        return key;
    }

    public Key createRsaKeyModel(KeyPair keyPair, User user, Key.Algorithm algorithm) {
        Key key = new Key();
        key.setUser(user);
        key.setType(Key.KeyType.RSA);
        key.setAlgorithm(algorithm != null ? algorithm : Key.Algorithm.RSA_3072_OAEP);
        key.setKeyData(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        key.setPublicKeyData(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        return key;
    }

    @Transactional
    public Key generateAndSaveAesKey(Long userId) throws NoSuchAlgorithmException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        SecretKey secretKey = generateAesKey();
        Key key = createAesKeyModel(secretKey, user);
        Key savedKey = _repository.save(key);
        return savedKey;
    }

    @Transactional
    public Key generateAndSaveRsaKey(Long userId, Key.Algorithm algorithm) throws NoSuchAlgorithmException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        KeyPair keyPair = generateRsaKeyPair();
        Key key = createRsaKeyModel(keyPair, user, algorithm);
        Key savedKey = _repository.save(key);
        return savedKey;
    }

    public List<Key> getKeysByUserId(Long userId) {
        return _repository.findByUserId(userId);
    }

    public Key getKeyByIdAndUserId(Long keyId, Long userId) {
        return _repository.findByIdAndUserId(keyId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Key not found with id: " + keyId + " for user: " + userId));
    }

    @Transactional
    public void deleteKey(Long keyId, Long userId) {
        Key key = _repository.findByIdAndUserId(keyId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Key not found with id: " + keyId + " for user: " + userId));
        if (!key.getDocuments().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete key: it is used by " + key.getDocuments().size() + " document(s)");
        }
        _repository.delete(key);
    }

    public Map<String, Object> getKeyStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        List<Key> allKeys = _repository.findByUserId(userId);

        long totalKeys = allKeys.size();
        long aesKeys = allKeys.stream().filter(k -> k.getType() == Key.KeyType.AES).count();
        long rsaKeys = allKeys.stream().filter(k -> k.getType() == Key.KeyType.RSA).count();
        long totalDocuments = allKeys.stream().mapToLong(k -> k.getDocuments().size()).sum();

        List<Map<String, Object>> keysDetails = allKeys.stream()
                .map(key -> {
                    Map<String, Object> keyMap = new HashMap<>();
                    keyMap.put("id", key.getId());
                    keyMap.put("type", key.getType().toString());
                    keyMap.put("algorithm", key.getAlgorithm().toString());
                    List<Long> documentIds = key.getDocuments().stream()
                            .map(Document::getId)
                            .collect(Collectors.toList());

                    keyMap.put("documents", documentIds);
                    keyMap.put("documents_count", key.getDocuments().size());
                    keyMap.put("createdAt", key.getCreatedAt().toString()); // LocalDateTime → String
                    return keyMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("user_id", userId);
        stats.put("username", user.getUsername());
        stats.put("total_keys", totalKeys);
        stats.put("aes_keys", aesKeys);
        stats.put("rsa_keys", rsaKeys);
        stats.put("total_documents", totalDocuments);
        stats.put("keys_details", keysDetails);

        return stats;
    }
}
