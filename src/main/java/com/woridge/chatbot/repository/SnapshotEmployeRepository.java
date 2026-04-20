package com.woridge.chatbot.repository;

import com.woridge.chatbot.model.SnapshotEmploye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnapshotEmployeRepository
        extends JpaRepository<SnapshotEmploye, String> {

    Optional<SnapshotEmploye> findByUserId(
            String userId
    );

    // JpaRepository fournit déjà :
    //   findAll()  → tous les employés
    //   count()    → total
    // Pas besoin de les redéfinir
}