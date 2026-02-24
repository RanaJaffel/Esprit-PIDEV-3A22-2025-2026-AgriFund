package com.agrifund.services;

import com.agrifund.entities.releve_terrain;
import com.agrifund.util.MyDabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceReleveTerrain implements InterfaceCRUD<releve_terrain> {

    private final Connection con;

    public ServiceReleveTerrain() {
        con = MyDabase.getInstance().getCon();
    }

    @Override
    public void ajouter(releve_terrain releveTerrain) throws SQLException {
        String req = "INSERT INTO releve_terrain(type_mesure, valeur_mesuree, unite, date_heure, id_capteur) VALUES (?,?,?,NOW(),?)";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setString(1, releveTerrain.getTypeMesure());
        pst.setDouble(2, releveTerrain.getValeurMesuree());
        pst.setString(3, releveTerrain.getUnite());
        pst.setInt(4, releveTerrain.getIdCapteur());
        pst.executeUpdate();
        System.out.println("✅ Relevé ajouté !");
    }

    @Override
    public void modifier(releve_terrain releveTerrain) throws SQLException {
        String req = "UPDATE releve_terrain "
                + "SET type_mesure=?, valeur_mesuree=?, unite=?, id_capteur=? "
                + "WHERE id_releve=?";

        PreparedStatement pst = con.prepareStatement(req);
        pst.setString(1, releveTerrain.getTypeMesure());
        pst.setDouble(2, releveTerrain.getValeurMesuree());
        pst.setString(3, releveTerrain.getUnite());
        pst.setInt(4, releveTerrain.getIdCapteur());
        pst.setInt(5, releveTerrain.getIdReleve());
        pst.executeUpdate();
        System.out.println("✏️ Relevé terrain modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM releve_terrain WHERE id_releve=?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("🗑️ Relevé supprimé !");
    }

    @Override
    public List<releve_terrain> afficher() throws SQLException {
        List<releve_terrain> list = new ArrayList<>();

        String req = "SELECT * FROM releve_terrain ORDER BY date_heure DESC";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            releve_terrain r = new releve_terrain();

            r.setIdReleve(rs.getInt("id_releve"));
            r.setTypeMesure(rs.getString("type_mesure"));
            r.setValeurMesuree(rs.getDouble("valeur_mesuree"));
            r.setUnite(rs.getString("unite"));
            r.setIdCapteur(rs.getInt("id_capteur"));

            // Récupérer la date/heure
            Timestamp timestamp = rs.getTimestamp("date_heure");
            if (timestamp != null) {
                r.setDateHeure(timestamp.toLocalDateTime());
            } else {
                r.setDateHeure(LocalDateTime.now());
            }

            list.add(r);
        }

        return list;
    }


    public releve_terrain getReleveById(int idReleve) throws SQLException {
        String req = "SELECT * FROM releve_terrain WHERE id_releve = ?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, idReleve);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            releve_terrain r = new releve_terrain();
            r.setIdReleve(rs.getInt("id_releve"));
            r.setTypeMesure(rs.getString("type_mesure"));
            r.setValeurMesuree(rs.getDouble("valeur_mesuree"));
            r.setUnite(rs.getString("unite"));
            r.setIdCapteur(rs.getInt("id_capteur"));

            Timestamp timestamp = rs.getTimestamp("date_heure");
            if (timestamp != null) {
                r.setDateHeure(timestamp.toLocalDateTime());
            }

            return r;
        }
        return null;
    }


    public List<releve_terrain> getRelevesByCapteur(int idCapteur) throws SQLException {
        List<releve_terrain> list = new ArrayList<>();
        String req = "SELECT * FROM releve_terrain WHERE id_capteur = ? ORDER BY date_heure DESC";

        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, idCapteur);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            releve_terrain r = new releve_terrain();
            r.setIdReleve(rs.getInt("id_releve"));
            r.setTypeMesure(rs.getString("type_mesure"));
            r.setValeurMesuree(rs.getDouble("valeur_mesuree"));
            r.setUnite(rs.getString("unite"));
            r.setIdCapteur(rs.getInt("id_capteur"));

            Timestamp timestamp = rs.getTimestamp("date_heure");
            if (timestamp != null) {
                r.setDateHeure(timestamp.toLocalDateTime());
            }

            list.add(r);
        }

        return list;
    }


    public int countReleves() throws SQLException {
        String req = "SELECT COUNT(*) as total FROM releve_terrain";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }
    public void supprimerTousLesRelevesDuJour() throws SQLException {
        String sql = "DELETE FROM releve_terrain WHERE DATE(date_heure) = CURDATE()";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.executeUpdate();
    }

    public void genererRapportJournalier() throws SQLException {
        String sql = """
            INSERT INTO rapport_journalier
            (date_rapport, type_mesure, moyenne, min, max, id_capteur)
            SELECT
                CURDATE(),
                type_mesure,
                AVG(valeur_mesuree),
                MIN(valeur_mesuree),
                MAX(valeur_mesuree),
                id_capteur
            FROM releve_terrain
            WHERE DATE(date_heure) = CURDATE()
            GROUP BY type_mesure, id_capteur
        """;

        PreparedStatement pst = con.prepareStatement(sql);
        pst.executeUpdate();
    }
    // ===== MÉTIER AVANCÉ =====
// Détection d’anomalie basée sur moyenne + écart-type

    public boolean detecterAnomalieStatistique(int idCapteur, double nouvelleValeur) throws SQLException {

        String sql = """
        SELECT AVG(valeur_mesuree) as moyenne,
               STDDEV(valeur_mesuree) as ecart
        FROM releve_terrain
        WHERE id_capteur = ?
    """;

        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, idCapteur);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {

            double moyenne = rs.getDouble("moyenne");
            double ecart = rs.getDouble("ecart");

            if (ecart == 0) return false;

            return Math.abs(nouvelleValeur - moyenne) > 2 * ecart;
        }

        return false;
    }
}