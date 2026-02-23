package com.agrifund.services;

import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class OffreFinanciereService {

    // CREATE
    public boolean ajouterOffre(OffreFinanciere offre) {
        String sql = "INSERT INTO offre_financiere (nom_offre, conditions, statut, id_produit) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, offre.getNomOffre());
            pstmt.setString(2, offre.getConditions());
            pstmt.setString(3, offre.getStatut());
            pstmt.setInt(4, offre.getIdProduit());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ - Toutes les offres
    public ObservableList<OffreFinanciere> getAllOffres() {
        ObservableList<OffreFinanciere> offres = FXCollections.observableArrayList();
        String sql = "SELECT * FROM offre_financiere";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                OffreFinanciere offre = new OffreFinanciere(
                        rs.getInt("id_offre"),
                        rs.getString("nom_offre"),
                        rs.getString("conditions"),
                        rs.getString("statut"),
                        rs.getInt("id_produit")
                );
                offres.add(offre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offres;
    }

    // READ - Offres avec JOIN (avec details du produit)
    public ObservableList<OffreFinanciere> getAllOffresAvecProduit() {
        ObservableList<OffreFinanciere> offres = FXCollections.observableArrayList();
        String sql = "SELECT o.*, p.nom_produit, p.type_financement, p.taux_interet, " +
                "p.montant_min, p.montant_max, p.regles_financieres " +
                "FROM offre_financiere o " +
                "INNER JOIN produit_financier p ON o.id_produit = p.id_produit";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                OffreFinanciere offre = new OffreFinanciere(
                        rs.getInt("id_offre"),
                        rs.getString("nom_offre"),
                        rs.getString("conditions"),
                        rs.getString("statut"),
                        rs.getInt("id_produit")
                );

                ProduitFinancier produit = new ProduitFinancier(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("type_financement"),
                        rs.getDouble("taux_interet"),
                        rs.getDouble("montant_min"),
                        rs.getDouble("montant_max"),
                        rs.getString("regles_financieres")
                );

                offre.setProduitFinancier(produit);
                offres.add(offre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offres;
    }

    // READ - Offre par ID
    public OffreFinanciere getOffreById(int id) {
        String sql = "SELECT * FROM offre_financiere WHERE id_offre = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new OffreFinanciere(
                        rs.getInt("id_offre"),
                        rs.getString("nom_offre"),
                        rs.getString("conditions"),
                        rs.getString("statut"),
                        rs.getInt("id_produit")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // UPDATE
    public boolean modifierOffre(OffreFinanciere offre) {
        String sql = "UPDATE offre_financiere SET nom_offre=?, conditions=?, " +
                "statut=?, id_produit=? WHERE id_offre=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, offre.getNomOffre());
            pstmt.setString(2, offre.getConditions());
            pstmt.setString(3, offre.getStatut());
            pstmt.setInt(4, offre.getIdProduit());
            pstmt.setInt(5, offre.getIdOffre());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // DELETE
    public boolean supprimerOffre(int id) {
        String sql = "DELETE FROM offre_financiere WHERE id_offre = ?";

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
    public ObservableList<OffreFinanciere> rechercherOffres(String critere) {
        ObservableList<OffreFinanciere> offres = FXCollections.observableArrayList();
        String sql = "SELECT * FROM offre_financiere WHERE nom_offre LIKE ? OR statut LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + critere + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OffreFinanciere offre = new OffreFinanciere(
                        rs.getInt("id_offre"),
                        rs.getString("nom_offre"),
                        rs.getString("conditions"),
                        rs.getString("statut"),
                        rs.getInt("id_produit")
                );
                offres.add(offre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offres;
    }

    // Obtenir offres par produit
    public ObservableList<OffreFinanciere> getOffresByProduit(int idProduit) {
        ObservableList<OffreFinanciere> offres = FXCollections.observableArrayList();
        String sql = "SELECT * FROM offre_financiere WHERE id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProduit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OffreFinanciere offre = new OffreFinanciere(
                        rs.getInt("id_offre"),
                        rs.getString("nom_offre"),
                        rs.getString("conditions"),
                        rs.getString("statut"),
                        rs.getInt("id_produit")
                );
                offres.add(offre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offres;
    }
}
