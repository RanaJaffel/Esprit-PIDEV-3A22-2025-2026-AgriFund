package com.agrifund.services;

import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ProduitFinancierService {

    // CREATE
    public boolean ajouterProduit(ProduitFinancier produit) {
        String sql = "INSERT INTO produit_financier (nom_produit, type_financement, taux_interet, " +
                "montant_min, montant_max, regles_financieres) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, produit.getNomProduit());
            pstmt.setString(2, produit.getTypeFinancement());
            pstmt.setDouble(3, produit.getTauxInteret());
            pstmt.setDouble(4, produit.getMontantMin());
            pstmt.setDouble(5, produit.getMontantMax());
            pstmt.setString(6, produit.getReglesFinancieres());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ - Tous les produits
    public ObservableList<ProduitFinancier> getAllProduits() {
        ObservableList<ProduitFinancier> produits = FXCollections.observableArrayList();
        String sql = "SELECT * FROM produit_financier";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ProduitFinancier produit = new ProduitFinancier(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("type_financement"),
                        rs.getDouble("taux_interet"),
                        rs.getDouble("montant_min"),
                        rs.getDouble("montant_max"),
                        rs.getString("regles_financieres")
                );
                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    // READ - Produit par ID
    public ProduitFinancier getProduitById(int id) {
        String sql = "SELECT * FROM produit_financier WHERE id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new ProduitFinancier(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("type_financement"),
                        rs.getDouble("taux_interet"),
                        rs.getDouble("montant_min"),
                        rs.getDouble("montant_max"),
                        rs.getString("regles_financieres")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // UPDATE
    public boolean modifierProduit(ProduitFinancier produit) {
        String sql = "UPDATE produit_financier SET nom_produit=?, type_financement=?, " +
                "taux_interet=?, montant_min=?, montant_max=?, regles_financieres=? " +
                "WHERE id_produit=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, produit.getNomProduit());
            pstmt.setString(2, produit.getTypeFinancement());
            pstmt.setDouble(3, produit.getTauxInteret());
            pstmt.setDouble(4, produit.getMontantMin());
            pstmt.setDouble(5, produit.getMontantMax());
            pstmt.setString(6, produit.getReglesFinancieres());
            pstmt.setInt(7, produit.getIdProduit());

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

    // SEARCH
    public ObservableList<ProduitFinancier> rechercherProduits(String critere) {
        ObservableList<ProduitFinancier> produits = FXCollections.observableArrayList();
        String sql = "SELECT * FROM produit_financier WHERE nom_produit LIKE ? OR type_financement LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + critere + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ProduitFinancier produit = new ProduitFinancier(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("type_financement"),
                        rs.getDouble("taux_interet"),
                        rs.getDouble("montant_min"),
                        rs.getDouble("montant_max"),
                        rs.getString("regles_financieres")
                );
                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }
}
