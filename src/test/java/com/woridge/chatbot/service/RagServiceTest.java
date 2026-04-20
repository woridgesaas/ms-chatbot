package com.woridge.chatbot.service;

import com.woridge.chatbot.model.KnowledgeChunk;
import com.woridge.chatbot.repository
        .KnowledgeChunkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension
        .ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core
        .JdbcTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private KnowledgeChunkRepository chunkRepo;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RagService ragService;

    @Test
    @DisplayName(
            "pgvector retourne chunks trouvés"
    )
    void findRelevantChunks_pgvector_success() {

        // ── Mock embedding ──
        float[] vec = new float[384];
        for (int i = 0; i < 384; i++) {
            vec[i] = 0.1f;
        }
        when(embeddingService
                .embed("combien de jours mariage"))
                .thenReturn(vec);
        when(embeddingService
                .toPostgresVector(vec))
                .thenReturn("[0.1,0.1,...]");

        // ── Mock pgvector SQL ──
        // 1 seul paramètre maintenant (pgVector)
        List<String> mockChunks = Arrays.asList(
                "Mariage du salarié 4 jours",
                "Justificatif : acte de mariage",
                "Congés exceptionnels"
        );
        when(jdbcTemplate.queryForList(
                anyString(),
                eq(String.class),
                anyString()
        )).thenReturn(mockChunks);

        // ── When ──
        List<String> result =
                ragService.findRelevantChunks(
                        "combien de jours mariage"
                );

        // ── Then ──
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.get(0)
                .contains("Mariage"));
    }

    @Test
    @DisplayName(
            "pgvector vide → fallback TF-IDF"
    )
    void findRelevantChunks_fallback_tfidf() {

        // ── Mock embedding ──
        float[] vec = new float[384];
        when(embeddingService
                .embed(anyString()))
                .thenReturn(vec);
        when(embeddingService
                .toPostgresVector(vec))
                .thenReturn("[...]");

        // ── Mock pgvector retourne vide ──
        when(jdbcTemplate.queryForList(
                anyString(),
                eq(String.class),
                anyString()
        )).thenReturn(List.of());

        // ── Mock TF-IDF fallback ──
        KnowledgeChunk chunk1 =
                new KnowledgeChunk();
        chunk1.setContenu(
                "Mariage du salarié 4 jours"
        );

        KnowledgeChunk chunk2 =
                new KnowledgeChunk();
        chunk2.setContenu(
                "Procédure de démission"
        );

        when(chunkRepo.findAll())
                .thenReturn(
                        Arrays.asList(
                                chunk1, chunk2
                        )
                );

        // ── When ──
        List<String> result =
                ragService.findRelevantChunks(
                        "combien de jours mariage"
                );

        // ── Then ──
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName(
            "Aucun chunk → liste vide"
    )
    void findRelevantChunks_noChunks_empty() {

        // ── Mock embedding ──
        float[] vec = new float[384];
        when(embeddingService
                .embed(anyString()))
                .thenReturn(vec);
        when(embeddingService
                .toPostgresVector(vec))
                .thenReturn("[...]");

        // ── Mock pgvector vide ──
        when(jdbcTemplate.queryForList(
                anyString(),
                eq(String.class),
                anyString()
        )).thenReturn(List.of());

        // ── Mock TF-IDF vide aussi ──
        when(chunkRepo.findAll())
                .thenReturn(List.of());

        // ── When ──
        List<String> result =
                ragService.findRelevantChunks(
                        "quelque chose"
                );

        // ── Then ──
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName(
            "Embedding fail → fallback TF-IDF"
    )
    void findRelevantChunks_embedFail_tfidf() {

        // ── Embedding retourne null ──
        when(embeddingService
                .embed(anyString()))
                .thenReturn(null);

        // ── Mock chunks TF-IDF ──
        KnowledgeChunk chunk =
                new KnowledgeChunk();
        chunk.setContenu(
                "Mariage 4 jours règlement"
        );
        when(chunkRepo.findAll())
                .thenReturn(List.of(chunk));

        // ── When ──
        List<String> result =
                ragService.findRelevantChunks(
                        "mariage"
                );

        // ── Then ──
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}