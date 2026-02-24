package com.agrifund.services;

import com.agrifund.entities.ressourceproject;
import com.agrifund.util.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ressourceprojectCRUD implements InterfaceCRUD<ressourceproject> {

    private final Connection cnx;

    public ressourceprojectCRUD() {
        this.cnx = MyDatabase.getInstance().getConnection();
    }

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
        String sql = "SELECT idressource, nomressource, typeressource, quantite, cout, fournisseur, statut, dateajout, idproject FROM ressourceproject";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ressourceproject r = new ressourceproject(
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
                list.add(r);
            }
        }
        return list;
    }
}