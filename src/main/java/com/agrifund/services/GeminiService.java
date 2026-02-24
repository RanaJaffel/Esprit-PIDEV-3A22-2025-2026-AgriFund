package com.agrifund.services;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import io.github.cdimascio.dotenv.Dotenv;

public class GeminiService {

    private final Client client;
    private static final String MODEL = "gemini-3-flash-preview";

    public GeminiService() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_api_key_here")) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY non configuree. Veuillez ajouter votre cle API dans le fichier .env");
        }

        this.client = Client.builder().apiKey(apiKey).build();
    }

    /**
     * Genere du texte a partir d'un prompt
     */
    public String genererTexte(String prompt) {
        try {
            GenerateContentResponse response = client.models.generateContent(MODEL, prompt, null);
            return response.text();
        } catch (Exception e) {
            System.err.println("Erreur Gemini API: " + e.getMessage());
            return "Erreur lors de la generation: " + e.getMessage();
        }
    }

    /**
     * Genere des conseils agricoles pour un produit financier
     */
    public String conseillerProduit(String nomProduit, String typeFinancement, double tauxInteret) {
        String prompt = String.format(
                "En tant qu'expert en financement agricole, donne un conseil concis (3-4 phrases) " +
                "sur le produit financier suivant:\n" +
                "- Nom: %s\n- Type: %s\n- Taux d'interet: %.2f%%\n" +
                "Reponds en francais.",
                nomProduit, typeFinancement, tauxInteret);
        return genererTexte(prompt);
    }
}
