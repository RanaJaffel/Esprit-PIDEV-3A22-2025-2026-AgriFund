package com.agrifund.services;

import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class OffreFinanciereService {

    // CREATE - avec banque_id
    public boolean ajouterOffre(OffreFinanciere offre) {
        String sql = "INSERT INTO offre_financiere (nom_offre, conditions, statut, id_produit, banque_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, offre.getNomOffre());
            pstmt.setString(2, offre.getConditions());
            pstmt.setString(3, offre.getStatut());
            pstmt.setInt(4, offre.getIdProduit());

            if (offre.getBanqueId() != null && offre.getBanqueId() > 0) {
                pstmt.setInt(5, offre.getBanqueId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ - Toutes les offres (pour Admin et Agriculteur)
    public ObservableList<OffreFinanciere> getAllOffres() {
        ObservableList<OffreFinanciere> offres = FXCollections.observableArrayList();
        String sql = "SELECT o.*, u.nom as banque_nom " +
                "FROM offre_financiere o " +
                "LEFT JOIN Banque b ON o.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                OffreFinanciere offre = mapResultSetToOffre(rs);
                offres.add(offre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offres;
    }

    // READ - Offres avec JOIN (avec détails du produit)
    public ObservableList<OffreFinanciere> getAllOffresAvecProduit() {
        ObservableList<OffreFinanciere> offres = FXCollections.observableArrayList();
        String sql = "SELECT o.*, p.nom_produit, p.type_financement, p.taux_interet, " +
                "p.prix_fixe, p.regles_financieres, u.nom as banque_nom " +
                "FROM offre_financiere o " +
                "INNER JOIN produit_financier p ON o.id_produit = p.id_produit " +
                "LEFT JOIN Banque b ON o.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                OffreFinanciere offre = mapResultSetToOffre(rs);

                ProduitFinancier produit = new ProduitFinancier(
                        rs.getInt("id_produit"),
                        rs.getString("nom_produit"),
                        rs.getString("type_financement"),
                        rs.getDouble("taux_interet"),
                        rs.getDouble("prix_fixe"),
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

    // READ - Offres par banque (pour Banque connectée)
    public ObservableList<OffreFinanciere> getOffresByBanqueId(int banqueId) {
        ObservableList<OffreFinanciere> offres = FXCollections.observableArrayList();
        String sql = "SELECT o.*, p.nom_produit, u.nom as banque_nom " +
                "FROM offre_financiere o " +
                "LEFT JOIN produit_financier p ON o.id_produit = p.id_produit " +
                "LEFT JOIN Banque b ON o.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE o.banque_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, banqueId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OffreFinanciere offre = mapResultSetToOffre(rs);
                offre.setNomProduit(rs.getString("nom_produit"));
                offres.add(offre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offres;
    }

    // READ - Offre par ID
    public OffreFinanciere getOffreById(int id) {
        String sql = "SELECT o.*, u.nom as banque_nom " +
                "FROM offre_financiere o " +
                "LEFT JOIN Banque b ON o.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE o.id_offre = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToOffre(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // UPDATE
    public boolean modifierOffre(OffreFinanciere offre) {
        String sql = "UPDATE offre_financiere SET nom_offre=?, conditions=?, " +
                "statut=?, id_produit=?, banque_id=? WHERE id_offre=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, offre.getNomOffre());
            pstmt.setString(2, offre.getConditions());
            pstmt.setString(3, offre.getStatut());
            pstmt.setInt(4, offre.getIdProduit());

            if (offre.getBanqueId() != null && offre.getBanqueId() > 0) {
                pstmt.setInt(5, offre.getBanqueId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setInt(6, offre.getIdOffre());

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
        String sql = "SELECT o.*, u.nom as banque_nom " +
                "FROM offre_financiere o " +
                "LEFT JOIN Banque b ON o.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE o.nom_offre LIKE ? OR o.statut LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + critere + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OffreFinanciere offre = mapResultSetToOffre(rs);
                offres.add(offre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offres;
    }

    // SEARCH - par banque
    public ObservableList<OffreFinanciere> rechercherOffresByBanque(String critere, int banqueId) {
        ObservableList<OffreFinanciere> offres = FXCollections.observableArrayList();
        String sql = "SELECT o.*, u.nom as banque_nom " +
                "FROM offre_financiere o " +
                "LEFT JOIN Banque b ON o.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE o.banque_id = ? AND (o.nom_offre LIKE ? OR o.statut LIKE ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, banqueId);
            String searchPattern = "%" + critere + "%";
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OffreFinanciere offre = mapResultSetToOffre(rs);
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
        String sql = "SELECT o.*, u.nom as banque_nom " +
                "FROM offre_financiere o " +
                "LEFT JOIN Banque b ON o.banque_id = b.id " +
                "LEFT JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE o.id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProduit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OffreFinanciere offre = mapResultSetToOffre(rs);
                offres.add(offre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return offres;
    }

    // Helper method
    private OffreFinanciere mapResultSetToOffre(ResultSet rs) throws SQLException {
        OffreFinanciere offre = new OffreFinanciere(
                rs.getInt("id_offre"),
                rs.getString("nom_offre"),
                rs.getString("conditions"),
                rs.getString("statut"),
                rs.getInt("id_produit")
        );

        offre.setBanqueId(rs.getInt("banque_id"));

        try {
            String nomBanque = rs.getString("banque_nom");
            if (nomBanque != null) {
                offre.setNomBanque(nomBanque);
            }
        } catch (SQLException e) {
            // colonne pas présente, ignorer
        }

        return offre;
    }
}
