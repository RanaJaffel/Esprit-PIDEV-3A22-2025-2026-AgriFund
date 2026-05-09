package com.agrifund.services;

import com.agrifund.entities.TokenReinitialisation;
import com.agrifund.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service pour gérer les tokens de réinitialisation de mot de passe
 */
public class TokenReinitialisationService {

    private Connection connection;

    public TokenReinitialisationService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * GÉNÉRER un nouveau token de réinitialisation
     * Le token expire après 1 heure
     */
    public String genererToken(int utilisateurId) throws SQLException {
        // Générer un token unique
        String token = UUID.randomUUID().toString();

        // Le token expire dans 1 heure
        LocalDateTime dateExpiration = LocalDateTime.now().plusHours(1);

        String sql = "INSERT INTO TokenReinitialisation (utilisateur_id, token, date_expiration, " +
                "utilise, date_creation) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            pst.setString(2, token);
            pst.setTimestamp(3, Timestamp.valueOf(dateExpiration));
            pst.setBoolean(4, false);
            pst.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            pst.executeUpdate();
            System.out.println("✓ Token de réinitialisation généré!");

        } finally {
            if (pst != null) pst.close();
        }

        return token;
    }

    /**
     * VÉRIFIER si un token est valide
     */
    public TokenReinitialisation verifierToken(String token) throws SQLException {
        String sql = "SELECT * FROM TokenReinitialisation WHERE token=?";

        TokenReinitialisation tokenObj = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, token);
            rs = pst.executeQuery();

            if (rs.next()) {
                tokenObj = new TokenReinitialisation();
                tokenObj.setId(rs.getInt("id"));
                tokenObj.setUtilisateurId(rs.getInt("utilisateur_id"));
                tokenObj.setToken(rs.getString("token"));

                Timestamp expiration = rs.getTimestamp("date_expiration");
                if (expiration != null) {
                    tokenObj.setDateExpiration(expiration.toLocalDateTime());
                }

                tokenObj.setUtilise(rs.getBoolean("utilise"));

                Timestamp utilisation = rs.getTimestamp("date_utilisation");
                if (utilisation != null) {
                    tokenObj.setDateUtilisation(utilisation.toLocalDateTime());
                }

                Timestamp creation = rs.getTimestamp("date_creation");
                if (creation != null) {
                    tokenObj.setDateCreation(creation.toLocalDateTime());
                }
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return tokenObj;
    }

    /**
     * MARQUER un token comme utilisé
     */
    public void marquerUtilise(String token) throws SQLException {
        String sql = "UPDATE TokenReinitialisation SET utilise=?, date_utilisation=? WHERE token=?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setBoolean(1, true);
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(3, token);

            pst.executeUpdate();
            System.out.println("✓ Token marqué comme utilisé!");

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * SUPPRIMER les tokens expirés (maintenance)
     */
    public void nettoyerTokensExpires() throws SQLException {
        String sql = "DELETE FROM TokenReinitialisation WHERE date_expiration < ?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));

            int deleted = pst.executeUpdate();
            System.out.println("✓ " + deleted + " token(s) expiré(s) supprimé(s)!");

        } finally {
            if (pst != null) pst.close();
        }
    }
}