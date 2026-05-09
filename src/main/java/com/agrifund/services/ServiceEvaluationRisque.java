package com.agrifund.services;

import com.agrifund.util.MyConnection;
import com.agrifund.entities.EvaluationRisque;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvaluationRisque implements InterfaceCRUD<EvaluationRisque> {

    private Connection connection;

    public ServiceEvaluationRisque() {
        this.connection = MyConnection.getInstance().getCon();
    }

    @Override
    public void ajouter(EvaluationRisque evaluation) throws SQLException {
        String req = "INSERT INTO evaluationrisque (scoreGlobal, niveauRisque, fiabiliteDonnees, " +
                "facteurPrincipal, recommandation, dateEvaluation, idProjet, banqueId) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evaluation.getScoreGlobal());
            pst.setString(2, evaluation.getNiveauRisque());
            pst.setString(3, evaluation.getFiabiliteDonnees());
            pst.setString(4, evaluation.getFacteurPrincipal());
            pst.setString(5, evaluation.getRecommandation()); // ✅ String
            pst.setDate(6, new java.sql.Date(evaluation.getDateEvaluation().getTime()));
            pst.setInt(7, evaluation.getIdProjet());
            pst.setInt(8, evaluation.getBanqueId());
            pst.executeUpdate();
            System.out.println("✅ Évaluation ajoutée avec succès!");
        }
    }

    @Override
    public void modifier(EvaluationRisque evaluation) throws SQLException {
        String req = "UPDATE evaluationrisque SET scoreGlobal=?, niveauRisque=?, fiabiliteDonnees=?, " +
                "facteurPrincipal=?, recommandation=?, dateEvaluation=?, idProjet=?, banqueId=? " +
                "WHERE idEvaluation=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evaluation.getScoreGlobal());
            pst.setString(2, evaluation.getNiveauRisque());
            pst.setString(3, evaluation.getFiabiliteDonnees());
            pst.setString(4, evaluation.getFacteurPrincipal());
            pst.setString(5, evaluation.getRecommandation()); // ✅ String
            pst.setDate(6, new java.sql.Date(evaluation.getDateEvaluation().getTime()));
            pst.setInt(7, evaluation.getIdProjet());
            pst.setInt(8, evaluation.getBanqueId());
            pst.setInt(9, evaluation.getIdEvaluation());
            pst.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM evaluationrisque WHERE idEvaluation=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public void supprimerParBanque(int idEvaluation, int banqueId) throws SQLException {
        String req = "DELETE FROM evaluationrisque WHERE idEvaluation=? AND banqueId=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idEvaluation);
            pst.setInt(2, banqueId);
            pst.executeUpdate();
        }
    }

    @Override
    public List<EvaluationRisque> afficher() throws SQLException {
        List<EvaluationRisque> evaluations = new ArrayList<>();
        String req = "SELECT * FROM evaluationrisque";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                evaluations.add(mapResultSet(rs));
            }
        }
        return evaluations;
    }

    public List<EvaluationRisque> afficherParBanque(int banqueId) throws SQLException {
        List<EvaluationRisque> evaluations = new ArrayList<>();
        String req = "SELECT * FROM evaluationrisque WHERE banqueId = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, banqueId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    evaluations.add(mapResultSet(rs));
                }
            }
        }
        return evaluations;
    }

    public EvaluationRisque getById(int idEvaluation) throws SQLException {
        String req = "SELECT * FROM evaluationrisque WHERE idEvaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idEvaluation);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Integer> getEvaluationIdsByBanque(int banqueId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String req = "SELECT idEvaluation FROM evaluationrisque WHERE banqueId = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, banqueId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("idEvaluation"));
                }
            }
        }
        return ids;
    }

    public List<Integer> getAllEvaluationIds() throws SQLException {
        List<Integer> evaluationIds = new ArrayList<>();
        String req = "SELECT idEvaluation FROM evaluationrisque";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                evaluationIds.add(rs.getInt("idEvaluation"));
            }
        }
        return evaluationIds;
    }

    private EvaluationRisque mapResultSet(ResultSet rs) throws SQLException {
        EvaluationRisque eval = new EvaluationRisque();
        eval.setIdEvaluation(rs.getInt("idEvaluation"));
        eval.setScoreGlobal(rs.getInt("scoreGlobal"));
        eval.setNiveauRisque(rs.getString("niveauRisque"));
        eval.setFiabiliteDonnees(rs.getString("fiabiliteDonnees"));
        eval.setFacteurPrincipal(rs.getString("facteurPrincipal"));
        eval.setRecommandation(rs.getString("recommandation")); // ✅ getString
        eval.setDateEvaluation(rs.getDate("dateEvaluation"));
        eval.setIdProjet(rs.getInt("idProjet"));
        eval.setBanqueId(rs.getInt("banqueId"));
        return eval;
    }
}