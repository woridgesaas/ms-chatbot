package com.woridge.chatbot.controller;

import com.woridge.chatbot.dto.SuccessResponse;
import com.woridge.chatbot.enumeration.DocType;
import com.woridge.chatbot.exception.ingestion
        .FileEmptyException;
import com.woridge.chatbot.exception.ingestion
        .IngestionFailedException;
import com.woridge.chatbot.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart
        .MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping("/upload")
    public ResponseEntity<SuccessResponse<String>>
    upload(
            @RequestParam("file")
            MultipartFile file,
            @RequestParam("docType")
            String docType) {

        if (file.isEmpty()) {
            throw new FileEmptyException(
                    "Le fichier uploadé est vide"
            );
        }

        log.info(
                "Upload — file: {}",
                file.getOriginalFilename()
        );

        try {
            Path tempPath = Files.createTempFile(
                    "ingestion_",
                    "_" + file.getOriginalFilename()
            );
            file.transferTo(tempPath);

            ingestionService.ingestFromFile(
                    tempPath.toFile(),
                    DocType.valueOf(
                            docType.toUpperCase()
                    )
            );

            Files.deleteIfExists(tempPath);

            return ResponseEntity.ok(
                    SuccessResponse
                            .createSuccessResponse(
                                    file.getOriginalFilename(),
                                    "Document ingéré "
                                            + "avec succès"
                            )
            );

        } catch (IngestionFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Erreur upload : {}",
                    e.getMessage()
            );
            throw new IngestionFailedException(
                    e.getMessage()
            );
        }
    }

    @PostMapping("/text")
    public ResponseEntity<SuccessResponse<String>>
    ingestText(
            @RequestParam("content")
            String content,
            @RequestParam("docType")
            String docType,
            @RequestParam("source")
            String source) {

        log.info(
                "Text ingestion — source: {}",
                source
        );

        try {
            Path tempPath = Files.createTempFile(
                    "text_", ".txt"
            );
            Files.writeString(tempPath, content);

            ingestionService.ingestFromFile(
                    tempPath.toFile(),
                    DocType.valueOf(
                            docType.toUpperCase()
                    )
            );

            Files.deleteIfExists(tempPath);

            return ResponseEntity.ok(
                    SuccessResponse
                            .createSuccessResponse(
                                    source,
                                    "Texte ingéré "
                                            + "avec succès"
                            )
            );

        } catch (IngestionFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    "Erreur ingestion text : {}",
                    e.getMessage()
            );
            throw new IngestionFailedException(
                    e.getMessage()
            );
        }
    }
}