package services;

import entities.EvaluationRisque;
import utils.MyConnection;

import java.sql.*;

/**
 * Classe de service pour gérer les opérations CRUD sur EvaluationRisque
 */
public class ServiceEvaluationRisque {

    private Connection connection;

    // Constructeur
    public ServiceEvaluationRisque() {
        this.connection = MyConnection.getInstance();
    }

    /**
     * Méthode pour ajouter une nouvelle évaluation de risque
     * @param evaluation - l'objet EvaluationRisque à ajouter
     * @throws SQLException
     */
    public void ajouter(EvaluationRisque evaluation) throws SQLException {
        String req = "INSERT INTO EvaluationRisque (scoreGlobal, niveauRisque, fiabiliteDonnees, " +
                "facteurPrincipal, recommandation, dateEvaluation, idProjet) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

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

    /**
     * Méthode pour modifier une évaluation de risque existante
     * @param evaluation - l'objet EvaluationRisque avec les nouvelles valeurs
     * @throws SQLException
     */
    public void modifier(EvaluationRisque evaluation) throws SQLException {
        String req = "UPDATE EvaluationRisque SET scoreGlobal = ?, niveauRisque = ?, " +
                "fiabiliteDonnees = ?, facteurPrincipal = ?, recommandation = ?, " +
                "dateEvaluation = ?, idProjet = ? WHERE idEvaluation = ?";

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
                System.out.println("Évaluation modifiée avec succès!");
            } else {
                System.out.println("Aucune évaluation trouvée avec cet ID");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'évaluation");
            throw e;
        }
    }

    /**
     * Méthode pour supprimer une évaluation de risque
     * @param idEvaluation - l'ID de l'évaluation à supprimer
     * @throws SQLException
     */
    public void supprimer(int idEvaluation) throws SQLException {
        String req = "DELETE FROM EvaluationRisque WHERE idEvaluation = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idEvaluation);

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
}