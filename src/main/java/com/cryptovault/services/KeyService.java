package com.cryptovault.services;

import com.cryptovault.abstractions.IKeyRepository;
import com.cryptovault.abstractions.IKeyService;
import com.cryptovault.models.Key;
import com.cryptovault.utils.CryptoUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Service
public class KeyService implements IKeyService {
    private final IKeyRepository _repository;
    private final CryptoUtils _cryptoUtils;

    public KeyService(IKeyRepository repository, CryptoUtils cryptoUtils){
        _repository = repository;
        _cryptoUtils = cryptoUtils;
    }

    public SecretKey generateAesKey() throws NoSuchAlgorithmException {
        return _cryptoUtils.generateAesKey();
    }

    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        return _cryptoUtils.generateRsaKeyPair();
    }

    public Key createAesKeyModel(SecretKey secretKey, com.cryptovault.models.User user) {
        Key key = new Key();
        key.setUser(user); // Нужен объект User
        key.setType(Key.KeyType.AES);
        key.setAlgorithm(Key.Algorithm.AES_256_GCM);
        key.setKeyData(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        return key;
    }
}
