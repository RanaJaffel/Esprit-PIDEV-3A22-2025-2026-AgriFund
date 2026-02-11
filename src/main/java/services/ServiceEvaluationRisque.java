package services;

import entities.EvaluationRisque;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvaluationRisque {

    private Connection connection;

    // Constructeur
    public ServiceEvaluationRisque() {
        this.connection = MyConnection.getInstance();
    }

    /**
     * Ajouter une nouvelle évaluation de risque
     * @param evaluation
     * @throws SQLException
     */
    public void ajouter(EvaluationRisque evaluation) throws SQLException {
        String req = "INSERT INTO EvaluationRisque (scoreGlobal, niveauRisque, fiabiliteDonnees, " +
                "facteurPrincipal, recommandation, dateEvaluation, idProjet) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
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
                // Récupérer l'ID généré automatiquement
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        evaluation.setIdEvaluation(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'évaluation: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Modifier une évaluation de risque existante
     * @param evaluation
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
            System.err.println("Erreur lors de la modification de l'évaluation: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Supprimer une évaluation de risque
     * @param idEvaluation
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
            System.err.println("Erreur lors de la suppression de l'évaluation: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Afficher toutes les évaluations de risque
     * @return List<EvaluationRisque>
     * @throws SQLException
     */
    public List<EvaluationRisque> afficher() throws SQLException {
        List<EvaluationRisque> evaluations = new ArrayList<>();
        String req = "SELECT * FROM EvaluationRisque";

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
            System.err.println("Erreur lors de l'affichage des évaluations: " + e.getMessage());
            throw e;
        }

        return evaluations;
    }

    /**
     * Récupérer une évaluation de risque par son ID
     * @param idEvaluation
     * @return EvaluationRisque ou null
     * @throws SQLException
     */
    public EvaluationRisque getById(int idEvaluation) throws SQLException {
        String req = "SELECT * FROM EvaluationRisque WHERE idEvaluation = ?";

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

    /**
     * Afficher les évaluations de risque pour un projet spécifique
     * @param idProjet
     * @return List<EvaluationRisque>
     * @throws SQLException
     */
    public List<EvaluationRisque> afficherParProjet(int idProjet) throws SQLException {
        List<EvaluationRisque> evaluations = new ArrayList<>();
        String req = "SELECT * FROM EvaluationRisque WHERE idProjet = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idProjet);

            try (ResultSet rs = pst.executeQuery()) {
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
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage des évaluations par projet: " + e.getMessage());
            throw e;
        }

        return evaluations;
    }
}
