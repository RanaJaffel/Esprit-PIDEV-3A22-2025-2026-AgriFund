package com.agrifund.services;

import com.agrifund.entities.rapport_journalier;
import com.agrifund.util.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRapportJournalier {

    private final Connection con;

    public ServiceRapportJournalier() {
        con = MyDabase.getInstance().getCon();
    }

    public List<rapport_journalier> afficherTous() throws SQLException {
        List<rapport_journalier> list = new ArrayList<>();

        String sql = "SELECT * FROM rapport_journalier ORDER BY date_rapport DESC";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            rapport_journalier r = new rapport_journalier();
            r.setIdRapport(rs.getInt("id_rapport"));
            r.setDateRapport(rs.getDate("date_rapport").toLocalDate());
            r.setTypeMesure(rs.getString("type_mesure"));
            r.setMoyenne(rs.getDouble("moyenne"));
            r.setMin(rs.getDouble("min"));
            r.setMax(rs.getDouble("max"));
            r.setIdCapteur(rs.getInt("id_capteur"));

            list.add(r);
        }
        return list;
    }
}