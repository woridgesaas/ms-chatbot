package com.woridge.chatbot.service;

import com.woridge.chatbot.config
        .WoridgeProperties;
import com.woridge.chatbot.dto.request
        .ChatRequest;
import com.woridge.chatbot.dto.response
        .ChatResponse;
import com.woridge.chatbot.dto.response
        .IntentResult;
import com.woridge.chatbot.enumeration.IntentType;
import com.woridge.chatbot.enumeration.UserRole;
import com.woridge.chatbot.exception.data
        .EmployeeNotFoundException;
import com.woridge.chatbot.model
        .SnapshotEmploye;
import com.woridge.chatbot.repository
        .SnapshotEmployeRepository;
import com.woridge.chatbot.repository
        .StatsCongeRepository;
import com.woridge.chatbot.repository
        .StatsRecrutementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension
        .ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ClassifierService classifierService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private RagService ragService;

    @Mock
    private LlmService llmService;

    @Mock
    private SnapshotEmployeRepository employeRepo;

    @Mock
    private StatsCongeRepository congeRepo;

    @Mock
    private StatsRecrutementRepository recrutRepo;

    @Mock
    private WoridgeProperties properties;

    @Mock
    private WoridgeProperties.Rag ragProperties;

    @InjectMocks
    private ChatService chatService;

    private ChatRequest request;

    @BeforeEach
    void setUp() {
        // Requête de base pour les tests
        request = new ChatRequest();
        request.setQuestion(
                "mon solde de congé"
        );
        request.setUserId("S760251");
        request.setRole("EMPLOYEE");

        // Mock properties RAG
        lenient().when(properties.getRag())
                .thenReturn(ragProperties);
        lenient().when(
                ragProperties.getConfidenceThreshold()
        ).thenReturn(0.45);
    }

    @Test
    @DisplayName(
            "SOLDE_CONGE retourne solde employé"
    )
    void chat_soldeConge_returnsCorrectAnswer() {

        // ── Mock classifier ──
        IntentResult intentResult =
                new IntentResult();
        intentResult.setIntent(
                IntentType.SOLDE_CONGE
        );
        intentResult.setConfidence(0.98);
        when(classifierService.classify(
                "mon solde de congé"
        )).thenReturn(intentResult);

        // ── Mock permission ──
        when(permissionService
                .parseRole("EMPLOYEE"))
                .thenReturn(UserRole.EMPLOYEE);
        when(permissionService.isAllowed(
                IntentType.SOLDE_CONGE,
                UserRole.EMPLOYEE
        )).thenReturn(true);

        // ── Mock employee ──
        SnapshotEmploye emp =
                new SnapshotEmploye();
        emp.setUserId("S760251");
        emp.setNom("Ammari");
        emp.setPrenom("Imane");
        emp.setSoldeConge(18);

        when(employeRepo.findByUserId("S760251"))
                .thenReturn(Optional.of(emp));

        // ── Mock LLM ──
        when(llmService.generateResponse(
                anyString(),
                anyString(),
                anyList()
        )).thenReturn(
                "Vous avez 18 jours de congé"
        );

        // ── When ──
        ChatResponse response =
                chatService.chat(request);

        // ── Then ──
        assertNotNull(response);
        assertEquals(
                "SOLDE_CONGE",
                response.getIntent()
        );
        assertEquals(
                0.98,
                response.getConfidence(),
                0.01
        );
        assertTrue(
                response.getAnswer()
                        .contains("18")
        );

        // Vérifier les appels
        verify(classifierService, times(1))
                .classify("mon solde de congé");
        verify(employeRepo, times(1))
                .findByUserId("S760251");
        verify(llmService, times(1))
                .generateResponse(
                        anyString(),
                        anyString(),
                        anyList()
                );
    }

    @Test
    @DisplayName(
            "EmployeeNotFound lance exception"
    )
    void chat_employeeNotFound_throwsException() {

        // ── Mock classifier ──
        IntentResult intentResult =
                new IntentResult();
        intentResult.setIntent(
                IntentType.SOLDE_CONGE
        );
        intentResult.setConfidence(0.98);
        when(classifierService.classify(
                anyString()
        )).thenReturn(intentResult);

        // ── Mock permission ──
        when(permissionService
                .parseRole(anyString()))
                .thenReturn(UserRole.EMPLOYEE);
        when(permissionService.isAllowed(
                any(), any()
        )).thenReturn(true);

        // ── Employee NOT found ──
        when(employeRepo.findByUserId(
                anyString()
        )).thenReturn(Optional.empty());

        // ── Then : exception ──
        assertThrows(
                EmployeeNotFoundException.class,
                () -> chatService.chat(request)
        );
    }

    @Test
    @DisplayName(
            "REGLEMENTS_RH déclenche RAG"
    )
    void chat_reglementsRH_usesRag() {

        request.setQuestion(
                "combien de jours mariage"
        );

        // ── Mock classifier ──
        IntentResult intentResult =
                new IntentResult();
        intentResult.setIntent(
                IntentType.REGLEMENTS_RH
        );
        intentResult.setConfidence(0.97);
        when(classifierService.classify(
                anyString()
        )).thenReturn(intentResult);

        // ── Mock permission ──
        when(permissionService
                .parseRole(anyString()))
                .thenReturn(UserRole.EMPLOYEE);
        when(permissionService.isAllowed(
                any(), any()
        )).thenReturn(true);

        // ── Mock RAG chunks ──
        List<String> chunks = List.of(
                "Mariage du salarié 4 jours",
                "Justificatif : acte de mariage"
        );
        when(ragService.findRelevantChunks(
                anyString()
        )).thenReturn(chunks);

        // ── Mock LLM ──
        when(llmService.generateResponse(
                anyString(),
                anyString(),
                anyList()
        )).thenReturn(
                "4 jours pour le mariage"
        );

        // ── When ──
        ChatResponse response =
                chatService.chat(request);

        // ── Then ──
        assertNotNull(response);
        assertEquals(
                "REGLEMENTS_RH",
                response.getIntent()
        );

        // Vérifier que RAG a été appelé
        verify(ragService, times(1))
                .findRelevantChunks(
                        "combien de jours mariage"
                );
    }

    @Test
    @DisplayName(
            "SOLDE_CONGE ne déclenche PAS RAG"
    )
    void chat_soldeConge_doesNotUseRag() {

        // ── Mock classifier ──
        IntentResult intentResult =
                new IntentResult();
        intentResult.setIntent(
                IntentType.SOLDE_CONGE
        );
        intentResult.setConfidence(0.98);
        when(classifierService.classify(
                anyString()
        )).thenReturn(intentResult);

        // ── Mock permission ──
        when(permissionService
                .parseRole(anyString()))
                .thenReturn(UserRole.EMPLOYEE);
        when(permissionService.isAllowed(
                any(), any()
        )).thenReturn(true);

        // ── Mock employee ──
        SnapshotEmploye emp =
                new SnapshotEmploye();
        emp.setUserId("S760251");
        emp.setNom("Ammari");
        emp.setPrenom("Imane");
        emp.setSoldeConge(18);
        when(employeRepo.findByUserId(
                anyString()
        )).thenReturn(Optional.of(emp));

        // ── Mock LLM ──
        when(llmService.generateResponse(
                anyString(),
                anyString(),
                anyList()
        )).thenReturn("réponse");

        // ── When ──
        chatService.chat(request);

        // ── Then ──
        // RAG NE doit PAS être appelé
        verify(ragService, never())
                .findRelevantChunks(
                        anyString()
                );
    }
}