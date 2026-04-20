package com.woridge.chatbot.repository;

import com.woridge.chatbot.model.StatsRecrutement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StatsRecrutementRepository
        extends JpaRepository<StatsRecrutement, Long> {

    Optional<StatsRecrutement> findByMois(
            LocalDate mois
    );
}