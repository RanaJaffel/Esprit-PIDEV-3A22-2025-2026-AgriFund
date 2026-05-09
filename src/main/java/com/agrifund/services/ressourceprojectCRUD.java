package com.agrifund.services;

import com.agrifund.entities.ressourceproject;
import com.agrifund.entities.RessourceDetailDTO;
import com.agrifund.util.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ressourceprojectCRUD implements InterfaceCRUD<ressourceproject> {

    private final Connection cnx;

    public ressourceprojectCRUD() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

    // ========================================================================
    // CRUD DE BASE
    // ========================================================================

    @Override
    public void ajouter(ressourceproject r) throws SQLException {
        String sql = "INSERT INTO ressourceproject (nomressource, typeressource, quantite, cout, fournisseur, statut, dateajout, idproject) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNomressource());
            ps.setString(2, r.getTyperessource());
            ps.setInt(3, r.getQuantite());
            ps.setBigDecimal(4, r.getCout());
            ps.setString(5, r.getFournisseur());
            ps.setString(6, r.getStatut());
            ps.setDate(7, r.getDateajout());
            ps.setInt(8, r.getIdproject());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setIdressource(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void modifier(ressourceproject r) throws SQLException {
        String sql = "UPDATE ressourceproject SET nomressource = ?, typeressource = ?, quantite = ?, cout = ?, fournisseur = ?, statut = ?, dateajout = ?, idproject = ? WHERE idressource = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getNomressource());
            ps.setString(2, r.getTyperessource());
            ps.setInt(3, r.getQuantite());
            ps.setBigDecimal(4, r.getCout());
            ps.setString(5, r.getFournisseur());
            ps.setString(6, r.getStatut());
            ps.setDate(7, r.getDateajout());
            ps.setInt(8, r.getIdproject());
            ps.setInt(9, r.getIdressource());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM ressourceproject WHERE idressource = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<ressourceproject> afficher() throws SQLException {
        List<ressourceproject> list = new ArrayList<>();
        String sql = "SELECT * FROM ressourceproject ORDER BY dateajout DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ressourceproject r = mapResultSetToRessource(rs);
                list.add(r);
            }
        }
        return list;
    }

    // ========================================================================
    // MÉTHODES POUR AGRICULTEUR - Voir uniquement ses ressources
    // ========================================================================

    /**
     * Récupère les ressources d'un agriculteur spécifique via ses projets
     */
    public List<ressourceproject> afficherParAgriculteur(int agriculteurId) throws SQLException {
        List<ressourceproject> list = new ArrayList<>();
        String sql = """
            SELECT r.* 
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            WHERE p.agriculteur_id = ?
            ORDER BY r.dateajout DESC
        """;

        System.out.println("[SQL] afficherParAgriculteur - agriculteur_id = " + agriculteurId);

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, agriculteurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ressourceproject r = mapResultSetToRessource(rs);
                    list.add(r);
                }
            }
        }

        System.out.println("[SQL] Ressources trouvées: " + list.size());
        return list;
    }

    /**
     * Récupère les ressources d'un projet spécifique
     */
    public List<ressourceproject> afficherParProjet(int idproject) throws SQLException {
        List<ressourceproject> list = new ArrayList<>();
        String sql = "SELECT * FROM ressourceproject WHERE idproject = ? ORDER BY dateajout DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idproject);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ressourceproject r = mapResultSetToRessource(rs);
                    list.add(r);
                }
            }
        }
        return list;
    }

    /**
     * Récupère une ressource par son ID
     */
    public ressourceproject rechercherParId(int idressource) throws SQLException {
        String sql = "SELECT * FROM ressourceproject WHERE idressource = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idressource);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRessource(rs);
                }
            }
        }
        return null;
    }

    /**
     * Vérifier si une ressource appartient à un agriculteur
     */
    public boolean ressourceAppartientAgriculteur(int idressource, int agriculteurId) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            WHERE r.idressource = ? AND p.agriculteur_id = ?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idressource);
            ps.setInt(2, agriculteurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // ========================================================================
    // MÉTHODES POUR ADMIN/BANQUE - Voir toutes les ressources avec détails
    // ========================================================================

    /**
     * Récupère toutes les ressources avec les infos du projet et de l'agriculteur
     */
    public List<RessourceDetailDTO> afficherAvecDetails() throws SQLException {
        List<RessourceDetailDTO> list = new ArrayList<>();
        String sql = """
            SELECT 
                r.idressource, r.nomressource, r.typeressource, r.quantite, 
                r.cout, r.fournisseur, r.statut, r.dateajout,
                p.idproject, p.nomproject, p.surface, p.budgetdemande, p.statut AS statut_projet,
                a.id AS agriculteur_id, 
                u.nom AS nom_agriculteur, u.prenom AS prenom_agriculteur,
                u.email AS email_agriculteur, u.tel AS tel_agriculteur,
                a.adresseferme, a.typeCulture
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            INNER JOIN agriculteur a ON p.agriculteur_id = a.id
            INNER JOIN utilisateur u ON a.utilisateur_id = u.id
            ORDER BY r.dateajout DESC
        """;

        System.out.println("[SQL] afficherAvecDetails - Chargement de toutes les ressources...");

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RessourceDetailDTO dto = mapResultSetToDTO(rs);
                list.add(dto);
            }
        }

        System.out.println("[SQL] Total ressources avec détails: " + list.size());
        return list;
    }

    /**
     * Récupère les ressources filtrées par statut projet
     */
    public List<RessourceDetailDTO> afficherAvecDetailsParStatutProjet(String statutProjet) throws SQLException {
        List<RessourceDetailDTO> list = new ArrayList<>();
        String sql = """
            SELECT 
                r.idressource, r.nomressource, r.typeressource, r.quantite, 
                r.cout, r.fournisseur, r.statut, r.dateajout,
                p.idproject, p.nomproject, p.surface, p.budgetdemande, p.statut AS statut_projet,
                a.id AS agriculteur_id, 
                u.nom AS nom_agriculteur, u.prenom AS prenom_agriculteur,
                u.email AS email_agriculteur, u.tel AS tel_agriculteur,
                a.adresseferme, a.typeCulture
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            INNER JOIN agriculteur a ON p.agriculteur_id = a.id
            INNER JOIN utilisateur u ON a.utilisateur_id = u.id
            WHERE p.statut = ?
            ORDER BY r.dateajout DESC
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statutProjet);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RessourceDetailDTO dto = mapResultSetToDTO(rs);
                    list.add(dto);
                }
            }
        }
        return list;
    }

    /**
     * Récupère les ressources filtrées par type
     */
    public List<RessourceDetailDTO> afficherAvecDetailsParType(String typeRessource) throws SQLException {
        List<RessourceDetailDTO> list = new ArrayList<>();
        String sql = """
            SELECT 
                r.idressource, r.nomressource, r.typeressource, r.quantite, 
                r.cout, r.fournisseur, r.statut, r.dateajout,
                p.idproject, p.nomproject, p.surface, p.budgetdemande, p.statut AS statut_projet,
                a.id AS agriculteur_id, 
                u.nom AS nom_agriculteur, u.prenom AS prenom_agriculteur,
                u.email AS email_agriculteur, u.tel AS tel_agriculteur,
                a.adresseferme, a.typeCulture
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            INNER JOIN agriculteur a ON p.agriculteur_id = a.id
            INNER JOIN utilisateur u ON a.utilisateur_id = u.id
            WHERE r.typeressource = ?
            ORDER BY r.dateajout DESC
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, typeRessource);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RessourceDetailDTO dto = mapResultSetToDTO(rs);
                    list.add(dto);
                }
            }
        }
        return list;
    }

    /**
     * Récupère les détails des ressources d'un agriculteur spécifique
     */
    public List<RessourceDetailDTO> afficherDetailsParAgriculteur(int agriculteurId) throws SQLException {
        List<RessourceDetailDTO> list = new ArrayList<>();
        String sql = """
            SELECT 
                r.idressource, r.nomressource, r.typeressource, r.quantite, 
                r.cout, r.fournisseur, r.statut, r.dateajout,
                p.idproject, p.nomproject, p.surface, p.budgetdemande, p.statut AS statut_projet,
                a.id AS agriculteur_id, 
                u.nom AS nom_agriculteur, u.prenom AS prenom_agriculteur,
                u.email AS email_agriculteur, u.tel AS tel_agriculteur,
                a.adresseferme, a.typeCulture
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            INNER JOIN agriculteur a ON p.agriculteur_id = a.id
            INNER JOIN utilisateur u ON a.utilisateur_id = u.id
            WHERE a.id = ?
            ORDER BY p.nomproject, r.dateajout DESC
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, agriculteurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RessourceDetailDTO dto = mapResultSetToDTO(rs);
                    list.add(dto);
                }
            }
        }
        return list;
    }

    /**
     * Récupère les détails des ressources d'un projet spécifique
     */
    public List<RessourceDetailDTO> afficherDetailsParProjet(int idproject) throws SQLException {
        List<RessourceDetailDTO> list = new ArrayList<>();
        String sql = """
            SELECT 
                r.idressource, r.nomressource, r.typeressource, r.quantite, 
                r.cout, r.fournisseur, r.statut, r.dateajout,
                p.idproject, p.nomproject, p.surface, p.budgetdemande, p.statut AS statut_projet,
                a.id AS agriculteur_id, 
                u.nom AS nom_agriculteur, u.prenom AS prenom_agriculteur,
                u.email AS email_agriculteur, u.tel AS tel_agriculteur,
                a.adresseferme, a.typeCulture
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            INNER JOIN agriculteur a ON p.agriculteur_id = a.id
            INNER JOIN utilisateur u ON a.utilisateur_id = u.id
            WHERE p.idproject = ?
            ORDER BY r.dateajout DESC
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idproject);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RessourceDetailDTO dto = mapResultSetToDTO(rs);
                    list.add(dto);
                }
            }
        }
        return list;
    }

    // ========================================================================
    // STATISTIQUES
    // ========================================================================

    /**
     * Compter les ressources d'un agriculteur
     */
    public int compterParAgriculteur(int agriculteurId) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            WHERE p.agriculteur_id = ?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, agriculteurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Compter les ressources d'un projet
     */
    public int compterParProjet(int idproject) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ressourceproject WHERE idproject = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idproject);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Compter le total des ressources
     */
    public int compterTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ressourceproject";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Compter par type de ressource
     */
    public int compterParType(String typeressource) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ressourceproject WHERE typeressource = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, typeressource);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Compter par statut
     */
    public int compterParStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ressourceproject WHERE statut = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Calculer le coût total de toutes les ressources
     */
    public double calculerCoutTotal() throws SQLException {
        String sql = "SELECT SUM(cout * quantite) AS total FROM ressourceproject";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    /**
     * Calculer le coût total des ressources d'un agriculteur
     */
    public double calculerCoutTotalParAgriculteur(int agriculteurId) throws SQLException {
        String sql = """
            SELECT SUM(r.cout * r.quantite) AS total 
            FROM ressourceproject r
            INNER JOIN projectagricole p ON r.idproject = p.idproject
            WHERE p.agriculteur_id = ?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, agriculteurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    /**
     * Calculer le coût total des ressources d'un projet
     */
    public double calculerCoutTotalParProjet(int idproject) throws SQLException {
        String sql = "SELECT SUM(cout * quantite) AS total FROM ressourceproject WHERE idproject = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idproject);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    // ========================================================================
    // MAPPERS
    // ========================================================================

    /**
     * Mapper ResultSet vers ressourceproject
     */
    private ressourceproject mapResultSetToRessource(ResultSet rs) throws SQLException {
        return new ressourceproject(
                rs.getInt("idressource"),
                rs.getString("nomressource"),
                rs.getString("typeressource"),
                rs.getInt("quantite"),
                rs.getBigDecimal("cout"),
                rs.getString("fournisseur"),
                rs.getString("statut"),
                rs.getDate("dateajout"),
                rs.getInt("idproject")
        );
    }

    /**
     * Mapper ResultSet vers RessourceDetailDTO
     */
    private RessourceDetailDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        RessourceDetailDTO dto = new RessourceDetailDTO();

        // Ressource
        dto.setIdressource(rs.getInt("idressource"));
        dto.setNomressource(rs.getString("nomressource"));
        dto.setTyperessource(rs.getString("typeressource"));
        dto.setQuantite(rs.getInt("quantite"));
        dto.setCout(rs.getBigDecimal("cout"));
        dto.setFournisseur(rs.getString("fournisseur"));
        dto.setStatut(rs.getString("statut"));
        dto.setDateajout(rs.getDate("dateajout"));

        // Projet
        dto.setIdproject(rs.getInt("idproject"));
        dto.setNomproject(rs.getString("nomproject"));
        dto.setSurface(rs.getFloat("surface"));
        dto.setBudgetdemande(rs.getBigDecimal("budgetdemande"));
        dto.setStatutProjet(rs.getString("statut_projet"));

        // Agriculteur
        dto.setAgriculteurId(rs.getInt("agriculteur_id"));
        dto.setNomAgriculteur(rs.getString("nom_agriculteur"));
        dto.setPrenomAgriculteur(rs.getString("prenom_agriculteur"));
        dto.setEmailAgriculteur(rs.getString("email_agriculteur"));
        dto.setTelAgriculteur(rs.getString("tel_agriculteur"));
        dto.setAdresseFerme(rs.getString("adresseferme"));
        dto.setTypeCulture(rs.getString("typeCulture"));

        return dto;
    }
}