package test;

import entities.DecisionFinanciere;
import org.junit.jupiter.api.*;
import services.ServiceDecisionFinanciere;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceDecisionFinanciereTest {

    static ServiceDecisionFinanciere service;
    static int idDecisionTest;

    @BeforeAll
    static void setup() {
        service = new ServiceDecisionFinanciere();
    }

    @Test
    @Order(1)
    void testAjouterDecision() throws SQLException {
        DecisionFinanciere decision = new DecisionFinanciere();
        decision.setStatut("En attente");
        decision.setJustification("Test unitaire : Ajout d'une décision");
        decision.setDateDecision(new Date());
        decision.setIdProjet(999);

        service.ajouter(decision);

        List<DecisionFinanciere> decisions = service.afficher();
        assertFalse(decisions.isEmpty(), "La liste des décisions ne doit pas être vide après ajout");

        idDecisionTest = decisions.get(decisions.size() - 1).getIdDecision();
        System.out.println("✅ Décision ajoutée avec l'ID : " + idDecisionTest);
    }

    @Test
    @Order(2)
    void testModifierDecision() throws SQLException {
        System.out.println("--- Avant modification ---");
        service.afficher().forEach(d -> System.out.println(d));

        DecisionFinanciere decision = new DecisionFinanciere();
        decision.setIdDecision(idDecisionTest);
        decision.setStatut("Approuvé");
        decision.setJustification("Test unitaire : Décision modifiée");
        decision.setDateDecision(new Date());
        decision.setIdProjet(999);

        service.modifier(decision);

        System.out.println("--- Après modification ---");
        service.afficher().forEach(d -> System.out.println(d));

        List<DecisionFinanciere> decisions = service.afficher();
        boolean trouve = decisions.stream().anyMatch(d ->
                d.getIdDecision() == idDecisionTest &&
                        "Approuvé".equals(d.getStatut()) &&
                        "Test unitaire : Décision modifiée".equals(d.getJustification())
        );

        assertTrue(trouve, "La décision doit être modifiée dans la base");
        System.out.println("✅ Décision modifiée avec succès");
    }

    /*
    @Test
    @Order(3)
    void testSupprimerDecision() throws SQLException {
        service.supprimer(idDecisionTest);
        List<DecisionFinanciere> decisions = service.afficher();
        boolean existe = decisions.stream().anyMatch(d -> d.getIdDecision() == idDecisionTest);
        assertFalse(existe, "La décision ne doit plus exister après suppression");
        System.out.println("✅ Décision supprimée avec succès");
    }
    */

    //@AfterAll
    /*static void cleanUp() throws SQLException {
        List<DecisionFinanciere> decisions = service.afficher();
        decisions.stream()
                .filter(d -> d.getJustification().contains("Test unitaire"))
                .forEach(d -> {
                    try {
                        service.supprimer(d.getIdDecision());
                    } catch (SQLException e) {
                        System.err.println("Erreur lors du nettoyage : " + e.getMessage());
                    }
                });
    }*/
}
