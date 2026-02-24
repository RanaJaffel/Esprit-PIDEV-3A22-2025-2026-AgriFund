

import com.agrifund.entities.capteur;
import com.agrifund.entities.releve_terrain;
import org.junit.jupiter.api.*;
import com.agrifund.services.ServiceCapteur;
import com.agrifund.services.ServiceReleveTerrain;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceReleveTerrainTest {

    static ServiceReleveTerrain service;
    static ServiceCapteur capteurService;
    static int idCapteurTest;
    static int idReleveTest;

    @BeforeAll
    static void setup() throws SQLException {
        service = new ServiceReleveTerrain();
        capteurService = new ServiceCapteur();

        // Créer un capteur test pour éviter les problèmes de clé étrangère
        capteur c = new capteur();
        c.setTypeCapteur("Température Test");
        c.setLocalisation("Salle Test");
        c.setStatut("ACTIF");
        c.setIdProjet(1);
        capteurService.ajouter(c);

        List<capteur> capteurs = capteurService.afficher();
        idCapteurTest = capteurs.get(capteurs.size() - 1).getIdCapteur();
    }

    @Test
    @Order(1)
    void testAjouterReleve() throws SQLException {
        releve_terrain r = new releve_terrain();
        r.setTypeMesure("Température");
        r.setValeurMesuree(25.5);
        r.setUnite("°C");
        r.setIdCapteur(idCapteurTest);

        service.ajouter(r);

        List<releve_terrain> releves = service.afficher();
        assertFalse(releves.isEmpty());
        assertTrue(releves.stream().anyMatch(rel -> "Température".equals(rel.getTypeMesure())));

        idReleveTest = releves.get(releves.size() - 1).getIdReleve();
    }

    @Test
    @Order(2)
    void testModifierReleve() throws SQLException {
        releve_terrain r = new releve_terrain();
        r.setIdReleve(idReleveTest);
        r.setTypeMesure("Humidité");
        r.setValeurMesuree(60.0);
        r.setUnite("%");
        r.setIdCapteur(idCapteurTest);

        service.modifier(r);

        releve_terrain rModifie = service.getReleveById(idReleveTest);
        assertNotNull(rModifie);
        assertEquals("Humidité", rModifie.getTypeMesure());
        assertEquals(60.0, rModifie.getValeurMesuree());
    }

    @Test
    @Order(3)
    void testSupprimerReleve() throws SQLException {
        service.supprimer(idReleveTest);
        releve_terrain r = service.getReleveById(idReleveTest);
        assertNull(r);
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        // Supprimer le capteur test
        capteurService.supprimer(idCapteurTest);
    }
}
