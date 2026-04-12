package com.woridge.chatbot.service;

import com.woridge.chatbot.enumeration.IntentType;
import com.woridge.chatbot.enumeration.UserRole;
import com.woridge.chatbot.exception.chat
        .AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PermissionService {

    public boolean isAllowed(
            IntentType intent,
            UserRole role) {

        boolean allowed = switch (intent) {

            case SOLDE_CONGE,
                 FICHE_PAIE      -> true;

            case REGLEMENTS_RH  -> true;

            case HORS_SCOPE     -> false;

            case STATS_PRESENCE,
                 STATS_CONGE_EQUIPE,
                 LISTE_EMPLOYES  -> role == UserRole.HR
                    || role == UserRole.MANAGER;

            case MASSE_SALARIALE -> role == UserRole.HR;
        };

        if (!allowed) {
            throw new AccessDeniedException(
                    "Intent: " + intent + " · Role: " + role
            );
        }

        return true;
    }

    public UserRole parseRole(String roleStr) {
        try {
            return UserRole.valueOf(
                    roleStr.toUpperCase().trim()
            );
        } catch (Exception e) {
            log.warn(
                    "Rôle inconnu : {} → défaut EMPLOYEE",
                    roleStr
            );
            return UserRole.EMPLOYEE;
        }
    }
}