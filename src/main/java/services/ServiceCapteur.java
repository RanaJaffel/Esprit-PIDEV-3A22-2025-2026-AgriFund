package services;

import entities.capteur;
import utils.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCapteur implements interfaceCrud<capteur> {

    private final Connection con = MyDabase.getInstance().getCon();

    @Override
    public void ajouter(capteur capteur) throws SQLException {
        String req = "INSERT INTO capteur(typeCapteur, localisation, statut, id_projet) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setString(1, capteur.getTypeCapteur());
        ps.setString(2, capteur.getLocalisation());
        ps.setString(3, capteur.getStatut());
        ps.setInt(4, capteur.getIdProjet());
        ps.executeUpdate();
        System.out.println("✅ Capteur ajouté : " + capteur.getTypeCapteur());
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
        String req = "UPDATE capteur SET typeCapteur=?, localisation=?, statut=?, id_projet=? WHERE id_capteur=?";
        PreparedStatement ps = con.prepareStatement(req);
        ps.setString(1, capteur.getTypeCapteur());
        ps.setString(2, capteur.getLocalisation());
        ps.setString(3, capteur.getStatut());
        ps.setInt(4, capteur.getIdProjet());
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
            c.setIdProjet(rs.getInt("id_projet"));
            list.add(c);
        }

        return list;
    }

    /**
     * Récupérer la localisation d'un capteur par son ID
     */
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

    /**
     * Récupérer un capteur par son ID
     */
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
            c.setIdProjet(rs.getInt("id_projet"));
            return c;
        }
        return null;
    }

    /**
     * Compter le nombre de capteurs actifs
     */
    public int countCapteursActifs() throws SQLException {
        String req = "SELECT COUNT(*) as total FROM capteur WHERE statut = 'ACTIF'";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }
}