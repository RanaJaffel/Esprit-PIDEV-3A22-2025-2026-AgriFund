package services;

import entities.DecisionFinanciere;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDecisionFinanciere implements InterfaceCRUD<DecisionFinanciere> {

    private Connection connection;

    public ServiceDecisionFinanciere() {
        this.connection = MyConnection.getInstance();
    }

    @Override
    public void ajouter(DecisionFinanciere decision) throws SQLException {
        String req = "INSERT INTO DecisionFinanciere (statut, justification, dateDecision, idProjet) VALUES (?, ?, ?, ?)";

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

    @Override
    public void modifier(DecisionFinanciere decision) throws SQLException {
        String req = "UPDATE DecisionFinanciere SET statut = ?, justification = ?, dateDecision = ?, idProjet = ? WHERE idDecision = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, decision.getStatut());
            pst.setString(2, decision.getJustification());
            pst.setDate(3, new java.sql.Date(decision.getDateDecision().getTime()));
            pst.setInt(4, decision.getIdProjet());
            pst.setInt(5, decision.getIdDecision());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Décision financière modifiée avec succès! ID: " + decision.getIdDecision());
            } else {
                System.out.println("Aucune décision trouvée avec cet ID: " + decision.getIdDecision());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la décision: " + e.getMessage());
            throw e;
        }
    }



    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM DecisionFinanciere WHERE idDecision = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);

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

    @Override
    public List<DecisionFinanciere> afficher() throws SQLException {
        List<DecisionFinanciere> decisions = new ArrayList<>();
        String req = "SELECT * FROM DecisionFinanciere";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                DecisionFinanciere decision = new DecisionFinanciere(
                        rs.getInt("idDecision"),
                        rs.getString("statut"),
                        rs.getString("justification"),
                        rs.getDate("dateDecision"),
                        rs.getInt("idProjet")
                );
                decisions.add(decision);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage des décisions");
            throw e;
        }

        return decisions;
    }

    public DecisionFinanciere getById(int idDecision) throws SQLException {
        String req = "SELECT * FROM DecisionFinanciere WHERE idDecision = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idDecision);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new DecisionFinanciere(
                            rs.getInt("idDecision"),
                            rs.getString("statut"),
                            rs.getString("justification"),
                            rs.getDate("dateDecision"),
                            rs.getInt("idProjet")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la décision: " + e.getMessage());
            throw e;
        }

        return null;
    }

}
