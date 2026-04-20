package com.woridge.chatbot.repository;

import com.woridge.chatbot.model.KnowledgeChunk;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeChunkRepository
        extends JpaRepository<KnowledgeChunk, Long> {

    // JpaRepository fournit déjà findAll()
    // Pas besoin de findAllByTenant

    void deleteBySource(String source);
}