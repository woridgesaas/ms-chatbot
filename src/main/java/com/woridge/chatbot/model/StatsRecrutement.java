package com.woridge.chatbot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stats_recrutement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsRecrutement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mois", nullable = false)
    private LocalDate mois;

    // ── Offres ──
    @Column(name = "nb_offres_publiees")
    private int nbOffresPubliees;

    @Column(name = "nb_offres_actives")
    private int nbOffresActives;

    // ── Candidatures ──
    @Column(name = "nb_candidatures")
    private int nbCandidatures;

    @Column(name = "nb_candidatures_en_attente")
    private int nbCandidaturesEnAttente;

    // ── Entretiens ──
    @Column(name = "nb_entretiens_planifies")
    private int nbEntretiensPlanifies;

    @Column(name = "nb_entretiens_effectues")
    private int nbEntretiensEffectues;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}