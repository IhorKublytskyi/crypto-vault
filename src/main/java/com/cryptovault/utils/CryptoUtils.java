package com.cryptovault.utils;

import com.cryptovault.datatransferobjects.EncryptedData;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Configuration
public class CryptoUtils {

    private final SecureRandom secureRandom = new SecureRandom();

    public SecretKey generateAesKey() throws NoSuchAlgorithmException{
        KeyGenerator generator = KeyGenerator.getInstance(CryptoUtilsConstants.AES_ALGORITHM);

        generator.init(CryptoUtilsConstants.AES_KEY_SIZE, secureRandom);

        return generator.generateKey();
    }

    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(CryptoUtilsConstants.RSA_ALGORITHM);

        keyGen.initialize(CryptoUtilsConstants.RSA_KEY_SIZE, secureRandom);

        return keyGen.generateKeyPair();
    }

    public PublicKey getPublicKeyFromBytes(byte[] keyBytes) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(CryptoUtilsConstants.RSA_ALGORITHM);
        return kf.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    public PrivateKey getPrivateKeyFromBytes(byte[] keyBytes) throws Exception {
        KeyFactory kf = KeyFactory.getInstance(CryptoUtilsConstants.RSA_ALGORITHM);
        return kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    public EncryptedData encryptAES(byte[] bytes, SecretKey key) throws Exception{
        byte[] iv = new byte[CryptoUtilsConstants.GCM_IV_LENGTH];

        secureRandom.nextBytes(iv);

        GCMParameterSpec gcm = new GCMParameterSpec(CryptoUtilsConstants.GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance(CryptoUtilsConstants.AES_GCM_TRANSFORM);

        cipher.init(Cipher.ENCRYPT_MODE, key, gcm);

        byte[] ciphertextWithTag = cipher.doFinal(bytes);

        int tagLengthBytes = CryptoUtilsConstants.GCM_TAG_LENGTH / 8;
        int ciphertextLength = ciphertextWithTag.length - tagLengthBytes;

        byte[] ciphertext = new byte[ciphertextLength];
        byte[] tag = new byte[tagLengthBytes];

        System.arraycopy(ciphertextWithTag, 0, ciphertext, 0, ciphertextLength);
        System.arraycopy(ciphertextWithTag, ciphertextLength, tag, 0, tagLengthBytes);

        return new EncryptedData(ciphertext, iv, tag);
    }

    public byte[] decryptAES(byte[] ciphertext, byte[] iv, byte[] tag, SecretKey key) throws Exception{

        int tagLengthBytes = CryptoUtilsConstants.GCM_TAG_LENGTH / 8;

        byte[] ciphertextWithTag = new byte[ciphertext.length + tagLengthBytes];

        System.arraycopy(ciphertext, 0, ciphertextWithTag, 0, ciphertext.length);
        System.arraycopy(tag, 0, ciphertextWithTag, ciphertext.length, tagLengthBytes);

        GCMParameterSpec gcm = new GCMParameterSpec(CryptoUtilsConstants.GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance(CryptoUtilsConstants.AES_GCM_TRANSFORM);

        cipher.init(Cipher.DECRYPT_MODE, key, gcm);

        return cipher.doFinal(ciphertextWithTag);
    }

    public byte[] wrapKey(byte[] keyBytes, PublicKey rsaPublicKey) throws Exception{
        Cipher cipher = Cipher.getInstance(CryptoUtilsConstants.RSA_TRANSFORM);

        OAEPParameterSpec oaep = new OAEPParameterSpec("SHA-256", "MFG1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);

        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey, oaep);

        return cipher.doFinal(keyBytes);
    }

    public SecretKey unwrapKey(byte[] wrappedKeyBytes, PrivateKey rsaPrivateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(CryptoUtilsConstants.RSA_TRANSFORM);
        OAEPParameterSpec oaep = new OAEPParameterSpec("SHA-256", "MGF1",
                new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);

        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey, oaep);

        byte[] unwrappedBytes = cipher.doFinal(wrappedKeyBytes);

        return new SecretKeySpec(unwrappedBytes, 0, unwrappedBytes.length, CryptoUtilsConstants.AES_ALGORITHM);
    }
}
