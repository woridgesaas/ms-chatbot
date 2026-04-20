package com.woridge.chatbot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "knowledge_base")
public class KnowledgeChunk {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            columnDefinition = "TEXT",
            nullable = false
    )
    private String contenu;

    // insertable=false → JPA ne touche pas
    // vecteur est géré par jdbcTemplate
    @Column(
            name = "vecteur",
            insertable = false,
            updatable = false
    )
    private String vecteur;

    private String source;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}