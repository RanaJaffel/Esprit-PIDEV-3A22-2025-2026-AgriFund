package com.agrifund.services;

import com.agrifund.entities.capteur;
import com.agrifund.util.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceCapteur implements com.agrifund.services.intCrud<capteur> {

    private final Connection con = MyDabase.getInstance().getCon();


    @Override
    public void ajouter(capteur capteur) throws SQLException {

        String req = """
        INSERT INTO capteur
        (typeCapteur, modele, localisation, statut,
         date_installation, idproject, id_user,
         latitude, longitude)
        VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?)
        """;

        PreparedStatement ps = con.prepareStatement(req);

        ps.setString(1, capteur.getTypeCapteur());
        ps.setString(2, capteur.getModele());
        ps.setString(3, capteur.getLocalisation());
        ps.setString(4, capteur.getStatut());

        if (capteur.getIdProjet() != null)
            ps.setInt(5, capteur.getIdProjet());
        else
            ps.setNull(5, Types.INTEGER);

        if (capteur.getIdUser() != null)
            ps.setInt(6, capteur.getIdUser());
        else
            ps.setNull(6, Types.INTEGER);

        if (capteur.getLatitude() != null)
            ps.setDouble(7, capteur.getLatitude());
        else
            ps.setNull(7, Types.DOUBLE);

        if (capteur.getLongitude() != null)
            ps.setDouble(8, capteur.getLongitude());
        else
            ps.setNull(8, Types.DOUBLE);

        ps.executeUpdate();

        System.out.println("✅ Capteur ajouté");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM capteur WHERE id_capteur = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("🗑️ Capteur supprimé : ID " + id);
    }

    @Override
    public void modifier(capteur capteur) throws SQLException {
        String req = "UPDATE capteur SET typeCapteur=?, localisation=?, statut=?, idproject=? WHERE id_capteur=?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setString(1, capteur.getTypeCapteur());
        ps.setString(2, capteur.getLocalisation());
        ps.setString(3, capteur.getStatut());
        if (capteur.getIdProjet() != null) {
            ps.setInt(4, capteur.getIdProjet());
        } else {
            ps.setNull(4, Types.INTEGER);
        }
        ps.setInt(5, capteur.getIdCapteur());
        ps.executeUpdate();
        System.out.println("✏️ Capteur modifié : ID " + capteur.getIdCapteur());
    }

    @Override
    public List<capteur> afficher() throws SQLException {
        List<capteur> list = new ArrayList<>();
        String req = "SELECT * FROM capteur ORDER BY id_capteur DESC";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            capteur c = new capteur();
            c.setIdCapteur(rs.getInt("id_capteur"));
            c.setTypeCapteur(rs.getString("typeCapteur"));
            c.setLocalisation(rs.getString("localisation"));
            c.setStatut(rs.getString("statut"));
            int idp = rs.getInt("idproject");
            c.setIdProjet(rs.wasNull() ? null : idp);
            list.add(c);
        }
        return list;
    }

    // =====================================================
    // NOUVELLE MÉTHODE : Capteurs par Agriculteur
    // =====================================================
    public List<capteur> afficherParAgriculteur(int agriculteurId) throws SQLException {
        List<capteur> list = new ArrayList<>();
        String req = """
            SELECT c.* FROM capteur c
            INNER JOIN projectagricole p ON c.idproject = p.idproject
            WHERE p.agriculteur_id = ?
            ORDER BY c.id_capteur DESC
        """;
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, agriculteurId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            capteur c = new capteur();
            c.setIdCapteur(rs.getInt("id_capteur"));
            c.setTypeCapteur(rs.getString("typeCapteur"));
            c.setLocalisation(rs.getString("localisation"));
            c.setStatut(rs.getString("statut"));
            int idp = rs.getInt("idproject");
            c.setIdProjet(rs.wasNull() ? null : idp);
            list.add(c);
        }
        return list;
    }

    // =====================================================
    // NOUVELLE MÉTHODE : Projets par Agriculteur
    // =====================================================
    public Map<Integer, String> getProjetsMapByAgriculteur(int agriculteurId) throws SQLException {
        Map<Integer, String> map = new LinkedHashMap<>();
        String req = "SELECT idproject, nomproject FROM projectagricole WHERE agriculteur_id = ? ORDER BY nomproject";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, agriculteurId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            map.put(rs.getInt("idproject"), rs.getString("nomproject"));
        }
        return map;
    }

    // =====================================================
    // NOUVELLE MÉTHODE : Tous les capteurs avec infos agriculteur (pour Admin)
    // =====================================================
    public List<Map<String, Object>> afficherTousAvecAgriculteur() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String req = """
            SELECT c.*, p.nomproject, u.nom as agriculteur_nom, u.prenom as agriculteur_prenom
            FROM capteur c
            LEFT JOIN projectagricole p ON c.idproject = p.idproject
            LEFT JOIN agriculteur a ON p.agriculteur_id = a.id
            LEFT JOIN utilisateur u ON a.utilisateur_id = u.id
            ORDER BY c.id_capteur DESC
        """;
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("idCapteur", rs.getInt("id_capteur"));
            row.put("typeCapteur", rs.getString("typeCapteur"));
            row.put("localisation", rs.getString("localisation"));
            row.put("statut", rs.getString("statut"));
            row.put("nomProjet", rs.getString("nomproject"));
            row.put("agriculteur", rs.getString("agriculteur_prenom") + " " + rs.getString("agriculteur_nom"));
            list.add(row);
        }
        return list;
    }

    public List<Integer> getIdsProjets() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String req = "SELECT idproject FROM projectagricole ORDER BY idproject";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            ids.add(rs.getInt("idproject"));
        }
        return ids;
    }

    public Map<Integer, String> getProjetsMap() throws SQLException {
        Map<Integer, String> map = new LinkedHashMap<>();
        String req = "SELECT idproject, nomproject FROM projectagricole ORDER BY nomproject";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            map.put(rs.getInt("idproject"), rs.getString("nomproject"));
        }
        return map;
    }

    public String getNomProjetById(int idProjet) throws SQLException {
        String req = "SELECT nomproject FROM projectagricole WHERE idproject = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, idProjet);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("nomproject");
        }
        return "Projet #" + idProjet;
    }

    public String getLocalisationById(int idCapteur) throws SQLException {
        String req = "SELECT localisation FROM capteur WHERE id_capteur = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, idCapteur);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("localisation");
        }
        return null;
    }

    public capteur getCapteurById(int idCapteur) throws SQLException {
        String req = "SELECT * FROM capteur WHERE id_capteur = ?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, idCapteur);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            capteur c = new capteur();
            c.setIdCapteur(rs.getInt("id_capteur"));
            c.setTypeCapteur(rs.getString("typeCapteur"));
            c.setLocalisation(rs.getString("localisation"));
            c.setStatut(rs.getString("statut"));
            int idp = rs.getInt("idproject");
            c.setIdProjet(rs.wasNull() ? null : idp);
            return c;
        }
        return null;
    }

    public int countCapteursActifs() throws SQLException {
        String req = "SELECT COUNT(*) as total FROM capteur WHERE statut = 'ACTIF'";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    public int countCapteursActifsByAgriculteur(int agriculteurId) throws SQLException {
        String req = """
            SELECT COUNT(*) as total FROM capteur c
            INNER JOIN projectagricole p ON c.idproject = p.idproject
            WHERE p.agriculteur_id = ? AND c.statut = 'ACTIF'
        """;
        PreparedStatement ps = con.prepareStatement(req);
        ps.setInt(1, agriculteurId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    public void verifierEtatCapteurs() throws SQLException {
        String sql = """
            UPDATE capteur c
            LEFT JOIN releve_terrain r ON c.id_capteur = r.id_capteur
            SET c.statut = 'INACTIF'
            WHERE r.date_heure IS NULL
               OR r.date_heure < NOW() - INTERVAL 1 DAY
        """;
        PreparedStatement pst = con.prepareStatement(sql);
        pst.executeUpdate();
        System.out.println("🔎 Vérification automatique des capteurs terminée");
    }
}
