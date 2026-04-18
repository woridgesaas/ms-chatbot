package com.woridge.chatbot.service;

import com.woridge.chatbot.model.KnowledgeChunk;
import com.woridge.chatbot.repository
        .KnowledgeChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final KnowledgeChunkRepository
            chunkRepo;
    private final EmbeddingService
            embeddingService;
    private final JdbcTemplate jdbcTemplate;

    // ── Point d'entrée principal ──
    public List<String> findRelevantChunks(
            String question,
            String tenant) {

        // Essayer pgvector d'abord
        List<String> pgResult =
                findWithPgVector(question, tenant);

        if (!pgResult.isEmpty()) {
            log.info(
                    "{} chunks via pgvector",
                    pgResult.size()
            );
            return pgResult;
        }

        // Fallback TF-IDF
        log.warn(
                "pgvector vide → fallback TF-IDF"
        );
        return findWithTfIdf(question, tenant);
    }

    // ── Recherche pgvector ──
    private List<String> findWithPgVector(
            String question,
            String tenant) {

        try {
            // Vectoriser la question
            float[] qVec =
                    embeddingService.embed(question);

            if (qVec == null) return List.of();

            String pgVector =
                    embeddingService
                            .toPostgresVector(qVec);

            // Recherche par similarité cosinus
            // top-k = 5 pour plus de contexte
            List<String> results =
                    jdbcTemplate.queryForList("""
                    SELECT contenu
                    FROM knowledge_base
                    WHERE tenant = ?
                      AND vecteur IS NOT NULL
                    ORDER BY vecteur <=> ?::vector
                    LIMIT 5
                    """,
                            String.class,
                            tenant,
                            pgVector
                    );

            log.info(
                    "pgvector : {} chunks trouvés",
                    results.size()
            );
            return results;

        } catch (Exception e) {
            log.error(
                    "Erreur pgvector : {}",
                    e.getMessage()
            );
            return List.of();
        }
    }

    // ── Fallback TF-IDF ──
    private List<String> findWithTfIdf(
            String question,
            String tenant) {

        List<KnowledgeChunk> chunks =
                chunkRepo.findAllByTenant(tenant);

        if (chunks.isEmpty()) {
            log.info(
                    "Aucun chunk pour tenant: {}",
                    tenant
            );
            return List.of();
        }

        log.info(
                "{} chunks TF-IDF pour tenant: {}",
                chunks.size(), tenant
        );

        List<String> contenus = chunks.stream()
                .map(KnowledgeChunk::getContenu)
                .collect(Collectors.toList());

        List<String> corpus = new ArrayList<>();
        corpus.add(question);
        corpus.addAll(contenus);

        Map<String, Double> idfMap =
                computeIDF(corpus);

        double[] qVec = computeTFIDFVector(
                question, idfMap
        );

        List<double[]> scored = new ArrayList<>();
        for (int i = 0;
             i < contenus.size(); i++) {
            double[] cVec = computeTFIDFVector(
                    contenus.get(i), idfMap
            );
            double sim =
                    cosineSimilarity(qVec, cVec);
            scored.add(new double[]{i, sim});
        }

        scored.sort((a, b) ->
                Double.compare(b[1], a[1])
        );

        List<String> result = scored.stream()
                .filter(s -> s[1] > 0.1)
                .limit(5)
                .map(s -> contenus.get((int) s[0]))
                .collect(Collectors.toList());

        log.info(
                "{} chunks TF-IDF pertinents",
                result.size()
        );

        return result;
    }

    // ── Calculer IDF ──
    private Map<String, Double> computeIDF(
            List<String> corpus) {

        Map<String, Integer> df =
                new HashMap<>();
        int N = corpus.size();

        for (String doc : corpus) {
            Set<String> words = new HashSet<>(
                    tokenize(doc)
            );
            for (String w : words) {
                df.merge(w, 1, Integer::sum);
            }
        }

        Map<String, Double> idf =
                new HashMap<>();
        for (Map.Entry<String, Integer> e
                : df.entrySet()) {
            idf.put(
                    e.getKey(),
                    Math.log(
                            (double)(N + 1)
                                    / (e.getValue() + 1)
                    ) + 1
            );
        }
        return idf;
    }

    // ── Calculer vecteur TF-IDF ──
    private double[] computeTFIDFVector(
            String doc,
            Map<String, Double> idfMap) {

        List<String> words = tokenize(doc);
        if (words.isEmpty()) {
            return new double[idfMap.size()];
        }

        Map<String, Integer> tf =
                new HashMap<>();
        for (String w : words) {
            tf.merge(w, 1, Integer::sum);
        }
        int total = words.size();

        List<String> vocab =
                new ArrayList<>(idfMap.keySet());
        double[] vec = new double[vocab.size()];

        for (int i = 0;
             i < vocab.size(); i++) {
            String word = vocab.get(i);
            if (tf.containsKey(word)) {
                double tfVal =
                        (double) tf.get(word)
                                / total;
                double idfVal =
                        idfMap.getOrDefault(
                                word, 1.0
                        );
                vec[i] = tfVal * idfVal;
            }
        }

        double norm = 0;
        for (double v : vec) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0;
                 i < vec.length; i++) {
                vec[i] /= norm;
            }
        }

        return vec;
    }

    // ── Similarité cosinus ──
    private double cosineSimilarity(
            double[] a,
            double[] b) {

        if (a.length != b.length) return 0;

        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) return 0;
        return dot / (
                Math.sqrt(normA) * Math.sqrt(normB)
        );
    }

    // ── Tokenizer français/arabe ──
    private List<String> tokenize(
            String text) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        String t = text.toLowerCase();

        // Remplacer accents français
        t = t.replace("é","e")
                .replace("è","e")
                .replace("ê","e")
                .replace("ë","e")
                .replace("à","a")
                .replace("â","a")
                .replace("ù","u")
                .replace("û","u")
                .replace("î","i")
                .replace("ï","i")
                .replace("ô","o")
                .replace("ç","c");

        // Supprimer ponctuation
        t = t.replaceAll(
                "[^a-z0-9\\s\\u0600-\\u06FF]", " "
        );

        Set<String> stop = Set.of(
                "le","la","les","de","du","des",
                "un","une","et","en","au","aux",
                "est","sont","que","qui","pour",
                "par","sur","dans","avec","ce",
                "se","sa","son","ses","ne","pas",
                "the","a","an","is","are","of",
                "to","in","for","on","with","my"
        );

        return Arrays.stream(t.split("\\s+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !stop.contains(w))
                .collect(Collectors.toList());
    }
}