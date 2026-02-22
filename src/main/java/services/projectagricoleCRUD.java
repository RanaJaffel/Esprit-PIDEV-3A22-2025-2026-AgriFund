package services;

import entities.projectagricole;
import Utils.MyDatabase;

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
        String sql = "INSERT INTO projectagricole (nomproject, surface, budgetdemande, statut, datesoumission, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNomproject());
            ps.setFloat(2, p.getSurface());
            ps.setBigDecimal(3, p.getBudgetdemande());
            ps.setString(4, p.getStatut());
            ps.setDate(5, p.getDatesoumission());
            if (p.getLatitude() != null) ps.setDouble(6, p.getLatitude());
            else ps.setNull(6, Types.DOUBLE);
            if (p.getLongitude() != null) ps.setDouble(7, p.getLongitude());
            else ps.setNull(7, Types.DOUBLE);
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
        String sql = "UPDATE projectagricole SET nomproject = ?, surface = ?, budgetdemande = ?, statut = ?, datesoumission = ?, latitude = ?, longitude = ? WHERE idproject = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNomproject());
            ps.setFloat(2, p.getSurface());
            ps.setBigDecimal(3, p.getBudgetdemande());
            ps.setString(4, p.getStatut());
            ps.setDate(5, p.getDatesoumission());
            if (p.getLatitude() != null) ps.setDouble(6, p.getLatitude());
            else ps.setNull(6, Types.DOUBLE);
            if (p.getLongitude() != null) ps.setDouble(7, p.getLongitude());
            else ps.setNull(7, Types.DOUBLE);
            ps.setInt(8, p.getIdproject());
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
        String sql = "SELECT idproject, nomproject, surface, budgetdemande, statut, datesoumission, latitude, longitude FROM projectagricole";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Double lat = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
                Double lng = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
                projectagricole p = new projectagricole(
                        rs.getInt("idproject"),
                        rs.getString("nomproject"),
                        rs.getFloat("surface"),
                        rs.getBigDecimal("budgetdemande"),
                        rs.getString("statut"),
                        rs.getDate("datesoumission"),
                        lat,
                        lng
                );
                list.add(p);
            }
        }
        return list;
    }
}