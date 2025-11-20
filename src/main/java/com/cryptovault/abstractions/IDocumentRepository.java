package com.cryptovault.abstractions;

import com.cryptovault.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDocumentRepository extends JpaRepository<Document, Long> {
}
