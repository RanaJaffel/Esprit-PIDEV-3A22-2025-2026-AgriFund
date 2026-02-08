package dao;

import model.OffreFinanciere;
import model.ProduitFinancier;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffreFinanciereDAO {
    private Connection connection;
    private ProduitFinancierDAO produitDAO;

    public OffreFinanciereDAO() {
        this.connection = DatabaseConnection.getConnection();
        this.produitDAO = new ProduitFinancierDAO();
    }

    // Créer une offre
    public boolean create(OffreFinanciere offre) {
        String sql = "INSERT INTO offre_financiere (nom_offre, conditions, statut, id_produit) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, offre.getNomOffre());
            stmt.setString(2, offre.getConditions());
            stmt.setString(3, offre.getStatut());
            stmt.setInt(4, offre.getIdProduit());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création : " + e.getMessage());
            return false;
        }
    }

    // Lire une offre par ID
    public OffreFinanciere findById(int id) {
        String sql = "SELECT * FROM offre_financiere WHERE id_offre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractOffreFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
        }
        return null;
    }

    // Lire toutes les offres
    public List<OffreFinanciere> findAll() {
        List<OffreFinanciere> offres = new ArrayList<>();
        String sql = "SELECT * FROM offre_financiere";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                offres.add(extractOffreFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return offres;
    }

    // Trouver les offres par produit
    public List<OffreFinanciere> findByProduit(int idProduit) {
        List<OffreFinanciere> offres = new ArrayList<>();
        String sql = "SELECT * FROM offre_financiere WHERE id_produit = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                offres.add(extractOffreFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return offres;
    }

    // Mettre à jour une offre
    public boolean update(OffreFinanciere offre) {
        String sql = "UPDATE offre_financiere SET nom_offre = ?, conditions = ?, " +
                "statut = ?, id_produit = ? WHERE id_offre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, offre.getNomOffre());
            stmt.setString(2, offre.getConditions());
            stmt.setString(3, offre.getStatut());
            stmt.setInt(4, offre.getIdProduit());
            stmt.setInt(5, offre.getIdOffre());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            return false;
        }
    }

    // Supprimer une offre
    public boolean delete(int id) {
        String sql = "DELETE FROM offre_financiere WHERE id_offre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            return false;
        }
    }

    // Méthode utilitaire pour extraire une offre d'un ResultSet
    private OffreFinanciere extractOffreFromResultSet(ResultSet rs) throws SQLException {
        OffreFinanciere offre = new OffreFinanciere();
        offre.setIdOffre(rs.getInt("id_offre"));
        offre.setNomOffre(rs.getString("nom_offre"));
        offre.setConditions(rs.getString("conditions"));
        offre.setStatut(rs.getString("statut"));
        offre.setIdProduit(rs.getInt("id_produit"));

        // Charger le produit associé
        ProduitFinancier produit = produitDAO.findById(offre.getIdProduit());
        offre.setProduitFinancier(produit);

        return offre;
    }
}