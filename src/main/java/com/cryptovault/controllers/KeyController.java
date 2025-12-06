package com.cryptovault.controllers;

import com.cryptovault.abstractions.IKeyService;
import com.cryptovault.models.Key;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Key", description = "Endpoints for managing cryptographic keys (AES, RSA)")
@RequestMapping("/keys")
@RestController

public class KeyController {
    private static final Logger logger = LoggerFactory.getLogger(KeyController.class);
    private final IKeyService keyService;

    @Value("${cryptovault.scripts.path}")
    private String scriptsPath;

    @Value("${cryptovault.reports.output.path}")
    private String reportsOutputPath;

    @Value("${cryptovault.scripts.python.executable:python3}")
    private String pythonExecutable;

    public KeyController(IKeyService keyService) {
        this.keyService = keyService;
    }

    // POST keys/generate
    @Operation(summary = "Generate a new cryptographic key", description = "Generates a new AES-256-GCM or RSA key for the specified user")
    @ApiResponse(responseCode = "201", description = "Key generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "500", description = "Internal server error during key generation")
    @PostMapping("/generate")
    public ResponseEntity<?> generateKey(
            @Parameter(description = "User ID", required = true) @RequestParam("userId") Long userId,

            @Parameter(description = "Key type: AES or RSA", required = true) @RequestParam("keyType") String keyType,

            @Parameter(description = "Algorithm (for RSA: RSA_2048_OAEP or RSA_3072_OAEP)") @RequestParam(value = "algorithm", required = false) String algorithm) {

        try {
            logger.info("Generating {} key for user: {}", keyType, userId);

            Key savedKey;
            if ("AES".equalsIgnoreCase(keyType)) {
                savedKey = keyService.generateAndSaveAesKey(userId);

            } else if ("RSA".equalsIgnoreCase(keyType)) {
                Key.Algorithm rsaAlgorithm = null;
                if (algorithm != null) {
                    try {
                        rsaAlgorithm = Key.Algorithm.valueOf(algorithm);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                                .body(createErrorResponse("Invalid algorithm: " + algorithm));
                    }
                }
                savedKey = keyService.generateAndSaveRsaKey(userId, rsaAlgorithm);

            } else {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid key type. Must be AES or RSA"));
            }

            logger.info("Key generated successfully. Key ID: {}", savedKey.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("key_id", savedKey.getId());
            response.put("type", savedKey.getType());
            response.put("algorithm", savedKey.getAlgorithm());
            response.put("created_at", savedKey.getCreatedAt());
            response.put("user_id", savedKey.getUser().getId());

            if (savedKey.getType() == Key.KeyType.RSA) {
                response.put("public_key", savedKey.getPublicKeyData());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error during key generation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));

        } catch (Exception e) {
            logger.error("Error during key generation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Key generation failed: " + e.getMessage()));
        }
    }

    // GET /keys
    @Operation(summary = "Get all keys for a user", description = "Returns a list of all cryptographic keys belonging to the specified user")
    @ApiResponse(responseCode = "200", description = "Keys retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping
    public ResponseEntity<?> getKeys(
            @Parameter(description = "User ID", required = true) @RequestParam("userId") Long userId) {

        try {
            logger.info("Fetching keys for user: {}", userId);

            List<Key> keys = keyService.getKeysByUserId(userId);

            List<Map<String, Object>> keyList = keys.stream().map(key -> {
                Map<String, Object> keyInfo = new HashMap<>();
                keyInfo.put("key_id", key.getId());
                keyInfo.put("type", key.getType());
                keyInfo.put("algorithm", key.getAlgorithm());
                keyInfo.put("created_at", key.getCreatedAt());
                keyInfo.put("documents_count", key.getDocuments() != null ? key.getDocuments().size() : 0);

                if (key.getType() == Key.KeyType.RSA) {
                    keyInfo.put("public_key", key.getPublicKeyData());
                }

                return keyInfo;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("user_id", userId);
            response.put("keys_count", keyList.size());
            response.put("keys", keyList);

            logger.info("Retrieved {} keys for user: {}", keyList.size(), userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching keys for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch keys: " + e.getMessage()));
        }
    }

    // DELETE /keys/{id}

    @Operation(summary = "Delete a cryptographic key", description = "Deletes a key if it is not used by any documents")
    @ApiResponse(responseCode = "200", description = "Key deleted successfully")
    @ApiResponse(responseCode = "404", description = "Key not found")
    @ApiResponse(responseCode = "409", description = "Key is in use and cannot be deleted")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{keyId}")
    public ResponseEntity<?> deleteKey(
            @Parameter(description = "Key ID to delete", required = true) @PathVariable Long keyId,

            @Parameter(description = "User ID", required = true) @RequestParam("userId") Long userId) {

        try {
            logger.info("Attempting to delete key: {} for user: {}", keyId, userId);

            keyService.deleteKey(keyId, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Key deleted successfully");
            response.put("key_id", String.valueOf(keyId));

            logger.info("Key deleted successfully: {}", keyId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Key not found: {}", keyId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Key not found with id: " + keyId));

        } catch (IllegalStateException e) {
            logger.error("Key is in use: {}", keyId, e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(e.getMessage()));

        } catch (Exception e) {
            logger.error("Error deleting key: {}", keyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Key deletion failed: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    // GET /keys/report
    @Operation(summary = "Generate Excel report with key statistics", description = "Generates and downloads an Excel report containing key statistics for the specified user")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "500", description = "Report generation failed")
    @GetMapping("/report")
    public ResponseEntity<?> generateKeyReport(
            @Parameter(description = "User ID", required = true) @RequestParam("userId") Long userId) {

        try {
            logger.info("Generating key statistics report for user: {}", userId);

            Map<String, Object> stats = keyService.getKeyStatistics(userId);
            ObjectMapper mapper = new ObjectMapper();
            String statsJson = mapper.writeValueAsString(stats);

            String tempJsonPath = reportsOutputPath + "/temp_stats_" + userId + "_" + System.currentTimeMillis()
                    + ".json";
            File tempJsonFile = new File(tempJsonPath);

            File outputDir = new File(reportsOutputPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            try (java.io.FileWriter writer = new java.io.FileWriter(tempJsonFile)) {
                writer.write(statsJson);
            }

            String filename = "key_statistics_user_" + userId + "_" + System.currentTimeMillis() + ".xlsx";
            String outputPath = reportsOutputPath + "/" + filename;

            String scriptPath = scriptsPath + "/generate_key_report.py";
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    scriptPath,
                    tempJsonPath,
                    outputPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            // output reading for logging
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.debug("Python script output: {}", line);
            }

            int exitCode = process.waitFor();
            tempJsonFile.delete();
            if (exitCode != 0) {
                logger.error("Script output: {}", output.toString());
                throw new RuntimeException("Report generation script failed with exit code: " + exitCode);
            }

            File reportFile = new File(outputPath);
            if (!reportFile.exists()) {
                throw new RuntimeException("Report file was not generated");
            }

            Resource resource = new FileSystemResource(reportFile);

            logger.info("Report generated successfully for user: {}", userId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (IllegalArgumentException e) {
            logger.error("User not found: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("User not found with id: " + userId));

        } catch (Exception e) {
            logger.error("Error generating report for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Report generation failed: " + e.getMessage()));
        }
    }

}
