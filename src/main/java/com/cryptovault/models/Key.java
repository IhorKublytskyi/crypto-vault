package com.cryptovault.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "keys",
        indexes = {
                @Index(name = "idx_key_user", columnList = "user_id"),
                @Index(name = "idx_key_type_algo", columnList = "type, algorithm")
        })

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Key {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private KeyType type;

    @NotBlank
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Algorithm algorithm;

    @Lob //Large object
    @Column(name = "key_data", nullable = false, columnDefinition = "TEXT")
    private String keyData;

    // Public part of the key RSA
    @Lob
    @Column(name = "public_key_data", columnDefinition = "TEXT")
    private String publicKeyData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "key", cascade = CascadeType.ALL)
    private List<Document> documents = new ArrayList<>();

    public enum KeyType {
        AES,
        RSA
    }

    public enum Algorithm {
        AES_256_GCM,
        RSA_2048_OAEP,
        RSA_3072_OAEP
    }

}
