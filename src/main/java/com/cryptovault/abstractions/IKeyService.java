package com.cryptovault.abstractions;

import com.cryptovault.models.Key;
import com.cryptovault.models.User;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public interface IKeyService {
    SecretKey generateAesKey() throws NoSuchAlgorithmException;
    KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException;
    Key createAesKeyModel(SecretKey secretKey, User user);
    Key createRsaKeyModel(KeyPair keyPair, User user, Key.Algorithm algorithm);
    Key generateAndSaveAesKey(Long userId) throws NoSuchAlgorithmException;
    Key generateAndSaveRsaKey(Long userId, Key.Algorithm algorithm) throws NoSuchAlgorithmException;
    List<Key> getKeysByUserId(Long userId);
    Key getKeyByIdAndUserId(Long keyId, Long userId);
    void deleteKey(Long keyId, Long userId);
    Map<String, Object> getKeyStatistics(Long userId);
}
