package com.agrifund.services;

import java.util.ArrayList;
import java.util.List;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Service de chatbot IA limite au contexte du projet AgriFund. Utilise Gemini
 * pour repondre uniquement aux questions liees au financement agricole,
 * produits financiers, et la plateforme AgriFund.
 */
public class ChatbotService {

    private final Client client;
    private static final String MODEL = "gemini-2.5-flash";
    private final List<String> conversationHistory;

    private static final String SYSTEM_PROMPT
            = "Tu es l'assistant IA d'AgriFund, une plateforme de gestion des produits financiers agricoles en Tunisie. "
            + "Tu dois UNIQUEMENT repondre aux questions liees a:\n"
            + "- Les produits financiers agricoles (credits, prets, microfinance)\n"
            + "- Les offres financieres pour les agriculteurs\n"
            + "- Le financement agricole en Tunisie\n"
            + "- Les exploitations agricoles (oleiculture, cereales, dattes, agrumes, elevage)\n"
            + "- Les conseils pour les agriculteurs sur le financement\n"
            + "- L'utilisation de la plateforme AgriFund (navigation, fonctionnalites)\n"
            + "- Les regions agricoles de Tunisie (Cap Bon, Sahel, Beja, Kasserine, Gafsa, Tozeur, Jendouba)\n"
            + "- Les bureaux AgriFund (Tunis, Sousse, Sfax, Nabeul, Beja, Tozeur)\n\n"
            + "Si la question n'est PAS liee a ces sujets, reponds poliment que tu ne peux aider "
            + "que sur des sujets lies au financement agricole et a la plateforme AgriFund.\n\n"
            + "Reponds toujours en francais, de maniere concise et utile. "
            + "Utilise des emojis agricoles quand c'est pertinent (🌾🌿🚜💰📊).";

    public ChatbotService() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_api_key_here")) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY non configuree. Veuillez ajouter votre cle API dans le fichier .env");
        }

        this.client = Client.builder().apiKey(apiKey).build();
        this.conversationHistory = new ArrayList<>();
    }

    /**
     * Envoie un message au chatbot et retourne la reponse. Le contexte de la
     * conversation est maintenu.
     */
    public String sendMessage(String userMessage) {
        try {
            conversationHistory.add("Utilisateur: " + userMessage);

            // Build context with conversation history (last 10 exchanges max)
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append(SYSTEM_PROMPT).append("\n\n");
            contextBuilder.append("Historique de la conversation:\n");

            int startIdx = Math.max(0, conversationHistory.size() - 20);
            for (int i = startIdx; i < conversationHistory.size(); i++) {
                contextBuilder.append(conversationHistory.get(i)).append("\n");
            }
            contextBuilder.append("\nReponds au dernier message de l'utilisateur:");

            GenerateContentResponse response = client.models.generateContent(MODEL, contextBuilder.toString(), null);
            String reply = response.text();

            conversationHistory.add("Assistant: " + reply);
            return reply;

        } catch (Exception e) {
            System.err.println("Erreur Chatbot Gemini: " + e.getMessage());
            return "❌ Desolee, une erreur s'est produite. Veuillez reessayer.";
        }
    }

    /**
     * Reinitialise l'historique de la conversation.
     */
    public void clearHistory() {
        conversationHistory.clear();
    }

    /**
     * Retourne un message d'accueil.
     */
    public String getWelcomeMessage() {
        return "🌾 Bonjour! Je suis l'assistant AgriFund.\n\n"
                + "Je peux vous aider avec:\n"
                + "• 💰 Les produits financiers agricoles\n"
                + "• 📊 Les offres de financement\n"
                + "• 🚜 Les conseils pour agriculteurs\n"
                + "• 🗺️ Les bureaux et exploitations\n"
                + "• ❓ L'utilisation de la plateforme\n\n"
                + "Comment puis-je vous aider?";
    }
}
