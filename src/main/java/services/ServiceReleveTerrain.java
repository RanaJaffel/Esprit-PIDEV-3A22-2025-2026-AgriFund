package services;

import entities.capteur;
import entities.releve_terrain;
import utils.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReleveTerrain implements interfaceCrud<releve_terrain>{
    private Connection con;

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
        System.out.println("Relevé ajouté !");


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
        System.out.println("Relevé terrain modifié !");
    }



    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM releve_terrain WHERE id_releve=?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Relevé supprimé !");

    }

    @Override
    public List<releve_terrain> afficher() throws SQLException {

        List<releve_terrain> list = new ArrayList<>();

        String req = "SELECT * FROM releve_terrain";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            releve_terrain r = new releve_terrain();

            r.setIdReleve(rs.getInt("id_releve"));
            r.setTypeMesure(rs.getString("type_mesure"));
            r.setValeurMesuree(rs.getDouble("valeur_mesuree"));
            r.setUnite(rs.getString("unite"));
            r.setIdCapteur(rs.getInt("id_capteur"));

            list.add(r);
        }

        return list;
    }

}
