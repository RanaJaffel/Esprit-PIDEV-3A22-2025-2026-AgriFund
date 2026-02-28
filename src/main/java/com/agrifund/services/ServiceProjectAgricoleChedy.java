package com.agrifund.services;

import com.agrifund.entities.projectagricole;
import com.agrifund.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceProjectAgricoleChedy {

    private Connection connection;

    public ServiceProjectAgricoleChedy() {
        this.connection = MyConnection.getInstance().getCon();
    }

    public List<projectagricole> getAllProjects() throws SQLException {
        List<projectagricole> projets = new ArrayList<>();
        String req = "SELECT * FROM projectagricole";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                projets.add(mapResultSet(rs));
            }
        }
        return projets;
    }

    public projectagricole getById(int idProjet) throws SQLException {
        String req = "SELECT * FROM projectagricole WHERE idproject = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idProjet);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public Map<String, Integer> getProjectNamesMap() throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        String req = "SELECT idproject, nomproject FROM projectagricole";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                map.put(rs.getString("nomproject"), rs.getInt("idproject"));
            }
        }
        return map;
    }

    public List<String> getAllProjectNames() throws SQLException {
        List<String> noms = new ArrayList<>();
        String req = "SELECT nomproject FROM projectagricole ORDER BY nomproject";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                noms.add(rs.getString("nomproject"));
            }
        }
        return noms;
    }

    public int getIdByName(String nomProjet) throws SQLException {
        String req = "SELECT idproject FROM projectagricole WHERE nomproject = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, nomProjet);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idproject");
                }
            }
        }
        return -1;
    }

    public String getNameById(int idProjet) throws SQLException {
        String req = "SELECT nomproject FROM projectagricole WHERE idproject = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idProjet);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nomproject");
                }
            }
        }
        return null;
    }

    /**
     * ✅ CORRIGÉ - Utilise agriculteur_id (avec underscore)
     */
    private projectagricole mapResultSet(ResultSet rs) throws SQLException {
        projectagricole projet = new projectagricole();

        projet.setIdproject(rs.getInt("idproject"));
        projet.setNomproject(rs.getString("nomproject"));
        projet.setSurface(rs.getFloat("surface"));
        projet.setBudgetdemande(rs.getBigDecimal("budgetdemande"));
        projet.setStatut(rs.getString("statut"));
        projet.setDatesoumission(rs.getDate("datesoumission"));

        // ✅ CORRIGÉ : agriculteur_id au lieu de agriculteurId
        projet.setAgriculteurId(rs.getInt("agriculteur_id"));

        // Latitude/Longitude (optionnel)
        try {
            projet.setLatitude(rs.getDouble("latitude"));
            projet.setLongitude(rs.getDouble("longitude"));
        } catch (SQLException e) {
            // Colonnes optionnelles, ignore si elles n'existent pas
        }

        return projet;
    }

    public List<Integer> getAllProjectIds() {
        return List.of();
    }
}