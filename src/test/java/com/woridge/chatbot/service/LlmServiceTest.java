package com.woridge.chatbot.service;

import com.woridge.chatbot.config
        .WoridgeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension
        .ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function
        .client.WebClient;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

    @Mock
    private WebClient groqWebClient;

    @Mock
    private WoridgeProperties properties;

    @InjectMocks
    private LlmService llmService;

    private Method detectLanguageMethod;

    @BeforeEach
    void setUp() throws Exception {
        // Accéder à la méthode privée
        // detectLanguage via réflexion
        detectLanguageMethod = LlmService.class
                .getDeclaredMethod(
                        "detectLanguage",
                        String.class
                );
        detectLanguageMethod.setAccessible(true);
    }

    private String detectLanguage(
            String question) throws Exception {
        return (String) detectLanguageMethod
                .invoke(llmService, question);
    }

    @Test
    @DisplayName(
            "Détecte le français par défaut"
    )
    void detectLanguage_french_default()
            throws Exception {
        String lang = detectLanguage(
                "mon solde de congé"
        );
        assertEquals("french", lang);
    }

    @Test
    @DisplayName(
            "Détecte l'arabe par les caractères"
    )
    void detectLanguage_arabic()
            throws Exception {
        String lang = detectLanguage(
                "رصيد إجازتي"
        );
        assertEquals("arabic", lang);
    }

    @Test
    @DisplayName(
            "Détecte l'arabe avec mix texte"
    )
    void detectLanguage_arabic_mixed()
            throws Exception {
        String lang = detectLanguage(
                "كم يوم للزواج"
        );
        assertEquals("arabic", lang);
    }

    @Test
    @DisplayName(
            "Détecte l'anglais avec 'my leave'"
    )
    void detectLanguage_english_myLeave()
            throws Exception {
        String lang = detectLanguage(
                "my leave balance"
        );
        assertEquals("english", lang);
    }

    @Test
    @DisplayName(
            "Détecte l'anglais avec 'how many'"
    )
    void detectLanguage_english_howMany()
            throws Exception {
        String lang = detectLanguage(
                "how many employees absent today"
        );
        assertEquals("english", lang);
    }

    @Test
    @DisplayName(
            "Détecte l'anglais avec 'show list'"
    )
    void detectLanguage_english_showList()
            throws Exception {
        String lang = detectLanguage(
                "show list of employees"
        );
        assertEquals("english", lang);
    }

    @Test
    @DisplayName(
            "Question vide retourne français"
    )
    void detectLanguage_empty_returnsFrench()
            throws Exception {
        String lang = detectLanguage("");
        assertEquals("french", lang);
    }

    @Test
    @DisplayName(
            "Question null retourne français"
    )
    void detectLanguage_null_returnsFrench()
            throws Exception {
        String lang = detectLanguage(null);
        assertEquals("french", lang);
    }

    @Test
    @DisplayName(
            "Français avec mot anglais isolé "
                    + "reste français"
    )
    void detectLanguage_frenchWithEnglishWord()
            throws Exception {
        // "my" isolé sans contexte anglais
        // doit être considéré comme français
        // car il n'y a pas d'espaces autour
        String lang = detectLanguage(
                "combien de jours"
        );
        assertEquals("french", lang);
    }
}