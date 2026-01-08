package com.cryptovault.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_doc_key", columnList = "key_id"),
        @Index(name = "idx_doc_filename", columnList = "filename")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an encrypted document stored in the system")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the document", example = "1001")
    private Long id;

    @NotBlank(message = "Filename is required")
    @Column(nullable = false, length = 255)
    @Schema(description = "Original filename", example = "secret_report.pdf")
    private String filename;

    @NotBlank
    @Column(name = "path_enc", nullable = false, length = 500)
    @Schema(description = "Path to the encrypted file on disk", example = "/uploads/enc/1001.enc")
    private String pathEnc;

    @NotBlank
    @Column(nullable = false, length = 64)
    @Schema(description = "Initialization Vector (IV) for AES encryption (Base64)", example = "d93k...")
    private String iv;

    @NotBlank
    @Column(nullable = false, length = 64)
    @Schema(description = "Authentication Tag (GCM) for integrity check", example = "xYz12...")
    private String tag;

    @Column(name = "wrapped_key", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Encrypted AES key (Envelope encryption)", example = "Base64 string...")
    private String wrappedKey;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_id", nullable = false)
    @Schema(description = "The cryptographic key used to encrypt this document")
    private Key key;

    // SHA-256 for integrity
    @Column(name = "file_hash", length = 64)
    @Schema(description = "SHA-256 hash of the original file", example = "a1b2c3d4...")
    private String fileHash;

    @Column(name = "original_size")
    @Schema(description = "Size of the original file in bytes", example = "102400")
    private Long originalSize;

    // MIME type
    @Column(name = "content_type", length = 100)
    @Schema(description = "MIME type of the file", example = "application/pdf")
    private String contentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Date and time of creation", example = "2023-10-23T10:15:30")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}