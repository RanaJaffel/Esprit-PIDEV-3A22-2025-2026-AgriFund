package com.agrifund.services;

import com.agrifund.entities.DecisionFinanciere;
import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.projectagricole;
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
        String req = "INSERT INTO decisionfinanciere (statut, justification, dateDecision, idEvaluation, banqueId) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, decision.getStatut());
            pst.setString(2, decision.getJustification());
            pst.setDate(3, new java.sql.Date(decision.getDateDecision().getTime()));
            pst.setInt(4, decision.getIdEvaluation());
            pst.setInt(5, decision.getBanqueId());

            pst.executeUpdate();

            // ✅ Envoyer l'email à l'agriculteur
            envoyerEmailAgriculteur(decision);

            System.out.println("✅ Décision ajoutée et email envoyé!");
        }
    }

    /**
     * ✅ NOUVEAU - Envoyer email à l'agriculteur concerné
     */
    private void envoyerEmailAgriculteur(DecisionFinanciere decision) {
        try {
            // Récupérer l'évaluation pour avoir l'ID du projet
            ServiceEvaluationRisque evalService = new ServiceEvaluationRisque();
            var evaluation = evalService.getById(decision.getIdEvaluation());

            if (evaluation == null) return;

            // Récupérer le projet
            ServiceProjectAgricoleChedy projetService = new ServiceProjectAgricoleChedy();
            projectagricole projet = projetService.getById(evaluation.getIdProjet());

            if (projet == null) return;

            // Récupérer l'agriculteur
            AgriculteurService agriculteurService = new AgriculteurService();
            Agriculteur agriculteur = agriculteurService.rechercherParId(projet.getAgriculteurId());

            if (agriculteur == null || agriculteur.getEmail() == null) return;

            // Récupérer le nom de la banque
            BanqueService banqueService = new BanqueService();
            var banque = banqueService.rechercherParUtilisateurId(decision.getBanqueId());
            String nomBanque = banque != null ? banque.getNom() : "AgriFund";

            // Envoyer l'email
            EmailService.envoyerNotificationDecision(
                    agriculteur.getEmail(),
                    agriculteur.getNom() + " " + agriculteur.getPrenom(),
                    projet.getNomproject(),
                    decision.getStatut(),
                    decision.getJustification(),
                    nomBanque
            );

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }

    @Override
    public void modifier(DecisionFinanciere decision) throws SQLException {
        String req = "UPDATE decisionfinanciere SET statut=?, justification=?, dateDecision=?, idEvaluation=? " +
                "WHERE idDecision=? AND banqueId=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, decision.getStatut());
            pst.setString(2, decision.getJustification());
            pst.setDate(3, new java.sql.Date(decision.getDateDecision().getTime()));
            pst.setInt(4, decision.getIdEvaluation());
            pst.setInt(5, decision.getIdDecision());
            pst.setInt(6, decision.getBanqueId());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                // ✅ Envoyer email de mise à jour
                envoyerEmailAgriculteur(decision);
            }
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM decisionfinanciere WHERE idDecision=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public void supprimerParBanque(int idDecision, int banqueId) throws SQLException {
        String req = "DELETE FROM decisionfinanciere WHERE idDecision=? AND banqueId=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idDecision);
            pst.setInt(2, banqueId);
            pst.executeUpdate();
        }
    }

    @Override
    public List<DecisionFinanciere> afficher() throws SQLException {
        List<DecisionFinanciere> list = new ArrayList<>();
        String req = "SELECT * FROM decisionfinanciere";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    /**
     * ✅ NOUVEAU - Afficher uniquement les décisions d'une banque
     */
    public List<DecisionFinanciere> afficherParBanque(int banqueId) throws SQLException {
        List<DecisionFinanciere> list = new ArrayList<>();
        String req = "SELECT * FROM decisionfinanciere WHERE banqueId = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, banqueId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    public DecisionFinanciere getById(int idDecision) throws SQLException {
        String req = "SELECT * FROM decisionfinanciere WHERE idDecision=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, idDecision);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    private DecisionFinanciere mapResultSet(ResultSet rs) throws SQLException {
        return new DecisionFinanciere(
                rs.getInt("idDecision"),
                rs.getString("statut"),
                rs.getString("justification"),
                rs.getDate("dateDecision"),
                rs.getInt("idEvaluation"),
                rs.getInt("banqueId")
        );
    }
}
