package com.agrifund.services;

import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ProduitFinancierService {

    // CREATE - avec banque_id
    public boolean ajouterProduit(ProduitFinancier produit) {
        String sql = "INSERT INTO produit_financier (nom_produit, type_financement, taux_interet, " +
                "montant_min, montant_max, regles_financieres, banque_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, produit.getNomProduit());
            pstmt.setString(2, produit.getTypeFinancement());
            pstmt.setDouble(3, produit.getTauxInteret());
            pstmt.setDouble(4, produit.getMontantMin());
            pstmt.setDouble(5, produit.getMontantMax());
            pstmt.setString(6, produit.getReglesFinancieres());

            if (produit.getBanqueId() > 0) {
                pstmt.setInt(7, produit.getBanqueId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ - Tous les produits (pour Admin et Agriculteur)
    public ObservableList<ProduitFinancier> getAllProduits() {
        ObservableList<ProduitFinancier> produits = FXCollections.observableArrayList();
        String sql = "SELECT p.*, b.codebanque as nom_banque, u.nom as banque_nom " +
                "FROM produit_financier p " +
                "LEFT JOIN Banque b ON p.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ProduitFinancier produit = mapResultSetToProduit(rs);
                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    // READ - Produits par banque (pour Banque connectée)
    public ObservableList<ProduitFinancier> getProduitsByBanqueId(int banqueId) {
        ObservableList<ProduitFinancier> produits = FXCollections.observableArrayList();
        String sql = "SELECT p.*, b.codebanque as nom_banque, u.nom as banque_nom " +
                "FROM produit_financier p " +
                "LEFT JOIN Banque b ON p.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE p.banque_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, banqueId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ProduitFinancier produit = mapResultSetToProduit(rs);
                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    // READ - Produit par ID
    public ProduitFinancier getProduitById(int id) {
        String sql = "SELECT p.*, b.codebanque as nom_banque, u.nom as banque_nom " +
                "FROM produit_financier p " +
                "LEFT JOIN Banque b ON p.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE p.id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduit(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // UPDATE
    public boolean modifierProduit(ProduitFinancier produit) {
        String sql = "UPDATE produit_financier SET nom_produit=?, type_financement=?, " +
                "taux_interet=?, montant_min=?, montant_max=?, regles_financieres=?, banque_id=? " +
                "WHERE id_produit=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, produit.getNomProduit());
            pstmt.setString(2, produit.getTypeFinancement());
            pstmt.setDouble(3, produit.getTauxInteret());
            pstmt.setDouble(4, produit.getMontantMin());
            pstmt.setDouble(5, produit.getMontantMax());
            pstmt.setString(6, produit.getReglesFinancieres());

            if (produit.getBanqueId() > 0) {
                pstmt.setInt(7, produit.getBanqueId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            pstmt.setInt(8, produit.getIdProduit());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // DELETE
    public boolean supprimerProduit(int id) {
        String sql = "DELETE FROM produit_financier WHERE id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // SEARCH - tous
    public ObservableList<ProduitFinancier> rechercherProduits(String critere) {
        ObservableList<ProduitFinancier> produits = FXCollections.observableArrayList();
        String sql = "SELECT p.*, b.codebanque as nom_banque, u.nom as banque_nom " +
                "FROM produit_financier p " +
                "LEFT JOIN Banque b ON p.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE p.nom_produit LIKE ? OR p.type_financement LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + critere + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ProduitFinancier produit = mapResultSetToProduit(rs);
                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    // SEARCH - par banque
    public ObservableList<ProduitFinancier> rechercherProduitsByBanque(String critere, int banqueId) {
        ObservableList<ProduitFinancier> produits = FXCollections.observableArrayList();
        String sql = "SELECT p.*, b.codebanque as nom_banque, u.nom as banque_nom " +
                "FROM produit_financier p " +
                "LEFT JOIN Banque b ON p.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE p.banque_id = ? AND (p.nom_produit LIKE ? OR p.type_financement LIKE ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, banqueId);
            String searchPattern = "%" + critere + "%";
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ProduitFinancier produit = mapResultSetToProduit(rs);
                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    // Helper method
    private ProduitFinancier mapResultSetToProduit(ResultSet rs) throws SQLException {
        ProduitFinancier produit = new ProduitFinancier(
                rs.getInt("id_produit"),
                rs.getString("nom_produit"),
                rs.getString("type_financement"),
                rs.getDouble("taux_interet"),
                rs.getDouble("montant_min"),
                rs.getDouble("montant_max"),
                rs.getString("regles_financieres")
        );

        produit.setBanqueId(rs.getInt("banque_id"));

        try {
            String nomBanque = rs.getString("banque_nom");
            if (nomBanque != null) {
                produit.setNomBanque(nomBanque);
            }
        } catch (SQLException e) {
            // colonne pas présente, ignorer
        }

        return produit;
    }
}