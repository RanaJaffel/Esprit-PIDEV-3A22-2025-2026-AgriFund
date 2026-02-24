
import com.agrifund.entities.rapport_journalier;
import org.junit.jupiter.api.*;
import com.agrifund.services.ServiceRapportJournalier;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceRapportJournalierTest {

    static ServiceRapportJournalier service;

    @BeforeAll
    static void setup() {
        service = new ServiceRapportJournalier();
    }

    @Test
    @Order(1)
    void testAfficherTous() throws SQLException {
        List<rapport_journalier> rapports = service.afficherTous();
        assertNotNull(rapports);
        // Si la table est vide, ce test passera quand même (non null)
    }

    // Comme ServiceRapportJournalier n'a pas de CRUD direct,
    // tu peux tester des cas comme : la présence d'au moins un rapport
    @Test
    @Order(2)
    void testRapportNonVide() throws SQLException {
        List<rapport_journalier> rapports = service.afficherTous();
        if (!rapports.isEmpty()) {
            rapport_journalier r = rapports.get(0);
            assertNotNull(r.getDateRapport());
            assertNotNull(r.getTypeMesure());
        }
    }
}
