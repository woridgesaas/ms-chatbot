package com.woridge.chatbot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stats_conge")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant", nullable = false)
    private String tenant;

    @Column(name = "mois", nullable = false)
    private LocalDate mois;

    // ── Présence ──
    @Column(name = "nb_en_conge")
    private int nbEnConge;

    @Column(name = "nb_absents")
    private int nbAbsents;

    @Column(name = "nb_total_employes")
    private int nbTotalEmployes;

    // ── Types de congé ──
    @Column(name = "nb_conge_annuel")
    private int nbCongeAnnuel;

    @Column(name = "nb_conge_maladie")
    private int nbCongeMaladie;

    @Column(name = "nb_conge_maternite")
    private int nbCongeMaternite;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}