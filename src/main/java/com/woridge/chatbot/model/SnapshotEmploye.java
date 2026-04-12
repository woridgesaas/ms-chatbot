package com.woridge.chatbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "snapshot_employe")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotEmploye {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "departement")
    private String departement;

    @Column(name = "poste")
    private String poste;

    @Column(
            name = "salaire_actuel",
            columnDefinition = "DECIMAL(10,2)"
    )
    private BigDecimal salaireActuel;

    @Column(name = "mois_salaire")
    private String moisSalaire;

    @Column(name = "a_demissionne")
    private Boolean aDemissionne;

    @Column(name = "date_demission")
    private LocalDate dateDemission;

    @Column(name = "solde_conge")
    private Integer soldeConge;

    @Column(name = "tenant", nullable = false)
    private String tenant;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}