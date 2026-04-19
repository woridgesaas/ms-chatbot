package com.woridge.chatbot.service;

import com.woridge.chatbot.enumeration.IntentType;
import com.woridge.chatbot.enumeration.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionServiceTest {

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService =
                new PermissionService();
    }

    @Test
    @DisplayName(
            "EMPLOYEE peut voir son solde"
    )
    void employee_canAccess_soldeConge() {
        IntentType intent =
                IntentType.SOLDE_CONGE;
        UserRole role = UserRole.EMPLOYEE;

        boolean allowed =
                permissionService.isAllowed(
                        intent, role
                );

        assertTrue(allowed);
    }

    @Test
    @DisplayName(
            "EMPLOYEE NE peut PAS voir masse "
                    + "salariale — lance exception"
    )
    void employee_cannotAccess_masseSalariale() {
        IntentType intent =
                IntentType.MASSE_SALARIALE;
        UserRole role = UserRole.EMPLOYEE;

        // Vérifie que l'exception est bien lancée
        assertThrows(
                com.woridge.chatbot.exception.chat
                        .AccessDeniedException.class,
                () -> permissionService.isAllowed(
                        intent, role
                )
        );
    }

    @Test
    @DisplayName(
            "HR peut voir masse salariale"
    )
    void hr_canAccess_masseSalariale() {
        IntentType intent =
                IntentType.MASSE_SALARIALE;
        UserRole role = UserRole.HR;

        boolean allowed =
                permissionService.isAllowed(
                        intent, role
                );

        assertTrue(allowed);
    }

    @Test
    @DisplayName(
            "ADMIN peut tout voir"
    )
    void admin_canAccess_all() {
        UserRole role = UserRole.HR;

        assertTrue(
                permissionService.isAllowed(
                        IntentType.SOLDE_CONGE,
                        role
                )
        );
        assertTrue(
                permissionService.isAllowed(
                        IntentType.MASSE_SALARIALE,
                        role
                )
        );
        assertTrue(
                permissionService.isAllowed(
                        IntentType.LISTE_EMPLOYES,
                        role
                )
        );
    }
}