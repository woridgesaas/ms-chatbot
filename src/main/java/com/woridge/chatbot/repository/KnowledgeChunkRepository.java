package com.woridge.chatbot.repository;

import com.woridge.chatbot.model.KnowledgeChunk;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeChunkRepository
        extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk> findAllByTenant(
            String tenant
    );

    void deleteByTenantAndSource(
            String tenant,
            String source
    );
}