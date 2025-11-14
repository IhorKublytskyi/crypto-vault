package com.cryptovault.models;

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
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Filename is required")
    @Column(nullable = false, length = 255)
    private String filename;

    @NotBlank
    @Column(name = "path_enc", nullable = false, length = 500)
    private String pathEnc;

    @NotBlank
    @Column(nullable = false, length = 64)
    private String iv;

    @NotBlank
    @Column(nullable = false, length = 64)
    private String tag;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_id", nullable = false)
    private Key key;

    // SHA-256 for integrity
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "original_size")
    private Long originalSize;

    // MIME type
    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}