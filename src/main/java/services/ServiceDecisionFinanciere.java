package services;

import entities.DecisionFinanciere;
import utils.MyConnection;

import java.sql.*;


public class ServiceDecisionFinanciere {

    private Connection connection;

    // Constructeur
    public ServiceDecisionFinanciere() {
        this.connection = MyConnection.getInstance();
    }

    public void ajouter(DecisionFinanciere decision) throws SQLException {
        String req = "INSERT INTO DecisionFinanciere (statut, justification, dateDecision, idProjet) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, decision.getStatut());
            pst.setString(2, decision.getJustification());
            pst.setDate(3, new java.sql.Date(decision.getDateDecision().getTime()));
            pst.setInt(4, decision.getIdProjet());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Décision financière ajoutée avec succès!");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la décision");
            throw e;
        }
    }

    /**
     * Méthode pour modifier une décision financière existante
     * @param decision - l'objet DecisionFinanciere avec les nouvelles valeurs
     * @throws SQLException
     */
    public void modifier(DecisionFinanciere decision) throws SQLException {
        String req = "UPDATE DecisionFinanciere SET statut = ?, justification = ?, " +
                "dateDecision = ?, idProjet = ? WHERE idDecision = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, decision.getStatut());
            pst.setString(2, decision.getJustification());
            pst.setDate(3, new java.sql.Date(decision.getDateDecision().getTime()));
            pst.setInt(4, decision.getIdProjet());
            pst.setInt(5, decision.getIdDecision());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Décision financière modifiée avec succès!");
            } else {
                System.out.println("Aucune décision trouvée avec cet ID");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la décision");
            throw e;
        }
    }

    /**
     * Méthode pour supprimer une décision financière
     * @param idDecision - l'ID de la décision à supprimer
     * @throws SQLException
     */
    public void supprimer(int idDecision) throws SQLException {
        String req = "DELETE FROM DecisionFinanciere WHERE idDecision = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idDecision);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Décision financière supprimée avec succès!");
            } else {
                System.out.println("Aucune décision trouvée avec cet ID");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la décision");
            throw e;
        }
    }
}