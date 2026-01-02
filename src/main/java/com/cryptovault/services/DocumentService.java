package com.cryptovault.services;

import com.cryptovault.abstractions.IDocumentRepository;
import com.cryptovault.abstractions.IDocumentService;
import com.cryptovault.abstractions.IKeyRepository;
import com.cryptovault.abstractions.IFileStorageService;
import com.cryptovault.datatransferobjects.EncryptedData;
import com.cryptovault.models.Document;
import com.cryptovault.models.Key;
import com.cryptovault.utils.CryptoUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

@Service
public class DocumentService implements IDocumentService {
    private final IDocumentRepository documentRepository;
    private final IKeyRepository keyRepository;
    private final IFileStorageService fileStorageService;
    private final CryptoUtils cryptoUtils;

    public DocumentService(
            IDocumentRepository documentRepository,
            IKeyRepository keyRepository,
            IFileStorageService fileStorageService,
            CryptoUtils cryptoUtils) {
        this.documentRepository = documentRepository;
        this.keyRepository = keyRepository;
        this.fileStorageService = fileStorageService;
        this.cryptoUtils = cryptoUtils;
    }

    @Override
    public Document EncryptFile(byte[] plaintext, Long keyId, String filename, String contentType) throws Exception {
        Key rsaKey = keyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("Key not found with id: " + keyId));

        if (rsaKey.getType() != Key.KeyType.RSA) {
            throw new IllegalArgumentException("Key must be RSA type for envelope encryption");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plaintext);
            String fileHash = bytesToHex(hashBytes);

            SecretKey sessionAesKey = cryptoUtils.generateAesKey();

            EncryptedData encryptedData = cryptoUtils.encryptAES(plaintext, sessionAesKey);

            PublicKey rsaPublicKey = cryptoUtils.getPublicKeyFromBytes(
                    Base64.getDecoder().decode(rsaKey.getPublicKeyData())
            );
            byte[] wrappedKeyBytes = cryptoUtils.wrapKey(sessionAesKey.getEncoded(), rsaPublicKey);

            String pathEnc = fileStorageService.save(encryptedData.getCiphertext());

            Document document = new Document();
            document.setFilename(filename);
            document.setPathEnc(pathEnc);
            document.setKey(rsaKey);
            document.setIv(Base64.getEncoder().encodeToString(encryptedData.getIv()));
            document.setTag(Base64.getEncoder().encodeToString(encryptedData.getTag()));
            document.setWrappedKey(Base64.getEncoder().encodeToString(wrappedKeyBytes));
            document.setFileHash(fileHash);
            document.setOriginalSize((long) plaintext.length);
            document.setContentType(contentType);

            return documentRepository.save(document);

        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    @Override
    public Resource DecryptFile(Long documentId) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + documentId));

        Key rsaKey = document.getKey();

        PrivateKey rsaPrivateKey = cryptoUtils.getPrivateKeyFromBytes(
                Base64.getDecoder().decode(rsaKey.getKeyData())
        );

        byte[] wrappedKeyBytes = Base64.getDecoder().decode(document.getWrappedKey());
        SecretKey sessionAesKey = cryptoUtils.unwrapKey(wrappedKeyBytes, rsaPrivateKey);

        byte[] encryptedFileContent = fileStorageService.load(document.getPathEnc());

        byte[] iv = Base64.getDecoder().decode(document.getIv());
        byte[] tag = Base64.getDecoder().decode(document.getTag());

        byte[] decryptedBytes = cryptoUtils.decryptAES(encryptedFileContent, iv, tag, sessionAesKey);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(decryptedBytes);
        String computedHash = bytesToHex(hashBytes);

        if (!computedHash.equals(document.getFileHash())) {
            throw new SecurityException("File integrity check failed! File may be corrupted or tampered.");
        }

        return new ByteArrayResource(decryptedBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + documentId));
    }
}