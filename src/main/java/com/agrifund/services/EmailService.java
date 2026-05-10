package com.agrifund.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    // ═══════════════════════════════════════════════════════════════
    // ⚠️ CONFIGURATION - REMPLACE CES VALEURS
    // ═══════════════════════════════════════════════════════════════

    private static final String EMAIL_FROM = "souleimab945@gmail.com";      // ← TON EMAIL GMAIL
    private static final String EMAIL_PASSWORD = "sccq fmbh mosn tkli"; // ← MOT DE PASSE APP (16 caractères)

    // ═══════════════════════════════════════════════════════════════

    /**
     * Méthode simple pour tester l'envoi d'email
     */
    public static boolean envoyerEmailTest(String destinataire, String sujet, String message) {
        System.out.println("════════════════════════════════════════════");
        System.out.println("📧 DÉBUT ENVOI EMAIL");
        System.out.println("════════════════════════════════════════════");
        System.out.println("De: " + EMAIL_FROM);
        System.out.println("À: " + destinataire);
        System.out.println("Sujet: " + sujet);
        System.out.println("════════════════════════════════════════════");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.debug", "true"); // ✅ Active les logs détaillés

        try {
            System.out.println("🔄 Création de la session...");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            // ✅ Active le mode debug
            session.setDebug(true);

            System.out.println("🔄 Création du message...");

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EMAIL_FROM, "AgriFund"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            msg.setSubject(sujet);
            msg.setText(message);

            System.out.println("🔄 Envoi en cours...");

            Transport.send(msg);

            System.out.println("════════════════════════════════════════════");
            System.out.println("✅ EMAIL ENVOYÉ AVEC SUCCÈS!");
            System.out.println("════════════════════════════════════════════");

            return true;

        } catch (AuthenticationFailedException e) {
            System.err.println("════════════════════════════════════════════");
            System.err.println("❌ ERREUR D'AUTHENTIFICATION!");
            System.err.println("════════════════════════════════════════════");
            System.err.println("Cause: Email ou mot de passe incorrect");
            System.err.println("Solution:");
            System.err.println("  1. Vérifie que EMAIL_FROM est correct");
            System.err.println("  2. Utilise un MOT DE PASSE D'APPLICATION (pas ton mot de passe normal)");
            System.err.println("  3. Va sur: https://myaccount.google.com/apppasswords");
            System.err.println("════════════════════════════════════════════");
            e.printStackTrace();
            return false;

        } catch (MessagingException e) {
            System.err.println("════════════════════════════════════════════");
            System.err.println("❌ ERREUR D'ENVOI!");
            System.err.println("════════════════════════════════════════════");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            return false;

        } catch (Exception e) {
            System.err.println("════════════════════════════════════════════");
            System.err.println("❌ ERREUR INATTENDUE!");
            System.err.println("════════════════════════════════════════════");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoyer notification de décision (version HTML)
     */
    public static void envoyerNotificationDecision(String emailAgriculteur, String nomAgriculteur,
                                                   String nomProjet, String statut,
                                                   String justification, String nomBanque) {

        System.out.println("════════════════════════════════════════════");
        System.out.println("📧 ENVOI NOTIFICATION DÉCISION");
        System.out.println("════════════════════════════════════════════");
        System.out.println("Agriculteur: " + nomAgriculteur);
        System.out.println("Email: " + emailAgriculteur);
        System.out.println("Projet: " + nomProjet);
        System.out.println("Statut: " + statut);
        System.out.println("════════════════════════════════════════════");

        if (emailAgriculteur == null || emailAgriculteur.isEmpty()) {
            System.err.println("❌ Email agriculteur vide ou null!");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "AgriFund - Plateforme Agricole"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAgriculteur));

            // Sujet
            String sujet = getEmailSubject(statut, nomProjet);
            message.setSubject(sujet);

            // Contenu HTML
            String contenuHtml = buildEmailContent(nomAgriculteur, nomProjet, statut, justification, nomBanque);
            message.setContent(contenuHtml, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à: " + emailAgriculteur);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getEmailSubject(String statut, String nomProjet) {
        switch (statut) {
            case "Approuvé":
                return "🎉 Bonne nouvelle ! Votre projet \"" + nomProjet + "\" a été approuvé";
            case "Rejeté":
                return "📋 Décision concernant votre projet \"" + nomProjet + "\"";
            default:
                return "📋 Mise à jour de votre projet \"" + nomProjet + "\"";
        }
    }

    private static String buildEmailContent(String nomAgriculteur, String nomProjet,
                                            String statut, String justification, String nomBanque) {

        String couleurStatut = statut.equals("Approuvé") ? "#28a745" :
                statut.equals("Rejeté") ? "#dc3545" : "#ffc107";

        String icone = statut.equals("Approuvé") ? "✅" :
                statut.equals("Rejeté") ? "❌" : "⏳";

        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden;'>" +

                // Header
                "<div style='background: linear-gradient(135deg, #133D03, #2D5016); color: white; padding: 30px; text-align: center;'>" +
                "<h1 style='margin: 0;'>🌾 AgriFund</h1>" +
                "<p style='margin: 10px 0 0 0;'>Plateforme de Financement Agricole</p>" +
                "</div>" +

                // Content
                "<div style='padding: 30px;'>" +
                "<p style='font-size: 18px;'>Bonjour <strong>" + nomAgriculteur + "</strong>,</p>" +
                "<p>Une décision a été prise concernant votre projet agricole.</p>" +

                // Projet
                "<div style='background-color: #f8f9fa; border-left: 4px solid #133D03; padding: 15px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #666;'>Projet :</p>" +
                "<p style='margin: 5px 0 0 0; font-size: 20px; font-weight: bold; color: #133D03;'>" + nomProjet + "</p>" +
                "</div>" +

                // Statut
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<span style='background-color: " + couleurStatut + "; color: white; padding: 15px 40px; border-radius: 50px; font-size: 20px; font-weight: bold;'>" +
                icone + " " + statut.toUpperCase() + "</span>" +
                "</div>" +

                // Justification
                "<div style='background-color: #fff3cd; border: 1px solid #ffc107; padding: 20px; border-radius: 8px;'>" +
                "<p style='margin: 0 0 10px 0; font-weight: bold;'>📝 Justification :</p>" +
                "<p style='margin: 0;'>" + justification + "</p>" +
                "</div>" +

                "<p style='margin-top: 20px; color: #666;'>Décision par : <strong>" + nomBanque + "</strong></p>" +
                "</div>" +

                // Footer
                "<div style='background-color: #f8f9fa; padding: 20px; text-align: center;'>" +
                "<p style='margin: 0; color: #999; font-size: 12px;'>© 2024 AgriFund</p>" +
                "</div>" +

                "</div></body></html>";
    }
}
