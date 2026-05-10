package com.agrifund.services;

import com.agrifund.entities.Code2FA;
import com.agrifund.entities.Parametres2FA;
import com.agrifund.entities.Utilisateur;
import com.agrifund.util.DatabaseConnection;
import com.agrifund.util.EmailService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service d'Authentification à Deux Facteurs (2FA)
 */
public class TwoFactorAuthService {

    private Connection connection;
    private UtilisateurService utilisateurService;

    public TwoFactorAuthService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.utilisateurService = new UtilisateurService();
    }

    // ============================================
    // GESTION DES PARAMÈTRES 2FA
    // ============================================

    /**
     * Obtenir les paramètres 2FA d'un utilisateur
     */
    public Parametres2FA getParametres(int utilisateurId) throws SQLException {
        String sql = "SELECT * FROM Parametres2FA WHERE utilisateur_id = ?";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);

            rs = pst.executeQuery();

            if (rs.next()) {
                Parametres2FA params = new Parametres2FA();
                params.setId(rs.getInt("id"));
                params.setUtilisateurId(rs.getInt("utilisateur_id"));
                params.setEstActive(rs.getBoolean("est_active"));
                params.setMethodePreferee(rs.getString("methode_preferee"));
                params.setTelephone2fa(rs.getString("telephone_2fa"));

                Timestamp activation = rs.getTimestamp("date_activation");
                if (activation != null) {
                    params.setDateActivation(activation.toLocalDateTime());
                }

                return params;
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return null;
    }

    /**
     * Créer ou mettre à jour les paramètres 2FA
     */
    public void sauvegarderParametres(Parametres2FA params) throws SQLException {
        Parametres2FA existing = getParametres(params.getUtilisateurId());

        if (existing == null) {
            // Créer
            String sql = "INSERT INTO Parametres2FA (utilisateur_id, est_active, methode_preferee, telephone_2fa, date_activation) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pst = null;

            try {
                pst = connection.prepareStatement(sql);
                pst.setInt(1, params.getUtilisateurId());
                pst.setBoolean(2, params.isEstActive());
                pst.setString(3, params.getMethodePreferee());
                pst.setString(4, params.getTelephone2fa());

                if (params.isEstActive() && params.getDateActivation() == null) {
                    pst.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                } else {
                    pst.setTimestamp(5, params.getDateActivation() != null ?
                            Timestamp.valueOf(params.getDateActivation()) : null);
                }

                pst.executeUpdate();
                System.out.println("✓ Paramètres 2FA créés");

            } finally {
                if (pst != null) pst.close();
            }

        } else {
            // Mettre à jour
            String sql = "UPDATE Parametres2FA SET est_active = ?, methode_preferee = ?, " +
                    "telephone_2fa = ?, date_activation = ? WHERE utilisateur_id = ?";

            PreparedStatement pst = null;

            try {
                pst = connection.prepareStatement(sql);
                pst.setBoolean(1, params.isEstActive());
                pst.setString(2, params.getMethodePreferee());
                pst.setString(3, params.getTelephone2fa());

                if (params.isEstActive() && existing.getDateActivation() == null) {
                    pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                } else {
                    pst.setTimestamp(4, params.getDateActivation() != null ?
                            Timestamp.valueOf(params.getDateActivation()) : null);
                }

                pst.setInt(5, params.getUtilisateurId());

                pst.executeUpdate();
                System.out.println("✓ Paramètres 2FA mis à jour");

            } finally {
                if (pst != null) pst.close();
            }
        }
    }

    /**
     * Vérifier si 2FA est activé pour un utilisateur
     */
    public boolean is2FAActive(int utilisateurId) throws SQLException {
        Parametres2FA params = getParametres(utilisateurId);
        return params != null && params.isEstActive();
    }

    // ============================================
    // GESTION DES CODES 2FA
    // ============================================

    /**
     * Générer un code 2FA à 6 chiffres
     */
    private String genererCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6 chiffres
        return String.valueOf(code);
    }

    /**
     * Envoyer un code 2FA
     */
    public Code2FA envoyerCode2FA(int utilisateurId) throws SQLException {
        Utilisateur user = utilisateurService.rechercherParId(utilisateurId);

        if (user == null) {
            System.out.println("✗ Utilisateur non trouvé!");
            return null;
        }

        Parametres2FA params = getParametres(utilisateurId);
        if (params == null || !params.isEstActive()) {
            System.out.println("✗ 2FA non activé pour cet utilisateur");
            return null;
        }

        // Générer le code
        String code = genererCode();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(5); // Valide 5 minutes

        Code2FA code2fa = new Code2FA(utilisateurId, code, expiration, params.getMethodePreferee());

        // Enregistrer en base
        String sql = "INSERT INTO Code2FA (utilisateur_id, code, date_expiration, type_envoi) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, utilisateurId);
            pst.setString(2, code);
            pst.setTimestamp(3, Timestamp.valueOf(expiration));
            pst.setString(4, params.getMethodePreferee());

            pst.executeUpdate();

            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                code2fa.setId(rs.getInt(1));
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        // Envoyer par email
        if ("email".equals(params.getMethodePreferee())) {
            envoyerCodeParEmail(user, code);
        } else if ("sms".equals(params.getMethodePreferee())) {
            // Pour l'instant, afficher dans la console
            System.out.println("\n📱 CODE SMS (simulation): " + code);
            System.out.println("   Envoyé au: " + params.getTelephone2fa());
        }

        return code2fa;
    }

    /**
     * Envoyer le code par email
     */
    private void envoyerCodeParEmail(Utilisateur user, String code) {
        String sujet = "Code de vérification - Connexion sécurisée";

        String contenuHTML = creerEmailCode2FA(code, user.getPrenom());

        // Utiliser EmailService (à implémenter)
        try {
            boolean envoye = EmailService.envoyerEmail2FA(user.getEmail(), code);
            if (envoye) {
                System.out.println("✓ Code 2FA envoyé par email à: " + user.getEmail());
            } else {
                System.out.println("⚠️ Email non configuré, code affiché: " + code);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Impossible d'envoyer l'email");
            System.out.println("📧 CODE 2FA: " + code + " (valide 5 minutes)");
        }
    }

    /**
     * Créer le contenu HTML de l'email 2FA
     */
    private String creerEmailCode2FA(String code, String prenom) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><style>" +
                "body{font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;}" +
                ".container{max-width:600px;margin:0 auto;background:#fff;padding:30px;border-radius:8px;}" +
                ".code{font-size:32px;font-weight:bold;color:#4CAF50;text-align:center;" +
                "padding:20px;background:#f0f0f0;border-radius:5px;letter-spacing:8px;}" +
                ".warning{background:#fff3cd;padding:15px;border-left:4px solid #ffc107;margin:20px 0;}" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<h1 style='color:#4CAF50;text-align:center;'>🔐 Code de Vérification</h1>" +
                "<p>Bonjour " + prenom + ",</p>" +
                "<p>Voici votre code de vérification pour vous connecter :</p>" +
                "<div class='code'>" + code + "</div>" +
                "<div class='warning'>" +
                "⚠️ <strong>Important :</strong><ul>" +
                "<li>Ce code est valide pendant <strong>5 minutes</strong></li>" +
                "<li>Ne partagez jamais ce code</li>" +
                "<li>Si vous n'avez pas demandé cette connexion, ignorez cet email</li>" +
                "</ul></div>" +
                "<p style='text-align:center;color:#666;font-size:12px;margin-top:30px;'>" +
                "Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                "</div></body></html>";
    }

    /**
     * Vérifier un code 2FA
     */
    public boolean verifierCode(int utilisateurId, String codeEntre) throws SQLException {
        String sql = "SELECT * FROM Code2FA " +
                "WHERE utilisateur_id = ? AND code = ? AND est_utilise = FALSE " +
                "ORDER BY date_creation DESC LIMIT 1";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            pst.setString(2, codeEntre);

            rs = pst.executeQuery();

            if (rs.next()) {
                Code2FA code = new Code2FA();
                code.setId(rs.getInt("id"));
                code.setUtilisateurId(rs.getInt("utilisateur_id"));
                code.setCode(rs.getString("code"));

                Timestamp expiration = rs.getTimestamp("date_expiration");
                if (expiration != null) {
                    code.setDateExpiration(expiration.toLocalDateTime());
                }

                code.setEstUtilise(rs.getBoolean("est_utilise"));

                // Vérifier si le code est valide
                if (code.isValide()) {
                    // Marquer comme utilisé
                    marquerCodeUtilise(code.getId());
                    System.out.println("✓ Code 2FA vérifié avec succès!");
                    return true;
                } else {
                    System.out.println("✗ Code expiré!");
                    return false;
                }
            } else {
                System.out.println("✗ Code incorrect!");
                return false;
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }
    }

    /**
     * Marquer un code comme utilisé
     */
    private void marquerCodeUtilise(int codeId) throws SQLException {
        String sql = "UPDATE Code2FA SET est_utilise = TRUE, date_utilisation = ? WHERE id = ?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(2, codeId);

            pst.executeUpdate();

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * Nettoyer les codes expirés (maintenance)
     */
    public void nettoyerCodesExpires() throws SQLException {
        String sql = "DELETE FROM Code2FA WHERE date_expiration < NOW() AND est_utilise = FALSE";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            int deleted = pst.executeUpdate();

            if (deleted > 0) {
                System.out.println("✓ " + deleted + " code(s) expiré(s) supprimé(s)");
            }

        } finally {
            if (pst != null) pst.close();
        }
    }

    // ============================================
    // HISTORIQUE DE CONNEXION
    // ============================================

    /**
     * Enregistrer une tentative de connexion
     */
    public void enregistrerConnexion(int utilisateurId, boolean reussie, String methode) throws SQLException {
        String sql = "INSERT INTO HistoriqueConnexion (utilisateur_id, connexion_reussie, methode_auth) " +
                "VALUES (?, ?, ?)";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            pst.setBoolean(2, reussie);
            pst.setString(3, methode);

            pst.executeUpdate();

        } finally {
            if (pst != null) pst.close();
        }
    }
}
