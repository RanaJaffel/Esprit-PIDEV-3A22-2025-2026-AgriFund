package dao;

import model.ProduitFinancier;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitFinancierDAO {
    private Connection connection;

    public ProduitFinancierDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    // Créer un produit
    public boolean create(ProduitFinancier produit) {
        String sql = "INSERT INTO produit_financier (nom_produit, type_financement, " +
                "taux_interet, montant_min, montant_max, regles_financieres) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, produit.getNomProduit());
            stmt.setString(2, produit.getTypeFinancement());
            stmt.setDouble(3, produit.getTauxInteret());
            stmt.setDouble(4, produit.getMontantMin());
            stmt.setDouble(5, produit.getMontantMax());
            stmt.setString(6, produit.getReglesFinancieres());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création : " + e.getMessage());
            return false;
        }
    }

    // Lire un produit par ID
    public ProduitFinancier findById(int id) {
        String sql = "SELECT * FROM produit_financier WHERE id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProduitFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
        }
        return null;
    }

    // Lire tous les produits
    public List<ProduitFinancier> findAll() {
        List<ProduitFinancier> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit_financier";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produits.add(extractProduitFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return produits;
    }

    // Mettre à jour un produit
    public boolean update(ProduitFinancier produit) {
        String sql = "UPDATE produit_financier SET nom_produit = ?, type_financement = ?, " +
                "taux_interet = ?, montant_min = ?, montant_max = ?, " +
                "regles_financieres = ? WHERE id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, produit.getNomProduit());
            stmt.setString(2, produit.getTypeFinancement());
            stmt.setDouble(3, produit.getTauxInteret());
            stmt.setDouble(4, produit.getMontantMin());
            stmt.setDouble(5, produit.getMontantMax());
            stmt.setString(6, produit.getReglesFinancieres());
            stmt.setInt(7, produit.getIdProduit());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            return false;
        }
    }

    // Supprimer un produit
    public boolean delete(int id) {
        String sql = "DELETE FROM produit_financier WHERE id_produit = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            return false;
        }
    }

    // Méthode utilitaire pour extraire un produit d'un ResultSet
    private ProduitFinancier extractProduitFromResultSet(ResultSet rs) throws SQLException {
        ProduitFinancier produit = new ProduitFinancier();
        produit.setIdProduit(rs.getInt("id_produit"));
        produit.setNomProduit(rs.getString("nom_produit"));
        produit.setTypeFinancement(rs.getString("type_financement"));
        produit.setTauxInteret(rs.getDouble("taux_interet"));
        produit.setMontantMin(rs.getDouble("montant_min"));
        produit.setMontantMax(rs.getDouble("montant_max"));
        produit.setReglesFinancieres(rs.getString("regles_financieres"));
        return produit;
    }
}