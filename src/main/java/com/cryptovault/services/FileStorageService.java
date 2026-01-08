package com.cryptovault.services;

import com.cryptovault.abstractions.IFileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class FileStorageService implements IFileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${cryptovault.storage.path:./uploads/encrypted}")
    private String storageBasePath;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(storageBasePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created storage directory: {}", storageBasePath);
            }
        } catch (IOException e) {
            logger.error("Failed to create storage directory", e);
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public String save(byte[] data) throws IOException {
        String filename = UUID.randomUUID().toString() + ".enc";
        Path filePath = Paths.get(storageBasePath, filename);

        try {
            Files.write(filePath, data, StandardOpenOption.CREATE_NEW);
            logger.debug("Saved encrypted file: {}", filename);
            return filename;
        } catch (IOException e) {
            logger.error("Failed to save file: {}", filename, e);
            throw e;
        }
    }

    @Override
    public byte[] load(String filename) throws IOException {
        Path filePath = Paths.get(storageBasePath, filename);

        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filename);
        }

        try {
            byte[] data = Files.readAllBytes(filePath);
            logger.debug("Loaded encrypted file: {}", filename);
            return data;
        } catch (IOException e) {
            logger.error("Failed to load file: {}", filename, e);
            throw e;
        }
    }

    public void delete(String filename) throws IOException {
        Path filePath = Paths.get(storageBasePath, filename);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            logger.info("Deleted file: {}", filename);
        }
    }
}