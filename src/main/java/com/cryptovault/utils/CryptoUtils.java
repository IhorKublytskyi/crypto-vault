package com.cryptovault.utils;

import com.cryptovault.dtos.EncryptedData;
import org.springframework.stereotype.Component;

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
import java.util.Arrays;

@Component
public class CryptoUtils {

    private static final String AES_ALGO = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPPadding";

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int RSA_KEY_SIZE = 3072;

    public SecretKey generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGO);
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    public EncryptedData encryptAES(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] cipherTextWithTag = cipher.doFinal(plaintext);

        int tagLengthBytes = GCM_TAG_LENGTH / 8;
        int cipherTextLength = cipherTextWithTag.length - tagLengthBytes;

        byte[] actualCipherText = Arrays.copyOfRange(cipherTextWithTag, 0, cipherTextLength);
        byte[] tag = Arrays.copyOfRange(cipherTextWithTag, cipherTextLength, cipherTextWithTag.length);

        return new EncryptedData(actualCipherText, iv, tag);
    }

    public byte[] decryptAES(byte[] cipherText, byte[] iv, byte[] tag, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] cipherTextWithTag = new byte[cipherText.length + tag.length];
        System.arraycopy(cipherText, 0, cipherTextWithTag, 0, cipherText.length);
        System.arraycopy(tag, 0, cipherTextWithTag, cipherText.length, tag.length);

        return cipher.doFinal(cipherTextWithTag);
    }

    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    public PublicKey getPublicKeyFromBytes(byte[] keyBytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public PrivateKey getPrivateKeyFromBytes(byte[] keyBytes) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public byte[] wrapKey(byte[] keyBytesToWrap, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                new MGF1ParameterSpec("SHA-256"),
                PSource.PSpecified.DEFAULT
        );
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);

        return cipher.doFinal(keyBytesToWrap);
    }

    public SecretKey unwrapKey(byte[] wrappedKeyBytes, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                new MGF1ParameterSpec("SHA-256"),
                PSource.PSpecified.DEFAULT
        );
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);

        byte[] decodedKeyBytes = cipher.doFinal(wrappedKeyBytes);
        return new SecretKeySpec(decodedKeyBytes, AES_ALGO);
    }
}