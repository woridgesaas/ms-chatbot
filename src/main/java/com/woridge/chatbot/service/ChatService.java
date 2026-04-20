package com.woridge.chatbot.service;

import com.woridge.chatbot.config.WoridgeProperties;
import com.woridge.chatbot.dto.request.ChatRequest;
import com.woridge.chatbot.dto.response.ChatResponse;
import com.woridge.chatbot.dto.response.IntentResult;
import com.woridge.chatbot.enumeration.IntentType;
import com.woridge.chatbot.enumeration.UserRole;
import com.woridge.chatbot.exception.data
        .EmployeeNotFoundException;
import com.woridge.chatbot.exception.data
        .StatsNotAvailableException;
import com.woridge.chatbot.repository
        .SnapshotEmployeRepository;
import com.woridge.chatbot.repository
        .StatsCongeRepository;
import com.woridge.chatbot.repository
        .StatsRecrutementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ClassifierService classifierService;
    private final PermissionService permissionService;
    private final RagService ragService;
    private final LlmService llmService;
    private final SnapshotEmployeRepository employeRepo;
    private final StatsCongeRepository congeRepo;
    private final StatsRecrutementRepository recrutRepo;
    private final WoridgeProperties properties;

    public ChatResponse chat(ChatRequest request) {

        log.info(
                "Question — user: {}",
                request.getUserId()
        );

        // ── Étape 1 : Classifier ──
        IntentResult intentResult = classifierService
                .classify(request.getQuestion());

        IntentType intent     = intentResult.getIntent();
        double     confidence = intentResult.getConfidence();

        log.info("Intent: {} confidence: {}",
                intent, confidence);

        // ── Étape 2 : Permissions ──
        UserRole role = permissionService
                .parseRole(request.getRole());

        permissionService.isAllowed(intent, role);

        // ── Étape 3 : Données ──
        String data = fetchData(
                intent,
                request.getUserId()
        );

        // ── Étape 4 : RAG si besoin ──
        // REGLEMENTS_RH → toujours RAG
        // confidence < seuil → question ambiguë → RAG
        List<String> ragChunks = List.of();
        if (intent == IntentType.REGLEMENTS_RH
                || confidence < properties
                .getRag().getConfidenceThreshold()) {
            ragChunks = ragService.findRelevantChunks(
                    request.getQuestion()
            );
        }

        // ── Étape 5 : Générer réponse ──
        String answer = llmService.generateResponse(
                request.getQuestion(),
                data,
                ragChunks
        );

        return ChatResponse.builder()
                .answer(answer)
                .intent(intent.name())
                .confidence(confidence)
                .build();
    }

    private String fetchData(
            IntentType intent,
            String userId) {

        LocalDate moisCourant =
                LocalDate.now().withDayOfMonth(1);

        return switch (intent) {

            // ── Données personnelles employé ──

            case SOLDE_CONGE -> {
                var emp = employeRepo
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        "Employé non trouvé : "
                                                + userId
                                ));
                yield String.format(
                        "Employé: %s %s · "
                                + "Solde congé: %d jours restants",
                        emp.getNom(),
                        emp.getPrenom(),
                        emp.getSoldeConge()
                );
            }

            case FICHE_PAIE -> {
                var emp = employeRepo
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        "Employé non trouvé : "
                                                + userId
                                ));
                yield String.format(
                        "Employé: %s %s · "
                                + "Salaire %s: %.2f MAD",
                        emp.getNom(),
                        emp.getPrenom(),
                        emp.getMoisSalaire() != null
                                ? emp.getMoisSalaire()
                                : "ce mois",
                        emp.getSalaireActuel() != null
                                ? emp.getSalaireActuel()
                                : 0.0
                );
            }

            // ── Statistiques présence ──

            case STATS_PRESENCE -> {
                var stats = congeRepo
                        .findByMois(moisCourant)
                        .orElseThrow(() ->
                                new StatsNotAvailableException(
                                        "Stats présence non "
                                                + "disponibles"
                                ));
                yield String.format(
                        "Présence ce mois : "
                                + "%d présents · "
                                + "%d absents · "
                                + "%d total employés · "
                                + "Taux présence: %.1f%%",
                        stats.getNbTotalEmployes()
                                - stats.getNbAbsents(),
                        stats.getNbAbsents(),
                        stats.getNbTotalEmployes(),
                        stats.getNbTotalEmployes() > 0
                                ? (double)(
                                stats.getNbTotalEmployes()
                                        - stats.getNbAbsents()
                        ) / stats.getNbTotalEmployes()
                                * 100
                                : 0.0
                );
            }

            // ── Statistiques congés équipe ──

            case STATS_CONGE_EQUIPE -> {
                var stats = congeRepo
                        .findByMois(moisCourant)
                        .orElseThrow(() ->
                                new StatsNotAvailableException(
                                        "Stats congés non "
                                                + "disponibles"
                                ));
                yield String.format(
                        "Congés ce mois : "
                                + "%d en congé · "
                                + "%d congé annuel · "
                                + "%d congé maladie · "
                                + "%d congé maternité",
                        stats.getNbEnConge(),
                        stats.getNbCongeAnnuel(),
                        stats.getNbCongeMaladie(),
                        stats.getNbCongeMaternite()
                );
            }

            // ── Liste et effectif ──

            case LISTE_EMPLOYES -> {
                long count = employeRepo.count();
                var emps = employeRepo.findAll();
                long actifs = emps.stream()
                        .filter(e ->
                                e.getADemissionne() == null
                                        || !e.getADemissionne())
                        .count();
                yield String.format(
                        "Effectif total: %d employés · "
                                + "%d actifs · "
                                + "%d démissionnaires",
                        count,
                        actifs,
                        count - actifs
                );
            }

            // ── Masse salariale ──

            case MASSE_SALARIALE -> {
                var emps = employeRepo.findAll();
                double total = emps.stream()
                        .filter(e ->
                                e.getSalaireActuel() != null)
                        .mapToDouble(e ->
                                e.getSalaireActuel()
                                        .doubleValue())
                        .sum();
                long nbSalaries = emps.stream()
                        .filter(e ->
                                e.getSalaireActuel() != null)
                        .count();
                yield String.format(
                        "Masse salariale: %.2f MAD · "
                                + "%d salariés concernés · "
                                + "Moyenne: %.2f MAD",
                        total,
                        nbSalaries,
                        nbSalaries > 0
                                ? total / nbSalaries
                                : 0.0
                );
            }

            // ── Règlement — données depuis RAG ──

            case REGLEMENTS_RH ->
                    "Consultation règlement intérieur Woridge";

            // ── Hors scope ──

            case HORS_SCOPE ->
                    "Question hors périmètre RH Woridge";
        };
    }
}