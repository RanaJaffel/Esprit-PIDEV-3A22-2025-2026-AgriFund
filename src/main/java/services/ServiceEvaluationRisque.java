package services;

import entities.EvaluationRisque;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvaluationRisque implements InterfaceCRUD<EvaluationRisque> {

    private Connection connection;

    public ServiceEvaluationRisque() {
        this.connection = MyConnection.getInstance();
    }

    @Override
    public void ajouter(EvaluationRisque evaluation) throws SQLException {
        String req = "INSERT INTO evaluationrisque (scoreGlobal, niveauRisque, fiabiliteDonnees, facteurPrincipal, recommandation, dateEvaluation, idProjet) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evaluation.getScoreGlobal());
            pst.setString(2, evaluation.getNiveauRisque());
            pst.setString(3, evaluation.getFiabiliteDonnees());
            pst.setString(4, evaluation.getFacteurPrincipal());
            pst.setInt(5, evaluation.getRecommandation());
            pst.setDate(6, new java.sql.Date(evaluation.getDateEvaluation().getTime()));
            pst.setInt(7, evaluation.getIdProjet());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Évaluation ajoutée avec succès!");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'évaluation");
            throw e;
        }
    }

    @Override
    public void modifier(EvaluationRisque evaluation) throws SQLException {
        String req = "UPDATE evaluationrisque SET scoreGlobal = ?, niveauRisque = ?, fiabiliteDonnees = ?, facteurPrincipal = ?, recommandation = ?, dateEvaluation = ?, idProjet = ? WHERE idEvaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evaluation.getScoreGlobal());
            pst.setString(2, evaluation.getNiveauRisque());
            pst.setString(3, evaluation.getFiabiliteDonnees());
            pst.setString(4, evaluation.getFacteurPrincipal());
            pst.setInt(5, evaluation.getRecommandation());
            pst.setDate(6, new java.sql.Date(evaluation.getDateEvaluation().getTime()));
            pst.setInt(7, evaluation.getIdProjet());
            pst.setInt(8, evaluation.getIdEvaluation());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Évaluation modifiée avec succès! ID: " + evaluation.getIdEvaluation());
            } else {
                System.out.println("Aucune évaluation trouvée avec cet ID: " + evaluation.getIdEvaluation());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'évaluation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM evaluationrisque WHERE idEvaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Évaluation supprimée avec succès!");
            } else {
                System.out.println("Aucune évaluation trouvée avec cet ID");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'évaluation");
            throw e;
        }
    }

    @Override
    public List<EvaluationRisque> afficher() throws SQLException {
        List<EvaluationRisque> evaluations = new ArrayList<>();
        String req = "SELECT * FROM evaluationrisque";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                EvaluationRisque evaluation = new EvaluationRisque(
                        rs.getInt("idEvaluation"),
                        rs.getInt("scoreGlobal"),
                        rs.getString("niveauRisque"),
                        rs.getString("fiabiliteDonnees"),
                        rs.getString("facteurPrincipal"),
                        rs.getInt("recommandation"),
                        rs.getDate("dateEvaluation"),
                        rs.getInt("idProjet")
                );
                evaluations.add(evaluation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage des évaluations");
            throw e;
        }

        return evaluations;
    }

    public EvaluationRisque getById(int idEvaluation) throws SQLException {
        String req = "SELECT * FROM evaluationrisque WHERE idEvaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idEvaluation);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new EvaluationRisque(
                            rs.getInt("idEvaluation"),
                            rs.getInt("scoreGlobal"),
                            rs.getString("niveauRisque"),
                            rs.getString("fiabiliteDonnees"),
                            rs.getString("facteurPrincipal"),
                            rs.getInt("recommandation"),
                            rs.getDate("dateEvaluation"),
                            rs.getInt("idProjet")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'évaluation: " + e.getMessage());
            throw e;
        }

        return null;
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
}
