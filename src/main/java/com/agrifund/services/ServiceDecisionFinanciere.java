package com.agrifund.services;

import com.agrifund.entities.DecisionFinanciere;
import com.agrifund.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDecisionFinanciere implements InterfaceCRUD<DecisionFinanciere> {

    private Connection connection;

    public ServiceDecisionFinanciere() {
        this.connection = MyConnection.getInstance().getCon();
    }

    @Override
    public void ajouter(DecisionFinanciere decision) throws SQLException {

        String req = "INSERT INTO decisionfinanciere (statut, justification, dateDecision, idEvaluation) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req)) {

            pst.setString(1, decision.getStatut());
            pst.setString(2, decision.getJustification());
            pst.setDate(3, new java.sql.Date(decision.getDateDecision().getTime()));
            pst.setInt(4, decision.getIdEvaluation());

            pst.executeUpdate();
            System.out.println("Décision financière ajoutée avec succès!");

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(DecisionFinanciere decision) throws SQLException {

        String req = "UPDATE decisionfinanciere SET statut=?, justification=?, dateDecision=?, idEvaluation=? WHERE idDecision=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {

            pst.setString(1, decision.getStatut());
            pst.setString(2, decision.getJustification());
            pst.setDate(3, new java.sql.Date(decision.getDateDecision().getTime()));
            pst.setInt(4, decision.getIdEvaluation());
            pst.setInt(5, decision.getIdDecision());

            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("Décision modifiée avec succès!");
            } else {
                System.out.println("Aucune décision trouvée avec cet ID.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM decisionfinanciere WHERE idDecision=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {

            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Décision supprimée avec succès!");

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<DecisionFinanciere> afficher() throws SQLException {

        List<DecisionFinanciere> list = new ArrayList<>();
        String req = "SELECT * FROM decisionfinanciere";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {

                DecisionFinanciere decision = new DecisionFinanciere(
                        rs.getInt("idDecision"),
                        rs.getString("statut"),
                        rs.getString("justification"),
                        rs.getDate("dateDecision"),
                        rs.getInt("idEvaluation")
                );

                list.add(decision);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage: " + e.getMessage());
            throw e;
        }

        return list;
    }

    public DecisionFinanciere getById(int idDecision) throws SQLException {

        String req = "SELECT * FROM decisionfinanciere WHERE idDecision=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {

            pst.setInt(1, idDecision);

            try (ResultSet rs = pst.executeQuery()) {

                if (rs.next()) {
                    return new DecisionFinanciere(
                            rs.getInt("idDecision"),
                            rs.getString("statut"),
                            rs.getString("justification"),
                            rs.getDate("dateDecision"),
                            rs.getInt("idEvaluation")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur getById: " + e.getMessage());
            throw e;
        }

        return null;
    }
}