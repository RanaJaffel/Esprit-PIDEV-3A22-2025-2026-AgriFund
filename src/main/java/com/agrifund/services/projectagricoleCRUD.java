package com.agrifund.services;

import com.agrifund.entities.ProjectWithAgriculteur;
import com.agrifund.entities.projectagricole;
import com.agrifund.util.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class projectagricoleCRUD implements InterfaceCRUD<projectagricole> {

    private final Connection cnx;

    public projectagricoleCRUD() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(projectagricole p) throws SQLException {
        String sql = "INSERT INTO projectagricole (agriculteur_id, nomproject, surface, budgetdemande, statut, datesoumission, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getAgriculteurId());
            ps.setString(2, p.getNomproject());
            ps.setFloat(3, p.getSurface());
            ps.setBigDecimal(4, p.getBudgetdemande());
            ps.setString(5, p.getStatut());
            ps.setDate(6, p.getDatesoumission());
            if (p.getLatitude() != null) ps.setDouble(7, p.getLatitude());
            else ps.setNull(7, Types.DOUBLE);
            if (p.getLongitude() != null) ps.setDouble(8, p.getLongitude());
            else ps.setNull(8, Types.DOUBLE);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setIdproject(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(projectagricole p) throws SQLException {
        String sql = "UPDATE projectagricole SET agriculteur_id = ?, nomproject = ?, surface = ?, budgetdemande = ?, statut = ?, datesoumission = ?, latitude = ?, longitude = ? WHERE idproject = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, p.getAgriculteurId());
            ps.setString(2, p.getNomproject());
            ps.setFloat(3, p.getSurface());
            ps.setBigDecimal(4, p.getBudgetdemande());
            ps.setString(5, p.getStatut());
            ps.setDate(6, p.getDatesoumission());
            if (p.getLatitude() != null) ps.setDouble(7, p.getLatitude());
            else ps.setNull(7, Types.DOUBLE);
            if (p.getLongitude() != null) ps.setDouble(8, p.getLongitude());
            else ps.setNull(8, Types.DOUBLE);
            ps.setInt(9, p.getIdproject());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM projectagricole WHERE idproject = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<projectagricole> afficher() throws SQLException {
        List<projectagricole> list = new ArrayList<>();
        String sql = "SELECT idproject, agriculteur_id, nomproject, surface, budgetdemande, statut, datesoumission, latitude, longitude FROM projectagricole";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                projectagricole p = mapResultSetToProject(rs);
                list.add(p);
            }
        }
        return list;
    }

    /**
     * NOUVEAU: Récupérer tous les projets d'un agriculteur spécifique
     */
    public List<projectagricole> afficherParAgriculteur(int agriculteurId) throws SQLException {
        List<projectagricole> list = new ArrayList<>();
        String sql = "SELECT idproject, agriculteur_id, nomproject, surface, budgetdemande, statut, datesoumission, latitude, longitude FROM projectagricole WHERE agriculteur_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, agriculteurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projectagricole p = mapResultSetToProject(rs);
                    list.add(p);
                }
            }
        }
        return list;
    }

    /**
     * NOUVEAU: Récupérer un projet par son ID
     */
    public projectagricole rechercherParId(int idproject) throws SQLException {
        String sql = "SELECT idproject, agriculteur_id, nomproject, surface, budgetdemande, statut, datesoumission, latitude, longitude FROM projectagricole WHERE idproject = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idproject);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProject(rs);
                }
            }
        }
        return null;
    }

    /**
     * NOUVEAU: Vérifier si un projet appartient à un agriculteur
     */
    public boolean projetAppartientAgriculteur(int idproject, int agriculteurId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM projectagricole WHERE idproject = ? AND agriculteur_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idproject);
            ps.setInt(2, agriculteurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * NOUVEAU: Compter les projets d'un agriculteur
     */
    public int compterProjetsParAgriculteur(int agriculteurId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM projectagricole WHERE agriculteur_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, agriculteurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * NOUVEAU: Compter les projets par statut pour un agriculteur
     */
    public int compterProjetsParStatut(int agriculteurId, String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM projectagricole WHERE agriculteur_id = ? AND statut = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, agriculteurId);
            ps.setString(2, statut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * NOUVEAU: Vérifier l'unicité du nom de projet pour un agriculteur
     */
    public boolean nomProjetExiste(int agriculteurId, String nomProject, Integer excludeProjectId) throws SQLException {
        String sql;
        if (excludeProjectId != null) {
            sql = "SELECT COUNT(*) FROM projectagricole WHERE agriculteur_id = ? AND LOWER(nomproject) = LOWER(?) AND idproject != ?";
        } else {
            sql = "SELECT COUNT(*) FROM projectagricole WHERE agriculteur_id = ? AND LOWER(nomproject) = LOWER(?)";
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, agriculteurId);
            ps.setString(2, nomProject);
            if (excludeProjectId != null) {
                ps.setInt(3, excludeProjectId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Helper: Mapper ResultSet vers projectagricole
     */
    private projectagricole mapResultSetToProject(ResultSet rs) throws SQLException {
        Double lat = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
        Double lng = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;

        return new projectagricole(
                rs.getInt("idproject"),
                rs.getInt("agriculteur_id"),
                rs.getString("nomproject"),
                rs.getFloat("surface"),
                rs.getBigDecimal("budgetdemande"),
                rs.getString("statut"),
                rs.getDate("datesoumission"),
                lat,
                lng
        );
    }
    /**
     * Récupérer tous les projets avec les informations de l'agriculteur
     * Pour Admin et Banque
     */
    public List<ProjectWithAgriculteur> afficherTousAvecAgriculteur() throws SQLException {
        List<ProjectWithAgriculteur> list = new ArrayList<>();

        String sql = "SELECT " +
                "p.idproject, p.agriculteur_id, p.nomproject, p.surface, p.budgetdemande, " +
                "p.statut, p.datesoumission, p.latitude, p.longitude, " +
                "u.nom AS agriculteur_nom, u.prenom AS agriculteur_prenom, " +
                "u.email AS agriculteur_email, u.tel AS agriculteur_tel, " +
                "a.adresseferme, a.typeCulture, a.superficieferme, a.compteverifie " +
                "FROM projectagricole p " +
                "INNER JOIN agriculteur a ON p.agriculteur_id = a.id " +
                "INNER JOIN utilisateur u ON a.utilisateur_id = u.id " +
                "ORDER BY p.datesoumission DESC";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ProjectWithAgriculteur p = new ProjectWithAgriculteur();

                // Projet
                p.setIdproject(rs.getInt("idproject"));
                p.setAgriculteurId(rs.getInt("agriculteur_id"));
                p.setNomproject(rs.getString("nomproject"));
                p.setSurface(rs.getFloat("surface"));
                p.setBudgetdemande(rs.getBigDecimal("budgetdemande"));
                p.setStatut(rs.getString("statut"));
                p.setDatesoumission(rs.getDate("datesoumission"));
                p.setLatitude(rs.getObject("latitude") != null ? rs.getDouble("latitude") : null);
                p.setLongitude(rs.getObject("longitude") != null ? rs.getDouble("longitude") : null);

                // Agriculteur
                p.setAgriculteurNom(rs.getString("agriculteur_nom"));
                p.setAgriculteurPrenom(rs.getString("agriculteur_prenom"));
                p.setAgriculteurEmail(rs.getString("agriculteur_email"));
                p.setAgriculteurTel(rs.getString("agriculteur_tel"));
                p.setAdresseFerme(rs.getString("adresseferme"));
                p.setTypeCulture(rs.getString("typeCulture"));
                p.setSuperficieFerme(rs.getBigDecimal("superficieferme"));
                p.setCompteVerifie(rs.getBoolean("compteverifie"));

                list.add(p);
            }
        }

        return list;
    }

    /**
     * Récupérer les projets par statut avec les informations de l'agriculteur
     */
    public List<ProjectWithAgriculteur> afficherParStatutAvecAgriculteur(String statut) throws SQLException {
        List<ProjectWithAgriculteur> list = new ArrayList<>();

        String sql = "SELECT " +
                "p.idproject, p.agriculteur_id, p.nomproject, p.surface, p.budgetdemande, " +
                "p.statut, p.datesoumission, p.latitude, p.longitude, " +
                "u.nom AS agriculteur_nom, u.prenom AS agriculteur_prenom, " +
                "u.email AS agriculteur_email, u.tel AS agriculteur_tel, " +
                "a.adresseferme, a.typeCulture, a.superficieferme, a.compteverifie " +
                "FROM projectagricole p " +
                "INNER JOIN agriculteur a ON p.agriculteur_id = a.id " +
                "INNER JOIN utilisateur u ON a.utilisateur_id = u.id " +
                "WHERE p.statut = ? " +
                "ORDER BY p.datesoumission DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProjectWithAgriculteur p = new ProjectWithAgriculteur();

                    // Projet
                    p.setIdproject(rs.getInt("idproject"));
                    p.setAgriculteurId(rs.getInt("agriculteur_id"));
                    p.setNomproject(rs.getString("nomproject"));
                    p.setSurface(rs.getFloat("surface"));
                    p.setBudgetdemande(rs.getBigDecimal("budgetdemande"));
                    p.setStatut(rs.getString("statut"));
                    p.setDatesoumission(rs.getDate("datesoumission"));
                    p.setLatitude(rs.getObject("latitude") != null ? rs.getDouble("latitude") : null);
                    p.setLongitude(rs.getObject("longitude") != null ? rs.getDouble("longitude") : null);

                    // Agriculteur
                    p.setAgriculteurNom(rs.getString("agriculteur_nom"));
                    p.setAgriculteurPrenom(rs.getString("agriculteur_prenom"));
                    p.setAgriculteurEmail(rs.getString("agriculteur_email"));
                    p.setAgriculteurTel(rs.getString("agriculteur_tel"));
                    p.setAdresseFerme(rs.getString("adresseferme"));
                    p.setTypeCulture(rs.getString("typeCulture"));
                    p.setSuperficieFerme(rs.getBigDecimal("superficieferme"));
                    p.setCompteVerifie(rs.getBoolean("compteverifie"));

                    list.add(p);
                }
            }
        }

        return list;
    }
}
