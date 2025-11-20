package com.cryptovault.abstractions;

import com.cryptovault.models.Document;
import org.springframework.core.io.Resource;

public interface IDocumentService {
    Document EncryptFile(byte[] plaintext, Long keyId, String filename, String contentType) throws Exception;

    Resource DecryptFile(Long documentId) throws Exception;
}