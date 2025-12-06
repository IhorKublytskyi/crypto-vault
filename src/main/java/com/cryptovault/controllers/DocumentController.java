package com.cryptovault.controllers;

import com.cryptovault.abstractions.IDocumentService;
import com.cryptovault.models.Document;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Document", description = "Endpoints for encrypting and decrypting documents")
@RequestMapping("/documents")
@RestController
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB limit

    private final IDocumentService documentService;

    public DocumentController(IDocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Encrypt a document", description = "Uploads a file and encrypts it using AES-256-GCM. Requires a Key ID (RSA envelope).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document encrypted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Document.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (empty file or size exceeded)"),
            @ApiResponse(responseCode = "404", description = "Encryption key not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error during encryption")
    })
    @PostMapping(value = "/encrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> encrypt(
            @Parameter(description = "File to be encrypted (max 50MB)", required = true) @RequestParam("file") MultipartFile file,

            @Parameter(description = "ID of the Key to use for encryption", required = true) @RequestParam("keyId") Long keyId) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(
                        createErrorResponse("File size exceeds maximum allowed size of 50 MB"));
            }

            logger.info("Encrypting file: {} (size: {} bytes) with keyId: {}",
                    file.getOriginalFilename(), file.getSize(), keyId);

            byte[] fileBytes = file.getBytes();
            Document savedDocument = documentService.EncryptFile(
                    fileBytes,
                    keyId,
                    file.getOriginalFilename(),
                    file.getContentType());

            logger.info("File encrypted successfully. Document ID: {}", savedDocument.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("document_id", savedDocument.getId());
            response.put("filename", savedDocument.getFilename());
            response.put("algorithm", "AES-256-GCM");
            response.put("iv", savedDocument.getIv());
            response.put("tag", savedDocument.getTag());
            response.put("key_id", savedDocument.getKey().getId());
            response.put("path_enc", savedDocument.getPathEnc());
            response.put("file_hash", savedDocument.getFileHash());
            response.put("original_size", savedDocument.getOriginalSize());
            response.put("created_at", savedDocument.getCreatedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error during encryption", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during file encryption", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Encryption failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Decrypt a document", description = "Decrypts a document by its ID and returns the original file stream.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document decrypted successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error during decryption")
    })
    @GetMapping(value = "/decrypt/{documentId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> decrypt(
            @Parameter(description = "ID of the document to decrypt", required = true) @PathVariable Long documentId) {

        try {
            logger.info("Decrypting document with ID: {}", documentId);

            Resource resource = documentService.DecryptFile(documentId);
            String filename = "decrypted_document_" + documentId;

            logger.info("Document decrypted successfully. Document ID: {}", documentId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            logger.error("Document not found: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Document not found with id: " + documentId));
        } catch (SecurityException e) {
            logger.error("Security error during decryption", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Security error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during file decryption", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Decryption failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Health check", description = "Checks if the service is running")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CryptoVault");
        return ResponseEntity.ok(response);
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}