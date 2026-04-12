package com.woridge.chatbot.repository;

import com.woridge.chatbot.model.StatsConge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StatsCongeRepository
        extends JpaRepository<StatsConge, Long> {

    Optional<StatsConge> findByTenantAndMois(
            String tenant, LocalDate mois
    );
}