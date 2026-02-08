package services;

import entities.capteur;
import utils.MyDabase;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCapteur implements interfaceCrud<capteur> {

    private Connection con;

    public ServiceCapteur() {
        con = MyDabase.getInstance().getCon();
    }
    @Override
    public void ajouter(capteur capteur) throws SQLException {
        String req = "INSERT INTO capteur( typeCapteur, localisation, statut ) "+" VALUES ('"+capteur.getTypeCapteur()+"','"+capteur.getLocalisation()+"','"+capteur.getStatut()+"')";
        Statement statement = con.createStatement();
        statement.executeUpdate(req);
        System.out.println("Capteur ajouté !");
    }

    @Override
    public void modifier(capteur capteur) throws SQLException {
        String req = "UPDATE capteur SET typeCapteur=?, localisation=?, statut=? WHERE id_capteur=?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setString(1, capteur.getTypeCapteur());
        pst.setString(2, capteur.getLocalisation());
        pst.setString(3, capteur.getStatut());
        pst.setInt(4, capteur.getIdCapteur());
        pst.executeUpdate();
        System.out.println("Capteur modifié !");

    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM capteur WHERE id_capteur=?";
        PreparedStatement pst = con.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Capteur supprimé !");

    }

    @Override
    public List<capteur> afficher() throws SQLException {
        String req = "SELECT * FROM capteur";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(req);
        List<capteur> list=new ArrayList<capteur>();

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
}}
