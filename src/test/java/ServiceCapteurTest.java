
import entities.capteur;
import org.junit.jupiter.api.*;
import services.ServiceCapteur;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceCapteurTest {

    static ServiceCapteur service;
    static int idCapteurTest;

    @BeforeAll
    static void setup() {
        service = new ServiceCapteur();
    }

    @Test
    @Order(1)
    void testAjouterCapteur() throws SQLException {
        capteur c = new capteur();
        c.setTypeCapteur("Température");
        c.setLocalisation("Salle A");
        c.setStatut("ACTIF");
        c.setIdProjet(1);
        service.ajouter(c);

        List<capteur> capteurs = service.afficher();
        assertFalse(capteurs.isEmpty());
        assertTrue(capteurs.stream().anyMatch(cap -> "Température".equals(cap.getTypeCapteur())));

        // Récupérer l'id pour les tests suivants
        idCapteurTest = capteurs.get(capteurs.size() - 1).getIdCapteur();
        System.out.println("ID Capteur Test: " + idCapteurTest);
    }

    @Test
    @Order(2)
    void testModifierCapteur() throws SQLException {
        capteur c = new capteur();
        c.setIdCapteur(idCapteurTest);
        c.setTypeCapteur("Humidité");
        c.setLocalisation("Salle B");
        c.setStatut("INACTIF");
        c.setIdProjet(1);
        service.modifier(c);

        List<capteur> capteurs = service.afficher();
        boolean trouve = capteurs.stream()
                .anyMatch(cap -> "Humidité".equals(cap.getTypeCapteur()) && cap.getIdCapteur() == idCapteurTest);
        assertTrue(trouve);
    }



    @Test
    @Order(3)
    void testSupprimerCapteur() throws SQLException {
        service.supprimer(idCapteurTest);
        List<capteur> capteurs = service.afficher();
        boolean existe = capteurs.stream().anyMatch(cap -> cap.getIdCapteur() == idCapteurTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Nettoyage éventuel si un capteur test traîne encore
        List<capteur> capteurs = service.afficher();
        for (capteur c : capteurs) {
            if (c.getTypeCapteur().equals("Température") || c.getTypeCapteur().equals("Humidité")) {
                service.supprimer(c.getIdCapteur());
            }
        }
    }
}
