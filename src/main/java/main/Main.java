package main;

import entities.EvaluationRisque;
import entities.DecisionFinanciere;
import services.ServiceEvaluationRisque;
import services.ServiceDecisionFinanciere;
import utils.MyConnection;

import java.sql.SQLException;
import java.util.Date;


public class Main {

    public static void main(String[] args) {
        ServiceEvaluationRisque serviceEval = new ServiceEvaluationRisque();
        ServiceDecisionFinanciere serviceDecision = new ServiceDecisionFinanciere();

        try {


            EvaluationRisque eval1 = new EvaluationRisque(
                    70,
                    "Moyen",
                    "Élevée",
                    "Climat",
                    5,               // recommandation AI
                    new Date(),
                    2
            );
            serviceEval.ajouter(eval1);
            serviceEval.supprimer(5);


            // DÉCISIONS


            DecisionFinanciere decision1 = new DecisionFinanciere(
                    "Accepté",                      // statut (Accepté/Refusé)
                    "Projet rentable",              // justification
                    new Date(),                      // dateDecision (date du jour)
                    2                                // idProjet
            );
            serviceDecision.ajouter(decision1);
            serviceDecision.supprimer(3);


            System.out.println("Opérations terminées");

        } catch (SQLException e) {
            System.err.println(" Erreur :");
            e.printStackTrace();
        } finally {
            MyConnection.closeConnection();
        }
    }
}