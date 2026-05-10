package com.agrifund.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire d'Emojis
 * Permet d'insérer des emojis dans les messages
 */
public class EmojiManager {

    private static final Map<String, String> EMOJIS = new HashMap<>();

    static {
        // Emojis de base
        EMOJIS.put(":)", "😊");
        EMOJIS.put(":(", "😞");
        EMOJIS.put(":D", "😃");
        EMOJIS.put(";)", "😉");
        EMOJIS.put(":P", "😛");
        EMOJIS.put("<3", "❤️");
        EMOJIS.put(":*", "😘");

        // Emojis avec codes
        EMOJIS.put(":smile:", "😊");
        EMOJIS.put(":laugh:", "😄");
        EMOJIS.put(":sad:", "😢");
        EMOJIS.put(":cry:", "😭");
        EMOJIS.put(":angry:", "😠");
        EMOJIS.put(":love:", "❤️");
        EMOJIS.put(":heart:", "💖");
        EMOJIS.put(":like:", "👍");
        EMOJIS.put(":dislike:", "👎");
        EMOJIS.put(":ok:", "👌");
        EMOJIS.put(":fire:", "🔥");
        EMOJIS.put(":star:", "⭐");
        EMOJIS.put(":check:", "✅");
        EMOJIS.put(":cross:", "❌");
        EMOJIS.put(":warning:", "⚠️");
        EMOJIS.put(":info:", "ℹ️");
        EMOJIS.put(":question:", "❓");
        EMOJIS.put(":exclamation:", "❗");

        // Emojis nature
        EMOJIS.put(":sun:", "☀️");
        EMOJIS.put(":moon:", "🌙");
        EMOJIS.put(":cloud:", "☁️");
        EMOJIS.put(":rain:", "🌧️");
        EMOJIS.put(":tree:", "🌳");
        EMOJIS.put(":flower:", "🌸");
        EMOJIS.put(":leaf:", "🍃");

        // Emojis animaux
        EMOJIS.put(":cat:", "🐱");
        EMOJIS.put(":dog:", "🐶");
        EMOJIS.put(":bird:", "🐦");
        EMOJIS.put(":cow:", "🐮");
        EMOJIS.put(":horse:", "🐴");

        // Emojis nourriture
        EMOJIS.put(":coffee:", "☕");
        EMOJIS.put(":pizza:", "🍕");
        EMOJIS.put(":burger:", "🍔");
        EMOJIS.put(":cake:", "🎂");
        EMOJIS.put(":apple:", "🍎");
        EMOJIS.put(":banana:", "🍌");

        // Emojis activités
        EMOJIS.put(":car:", "🚗");
        EMOJIS.put(":bike:", "🚲");
        EMOJIS.put(":plane:", "✈️");
        EMOJIS.put(":phone:", "📱");
        EMOJIS.put(":computer:", "💻");
        EMOJIS.put(":book:", "📖");
        EMOJIS.put(":pen:", "✏️");

        // Emojis symboles
        EMOJIS.put(":time:", "⏰");
        EMOJIS.put(":money:", "💰");
        EMOJIS.put(":gift:", "🎁");
        EMOJIS.put(":bell:", "🔔");
        EMOJIS.put(":lock:", "🔒");
        EMOJIS.put(":key:", "🔑");

        // Emojis agriculture (pour l'app)
        EMOJIS.put(":farm:", "🌾");
        EMOJIS.put(":tractor:", "🚜");
        EMOJIS.put(":seed:", "🌱");
        EMOJIS.put(":harvest:", "🌾");
    }

    /**
     * Convertir les codes emoji en emojis Unicode
     */
    public static String convertirEmojis(String texte) {
        if (texte == null || texte.isEmpty()) {
            return texte;
        }

        String resultat = texte;

        // Remplacer tous les codes emoji
        for (Map.Entry<String, String> entry : EMOJIS.entrySet()) {
            resultat = resultat.replace(entry.getKey(), entry.getValue());
        }

        return resultat;
    }

    /**
     * Afficher la liste des emojis disponibles
     */
    public static void afficherListeEmojis() {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                  EMOJIS DISPONIBLES                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");

        System.out.println("\n😊 EXPRESSIONS");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{":)", ":(", ":D", ";)", ":P", ":*",
                ":smile:", ":laugh:", ":sad:", ":cry:", ":angry:"});

        System.out.println("\n❤️ ÉMOTIONS & SYMBOLES");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{"<3", ":love:", ":heart:", ":like:", ":dislike:",
                ":ok:", ":fire:", ":star:"});

        System.out.println("\n✅ STATUTS");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{":check:", ":cross:", ":warning:", ":info:",
                ":question:", ":exclamation:"});

        System.out.println("\n🌳 NATURE");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{":sun:", ":moon:", ":cloud:", ":rain:",
                ":tree:", ":flower:", ":leaf:"});

        System.out.println("\n🐱 ANIMAUX");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{":cat:", ":dog:", ":bird:", ":cow:", ":horse:"});

        System.out.println("\n🍕 NOURRITURE");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{":coffee:", ":pizza:", ":burger:", ":cake:",
                ":apple:", ":banana:"});

        System.out.println("\n🚜 AGRICULTURE");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{":farm:", ":tractor:", ":seed:", ":harvest:"});

        System.out.println("\n💻 OBJETS");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{":car:", ":bike:", ":plane:", ":phone:",
                ":computer:", ":book:", ":pen:"});

        System.out.println("\n💰 SYMBOLES");
        System.out.println("─────────────────────────────────────────────────────────");
        afficherCategorie(new String[]{":time:", ":money:", ":gift:", ":bell:",
                ":lock:", ":key:"});

        System.out.println("\n💡 ASTUCE: Tapez le code (ex: :smile:) dans votre message");
        System.out.println("           Il sera automatiquement converti en emoji!");
        System.out.println();
    }

    /**
     * Afficher une catégorie d'emojis
     */
    private static void afficherCategorie(String[] codes) {
        int count = 0;
        for (String code : codes) {
            String emoji = EMOJIS.get(code);
            if (emoji != null) {
                System.out.print(String.format("%-15s %s    ", code, emoji));
                count++;
                if (count % 3 == 0) {
                    System.out.println();
                }
            }
        }
        if (count % 3 != 0) {
            System.out.println();
        }
    }

    /**
     * Ajouter un emoji personnalisé
     */
    public static void ajouterEmoji(String code, String emoji) {
        EMOJIS.put(code, emoji);
    }

    /**
     * Vérifier si un code emoji existe
     */
    public static boolean emojiExiste(String code) {
        return EMOJIS.containsKey(code);
    }

    /**
     * Obtenir l'emoji correspondant à un code
     */
    public static String getEmoji(String code) {
        return EMOJIS.getOrDefault(code, code);
    }
}
