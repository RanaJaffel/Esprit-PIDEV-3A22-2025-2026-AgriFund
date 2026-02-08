package Utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Service d'envoi d'emails
 * Utilise JavaMail pour envoyer des emails de réinitialisation de mot de passe
 */
public class EmailService {

    // Configuration Gmail SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    // ⚠️ IMPORTANT: Remplacez par votre email et mot de passe d'application
    private static final String EMAIL_FROM = "souleimab945@gmail.com";
    private static final String EMAIL_PASSWORD = "pykd mqiu muuj smxk";

    /**
     * Envoyer un email de réinitialisation de mot de passe
     */
    public static boolean envoyerEmailReinitialisation(String emailDestinataire, String token) {
        try {
            // Configuration des propriétés SMTP
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // Création de la session avec authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
            message.setSubject("Réinitialisation de votre mot de passe - Gestion Utilisateurs");

            // Contenu HTML de l'email
            String contenuHTML = creerContenuEmail(token);
            message.setContent(contenuHTML, "text/html; charset=utf-8");

            // Envoi de l'email
            Transport.send(message);

            System.out.println("✓ Email envoyé avec succès à: " + emailDestinataire);
            return true;

        } catch (MessagingException e) {
            System.err.println("✗ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Créer le contenu HTML de l'email
     */
    private static String creerContenuEmail(String token) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                "        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }" +
                "        .content { padding: 30px 20px; }" +
                "        .token-box { background-color: #f0f0f0; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0; font-family: monospace; font-size: 16px; word-break: break-all; }" +
                "        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
                "        .footer { text-align: center; color: #666; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; }" +
                "        .button { display: inline-block; background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🔐 Réinitialisation de Mot de Passe</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Bonjour,</p>" +
                "            <p>Vous avez demandé la réinitialisation de votre mot de passe pour votre compte <strong>AgriFund</strong>.</p>" +
                "            <p>Voici votre code de réinitialisation :</p>" +
                "            <div class='token-box'>" +
                "                <strong>Token :</strong><br>" +
                "                " + token +
                "            </div>" +
                "            <div class='warning'>" +
                "                ⚠️ <strong>Important :</strong>" +
                "                <ul>" +
                "                    <li>Ce token est valide pendant <strong>1 heure</strong></li>" +
                "                    <li>Ne partagez ce code avec personne</li>" +
                "                    <li>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email</li>" +
                "                </ul>" +
                "            </div>" +
                "            <h3>Comment réinitialiser votre mot de passe :</h3>" +
                "            <ol>" +
                "                <li>Retournez à l'application</li>" +
                "                <li>Sélectionnez \"Mot de passe oublié\" dans le menu principal</li>" +
                "                <li>Entrez votre email</li>" +
                "                <li>Copiez et collez le token ci-dessus</li>" +
                "                <li>Définissez votre nouveau mot de passe</li>" +
                "            </ol>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                "            <p>&copy; 2025-2026 AgriFund - by greencoders</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Tester la configuration email
     */
    public static boolean testerConfiguration() {
        System.out.println("\n🔧 Test de la configuration email...");

        if (EMAIL_FROM.equals("votre.email@gmail.com") ||
                EMAIL_PASSWORD.equals("votre_mot_de_passe_application")) {
            System.err.println("✗ Configuration email non définie!");
            System.err.println("⚠️ Veuillez configurer EMAIL_FROM et EMAIL_PASSWORD dans EmailService.java");
            return false;
        }

        System.out.println("✓ Configuration email OK");
        System.out.println("  Email expéditeur: " + EMAIL_FROM);
        return true;
    }
}