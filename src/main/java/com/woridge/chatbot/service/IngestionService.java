package com.woridge.chatbot.service;

import com.woridge.chatbot.config.WoridgeProperties;
import com.woridge.chatbot.enumeration.DocType;
import com.woridge.chatbot.exception.ingestion
        .FileEmptyException;
import com.woridge.chatbot.exception.ingestion
        .IngestionFailedException;
import com.woridge.chatbot.model.KnowledgeChunk;
import com.woridge.chatbot.repository
        .KnowledgeChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation
        .Transactional;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final KnowledgeChunkRepository chunkRepo;
    private final WoridgeProperties properties;

    // ── Ingestion depuis URL ──
    @Transactional
    public void ingestFromUrl(
            String fileUrl,
            DocType docType,
            String tenant) {

        log.info("Ingestion URL : {} tenant: {}",
                fileUrl, tenant);

        try (InputStream is =
                     new URL(fileUrl).openStream()) {

            String text = extractTextFromPdf(is);
            ingestText(text, docType, fileUrl, tenant);

        } catch (Exception e) {
            log.error("Erreur ingestion URL : {}",
                    e.getMessage());
            throw new IngestionFailedException(
                    "Ingestion URL echouee : "
                            + e.getMessage()
            );
        }
    }

    // ── Ingestion depuis fichier ──
    @Transactional
    public void ingestFromFile(
            File file,
            DocType docType,
            String tenant) {

        log.info("Ingestion fichier : {} tenant: {}",
                file.getName(), tenant);

        if (!file.exists() || file.length() == 0) {
            throw new FileEmptyException(
                    "Fichier vide ou introuvable"
            );
        }

        try {
            String text;
            String name = file.getName().toLowerCase();

            if (name.endsWith(".pdf")) {
                try (PDDocument doc =
                             PDDocument.load(file)) {
                    text = new PDFTextStripper()
                            .getText(doc);
                }
            } else {
                text = new String(
                        Files.readAllBytes(file.toPath())
                );
            }

            ingestText(
                    text, docType,
                    file.getName(), tenant
            );

        } catch (IngestionFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur ingestion fichier : {}",
                    e.getMessage());
            throw new IngestionFailedException(
                    "Ingestion fichier echouee : "
                            + e.getMessage()
            );
        }
    }

    // ── Ingestion du texte en chunks ──
    @Transactional
    public void ingestText(
            String text,
            DocType docType,
            String source,
            String tenant) {

        if (text == null || text.isBlank()) {
            throw new IngestionFailedException(
                    "Texte vide — rien a ingerer"
            );
        }

        // Supprimer les chunks existants
        // pour cette source et ce tenant
        chunkRepo.deleteByTenantAndSource(
                tenant, source
        );

        // Découper en chunks
        List<String> chunks = splitIntoChunks(
                text,
                properties.getRag().getChunkSize()
        );

        log.info("{} chunks a ingerer pour source: {}",
                chunks.size(), source);

        int saved = 0;
        for (String chunk : chunks) {

            // Ignorer les chunks trop courts
            if (chunk.trim().length() < 20) continue;

            KnowledgeChunk kc = new KnowledgeChunk();
            kc.setContenu(chunk.trim());
            kc.setSource(source);
            kc.setDocType(docType.name());
            kc.setTenant(tenant);
            kc.setCreatedAt(LocalDateTime.now());

            chunkRepo.save(kc);
            saved++;
        }

        log.info(
                "Ingestion terminee — {} chunks "
                        + "sauvegardes sur {} pour tenant: {}",
                saved, chunks.size(), tenant
        );
    }

    // ── Découpage en chunks ──
    private List<String> splitIntoChunks(
            String text,
            int chunkSize) {

        List<String> chunks = new ArrayList<>();

        // Nettoyer le texte
        String cleaned = text
                .replaceAll("\\s+", " ")
                .trim();

        String[] words = cleaned.split(" ");
        StringBuilder current = new StringBuilder();
        int count = 0;

        for (String word : words) {
            current.append(word).append(" ");
            count++;

            if (count >= chunkSize) {
                chunks.add(
                        current.toString().trim()
                );
                current = new StringBuilder();
                count = 0;
            }
        }

        // Dernier chunk
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }

    // ── Extraction texte PDF ──
    private String extractTextFromPdf(
            InputStream is) throws Exception {
        try (PDDocument doc = PDDocument.load(is)) {
            return new PDFTextStripper()
                    .getText(doc);
        }
    }
}