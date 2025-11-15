package com.cryptovault.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cryptovault.models.Document;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByFilename(String filename);

    List<Document> findByKeyId(Long keyId);

    List<Document> findByKeyUserId(Long keyId);

    List<Document> findByContentType(String contentType);

    List<Document> findByFilenameEndingWith(String extension);

    boolean existsByFilename(String filename);

    Long countByKeyUserId(Long userId);

    @Query ("SELECT d FROM Document d WHERE d.fileHash IS NOT NULL")
    List<Document> findDocumentsWithHash();

    @Query(value = "SELECT * FROM documents ORDER BY original_size DESC LIMIT :limit", nativeQuery = true)
    List<Document> findTopNLargestDocuments(@Param("limit") int limit);
}
