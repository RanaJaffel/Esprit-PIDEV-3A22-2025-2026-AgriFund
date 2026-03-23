<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260322173328 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE historique_connexion (id INT AUTO_INCREMENT NOT NULL, date_connexion DATETIME DEFAULT NULL, adresse_ip VARCHAR(45) DEFAULT NULL, navigateur VARCHAR(255) DEFAULT NULL, systeme_exploitation VARCHAR(100) DEFAULT NULL, connexion_reussie TINYINT DEFAULT NULL, methode_auth VARCHAR(100) DEFAULT NULL, localisation VARCHAR(255) DEFAULT NULL, utilisateur_id_id INT NOT NULL, INDEX IDX_C018B2D4B981C689 (utilisateur_id_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE parametres2_fa (id INT AUTO_INCREMENT NOT NULL, est_active TINYINT DEFAULT NULL, methode_preferee VARCHAR(100) DEFAULT NULL, telephone_2fa VARCHAR(20) DEFAULT NULL, date_activation DATETIME DEFAULT NULL, utilisateur_id_id INT DEFAULT NULL, INDEX IDX_2014A37B981C689 (utilisateur_id_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE statut_en_ligne (id INT AUTO_INCREMENT NOT NULL, est_en_ligne TINYINT NOT NULL, datetime VARCHAR(255) NOT NULL, derniere_activite DATETIME DEFAULT NULL, utilisateur_id_id INT DEFAULT NULL, INDEX IDX_35AC6C13B981C689 (utilisateur_id_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('ALTER TABLE historique_connexion ADD CONSTRAINT FK_C018B2D4B981C689 FOREIGN KEY (utilisateur_id_id) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE parametres2_fa ADD CONSTRAINT FK_2014A37B981C689 FOREIGN KEY (utilisateur_id_id) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE statut_en_ligne ADD CONSTRAINT FK_35AC6C13B981C689 FOREIGN KEY (utilisateur_id_id) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE analyse_risque_agricole DROP FOREIGN KEY `analyse_risque_agricole_ibfk_1`');
        $this->addSql('ALTER TABLE analyse_risque_agricole DROP FOREIGN KEY `analyse_risque_agricole_ibfk_2`');
        $this->addSql('ALTER TABLE capteur DROP FOREIGN KEY `fk_capteur_projet`');
        $this->addSql('ALTER TABLE code2fa DROP FOREIGN KEY `code2fa_ibfk_1`');
        $this->addSql('ALTER TABLE conversation DROP FOREIGN KEY `conversation_ibfk_1`');
        $this->addSql('ALTER TABLE conversation DROP FOREIGN KEY `conversation_ibfk_2`');
        $this->addSql('ALTER TABLE decisionfinanciere DROP FOREIGN KEY `decisionfinanciere_ibfk_1`');
        $this->addSql('ALTER TABLE decisionfinanciere DROP FOREIGN KEY `fk_decision_banque`');
        $this->addSql('ALTER TABLE document DROP FOREIGN KEY `document_ibfk_1`');
        $this->addSql('ALTER TABLE donnees_satellite DROP FOREIGN KEY `donnees_satellite_ibfk_1`');
        $this->addSql('ALTER TABLE evaluationrisque DROP FOREIGN KEY `fk_evaluation_banque`');
        $this->addSql('ALTER TABLE evaluationrisque DROP FOREIGN KEY `fk_evaluation_projectagricole`');
        $this->addSql('ALTER TABLE historiqueconnexion DROP FOREIGN KEY `historiqueconnexion_ibfk_1`');
        $this->addSql('ALTER TABLE message DROP FOREIGN KEY `message_ibfk_1`');
        $this->addSql('ALTER TABLE message DROP FOREIGN KEY `message_ibfk_2`');
        $this->addSql('ALTER TABLE offre_financiere DROP FOREIGN KEY `fk_offre_banque`');
        $this->addSql('ALTER TABLE offre_financiere DROP FOREIGN KEY `fk_produit`');
        $this->addSql('ALTER TABLE parametres2fa DROP FOREIGN KEY `parametres2fa_ibfk_1`');
        $this->addSql('ALTER TABLE piecejointe DROP FOREIGN KEY `piecejointe_ibfk_1`');
        $this->addSql('ALTER TABLE produit_financier DROP FOREIGN KEY `fk_produit_banque`');
        $this->addSql('ALTER TABLE projectagricole DROP FOREIGN KEY `fk_project_agriculteur`');
        $this->addSql('ALTER TABLE rapport_journalier DROP FOREIGN KEY `fk_rapport_projet`');
        $this->addSql('ALTER TABLE releve_terrain DROP FOREIGN KEY `releve_terrain_ibfk_1`');
        $this->addSql('ALTER TABLE ressourceproject DROP FOREIGN KEY `fk_ressource_project`');
        $this->addSql('ALTER TABLE statutenligne DROP FOREIGN KEY `statutenligne_ibfk_1`');
        $this->addSql('ALTER TABLE tokenreinitialisation DROP FOREIGN KEY `tokenreinitialisation_ibfk_1`');
        $this->addSql('DROP TABLE analyse_risque_agricole');
        $this->addSql('DROP TABLE capteur');
        $this->addSql('DROP TABLE code2fa');
        $this->addSql('DROP TABLE conversation');
        $this->addSql('DROP TABLE decisionfinanciere');
        $this->addSql('DROP TABLE document');
        $this->addSql('DROP TABLE donnees_satellite');
        $this->addSql('DROP TABLE evaluationrisque');
        $this->addSql('DROP TABLE historiqueconnexion');
        $this->addSql('DROP TABLE message');
        $this->addSql('DROP TABLE offre_financiere');
        $this->addSql('DROP TABLE parametres2fa');
        $this->addSql('DROP TABLE piecejointe');
        $this->addSql('DROP TABLE produit_financier');
        $this->addSql('DROP TABLE projectagricole');
        $this->addSql('DROP TABLE rapport_journalier');
        $this->addSql('DROP TABLE releve_terrain');
        $this->addSql('DROP TABLE ressourceproject');
        $this->addSql('DROP TABLE statutenligne');
        $this->addSql('DROP TABLE tokenreinitialisation');
        $this->addSql('ALTER TABLE admin DROP FOREIGN KEY `admin_ibfk_1`');
        $this->addSql('DROP INDEX utilisateur_id ON admin');
        $this->addSql('ALTER TABLE admin CHANGE utilisateur_id utilsateur_id_id INT NOT NULL');
        $this->addSql('ALTER TABLE admin ADD CONSTRAINT FK_880E0D768347FF32 FOREIGN KEY (utilsateur_id_id) REFERENCES utilisateur (id)');
        $this->addSql('CREATE INDEX IDX_880E0D768347FF32 ON admin (utilsateur_id_id)');
        $this->addSql('ALTER TABLE agriculteur DROP FOREIGN KEY `agriculteur_ibfk_1`');
        $this->addSql('DROP INDEX utilisateur_id ON agriculteur');
        $this->addSql('DROP INDEX idx_status ON agriculteur');
        $this->addSql('DROP INDEX idx_verifie ON agriculteur');
        $this->addSql('ALTER TABLE agriculteur CHANGE statuscompte statuscompte VARCHAR(50) DEFAULT NULL, CHANGE compteverifie compteverifie TINYINT DEFAULT NULL, CHANGE utilisateur_id utilisateur_id_id INT NOT NULL');
        $this->addSql('ALTER TABLE agriculteur ADD CONSTRAINT FK_2366443BB981C689 FOREIGN KEY (utilisateur_id_id) REFERENCES utilisateur (id)');
        $this->addSql('CREATE INDEX IDX_2366443BB981C689 ON agriculteur (utilisateur_id_id)');
        $this->addSql('ALTER TABLE banque DROP FOREIGN KEY `banque_ibfk_1`');
        $this->addSql('DROP INDEX idx_code ON banque');
        $this->addSql('DROP INDEX idx_status ON banque');
        $this->addSql('DROP INDEX codebanque ON banque');
        $this->addSql('DROP INDEX utilisateur_id ON banque');
        $this->addSql('ALTER TABLE banque ADD addresse_siege VARCHAR(255) DEFAULT NULL, ADD representant_legal VARCHAR(255) DEFAULT NULL, ADD adresse_agence VARCHAR(255) DEFAULT NULL, ADD status_compte VARCHAR(255) DEFAULT NULL, DROP addresseSiege, DROP representantLegal, DROP adresseAgence, DROP statusCompte, CHANGE compteVerfiee compteverfiee TINYINT DEFAULT NULL, CHANGE utilisateur_id utilisateur_id_id INT NOT NULL');
        $this->addSql('ALTER TABLE banque ADD CONSTRAINT FK_B1F6CB3CB981C689 FOREIGN KEY (utilisateur_id_id) REFERENCES utilisateur (id)');
        $this->addSql('CREATE INDEX IDX_B1F6CB3CB981C689 ON banque (utilisateur_id_id)');
        $this->addSql('DROP INDEX email ON utilisateur');
        $this->addSql('DROP INDEX idx_email ON utilisateur');
        $this->addSql('ALTER TABLE utilisateur CHANGE date_inscrit date_inscrit DATETIME DEFAULT NULL, CHANGE derniere_connexion derniere_connexion DATETIME NOT NULL, CHANGE est_en_ligne est_en_ligne TINYINT DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE analyse_risque_agricole (id INT AUTO_INCREMENT NOT NULL, banque_id INT NOT NULL, agriculteur_id INT DEFAULT NULL, region VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, score_risque INT DEFAULT NULL COMMENT \'Score de 0 à 100\', niveau_risque VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, facteurs_risque TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, recommandations TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, date_analyse DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX agriculteur_id (agriculteur_id), INDEX idx_analyse_banque (banque_id), INDEX idx_analyse_date (date_analyse), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE capteur (id_capteur INT AUTO_INCREMENT NOT NULL, typeCapteur VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, localisation VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, statut VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT \'ACTIF\' NOT NULL COLLATE `utf8mb4_general_ci`, date_installation DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, idproject INT DEFAULT NULL, INDEX idx_statut (statut), INDEX idx_type (typeCapteur), INDEX fk_capteur_projet (idproject), PRIMARY KEY (id_capteur)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE code2fa (id INT AUTO_INCREMENT NOT NULL, utilisateur_id INT NOT NULL, code VARCHAR(6) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP, date_expiration DATETIME NOT NULL, est_utilise TINYINT DEFAULT 0, date_utilisation DATETIME DEFAULT NULL, type_envoi ENUM(\'email\', \'sms\') CHARACTER SET utf8mb4 DEFAULT \'email\' COLLATE `utf8mb4_general_ci`, INDEX idx_utilisateur (utilisateur_id), INDEX idx_code (code), INDEX idx_expiration (date_expiration), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE conversation (id INT AUTO_INCREMENT NOT NULL, utilisateur1_id INT NOT NULL, utilisateur2_id INT NOT NULL, utilisateur_min INT DEFAULT NULL, utilisateur_max INT DEFAULT NULL, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP, derniere_activite DATETIME DEFAULT CURRENT_TIMESTAMP, INDEX idx_utilisateur1 (utilisateur1_id), INDEX idx_utilisateur2 (utilisateur2_id), UNIQUE INDEX unique_conversation (utilisateur_min, utilisateur_max), INDEX idx_activite (derniere_activite), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE decisionfinanciere (idDecision INT AUTO_INCREMENT NOT NULL, statut VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, justification TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, dateDecision DATETIME NOT NULL, idEvaluation INT NOT NULL, banqueId INT DEFAULT NULL, INDEX decisionfinanciere_ibfk_1 (idEvaluation), INDEX fk_decision_banque (banqueId), PRIMARY KEY (idDecision)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE document (id INT AUTO_INCREMENT NOT NULL, utilisateur_id INT NOT NULL, nom VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, type_document VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, chemin_fichier VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, taille INT DEFAULT NULL, date_upload DATETIME DEFAULT CURRENT_TIMESTAMP, date_expiration DATE DEFAULT NULL, statut VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT \'en_attente\' COLLATE `utf8mb4_general_ci`, INDEX idx_type (type_document), INDEX idx_utilisateur (utilisateur_id), INDEX idx_statut (statut), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE donnees_satellite (id INT AUTO_INCREMENT NOT NULL, agriculteur_id INT DEFAULT NULL, latitude DOUBLE PRECISION NOT NULL, longitude DOUBLE PRECISION NOT NULL, date_mesure DATE NOT NULL, ndvi DOUBLE PRECISION DEFAULT NULL COMMENT \'Indice de végétation normalisé (-1 à 1)\', temperature_moyenne DOUBLE PRECISION DEFAULT NULL COMMENT \'Température moyenne en °C\', precipitation DOUBLE PRECISION DEFAULT NULL COMMENT \'Précipitations en mm\', humidite DOUBLE PRECISION DEFAULT NULL COMMENT \'Humidité relative en %\', indice_secheresse DOUBLE PRECISION DEFAULT NULL COMMENT \'Indice de sécheresse (0-100)\', risque_agricole VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci` COMMENT \'faible, moyen, eleve, critique\', donnees_brutes JSON DEFAULT NULL COMMENT \'Données JSON brutes de l\'\'API\', date_creation DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_satellite_coords (latitude, longitude), INDEX agriculteur_id (agriculteur_id), INDEX idx_satellite_date (date_mesure), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE evaluationrisque (idEvaluation INT AUTO_INCREMENT NOT NULL, scoreGlobal INT NOT NULL, niveauRisque VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, fiabiliteDonnees VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, facteurPrincipal TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, recommandation INT NOT NULL, dateEvaluation DATETIME NOT NULL, idProjet INT NOT NULL, banqueId INT DEFAULT NULL, INDEX fk_evaluation_projectagricole (idProjet), INDEX fk_evaluation_banque (banqueId), PRIMARY KEY (idEvaluation)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE historiqueconnexion (id INT AUTO_INCREMENT NOT NULL, utilisateur_id INT NOT NULL, date_connexion DATETIME DEFAULT CURRENT_TIMESTAMP, adresse_ip VARCHAR(45) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, navigateur VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, systeme_exploitation VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, connexion_reussie TINYINT DEFAULT 1, methode_auth ENUM(\'password\', \'2fa\', \'token\') CHARACTER SET utf8mb4 DEFAULT \'password\' COLLATE `utf8mb4_general_ci`, localisation VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, INDEX idx_utilisateur (utilisateur_id), INDEX idx_date (date_connexion), INDEX idx_reussie (connexion_reussie), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE message (id INT AUTO_INCREMENT NOT NULL, conversation_id INT NOT NULL, expediteur_id INT NOT NULL, contenu TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, a_piece_jointe TINYINT DEFAULT 0, nb_pieces_jointes INT DEFAULT 0, date_envoi DATETIME DEFAULT CURRENT_TIMESTAMP, date_modification DATETIME DEFAULT NULL, est_lu TINYINT DEFAULT 0, est_supprime TINYINT DEFAULT 0, date_lecture DATETIME DEFAULT NULL, INDEX idx_date (date_envoi), INDEX idx_lu (est_lu), INDEX idx_conversation (conversation_id), INDEX idx_message_lecture (date_lecture), INDEX idx_expediteur (expediteur_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE offre_financiere (id_offre INT AUTO_INCREMENT NOT NULL, nom_offre VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, conditions TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, statut VARCHAR(30) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, id_produit INT NOT NULL, banque_id INT DEFAULT NULL, INDEX fk_produit (id_produit), INDEX fk_offre_banque (banque_id), PRIMARY KEY (id_offre)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE parametres2fa (id INT AUTO_INCREMENT NOT NULL, utilisateur_id INT NOT NULL, est_active TINYINT DEFAULT 0, methode_preferee ENUM(\'email\', \'sms\', \'desactive\') CHARACTER SET utf8mb4 DEFAULT \'email\' COLLATE `utf8mb4_general_ci`, telephone_2fa VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, date_activation DATETIME DEFAULT NULL, UNIQUE INDEX utilisateur_id (utilisateur_id), INDEX idx_utilisateur (utilisateur_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE piecejointe (id INT AUTO_INCREMENT NOT NULL, message_id INT NOT NULL, type_fichier ENUM(\'image\', \'document\', \'audio\', \'video\', \'autre\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, nom_original VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, nom_stockage VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, chemin_fichier VARCHAR(500) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, taille_octets BIGINT NOT NULL, extension VARCHAR(10) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, mime_type VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, date_upload DATETIME DEFAULT CURRENT_TIMESTAMP, INDEX idx_message (message_id), INDEX idx_type (type_fichier), INDEX idx_date (date_upload), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE produit_financier (id_produit INT AUTO_INCREMENT NOT NULL, nom_produit VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, type_financement VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, taux_interet DOUBLE PRECISION NOT NULL, montant_min DOUBLE PRECISION NOT NULL, montant_max DOUBLE PRECISION NOT NULL, regles_financieres TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, banque_id INT DEFAULT NULL, INDEX fk_produit_banque (banque_id), PRIMARY KEY (id_produit)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE projectagricole (idproject INT AUTO_INCREMENT NOT NULL, agriculteur_id INT NOT NULL, nomproject VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, surface FLOAT NOT NULL, budgetdemande NUMERIC(12, 2) NOT NULL, statut ENUM(\'en cours\', \'accepte\', \'refuse\') CHARACTER SET utf8mb4 DEFAULT \'en cours\' NOT NULL COLLATE `utf8mb4_general_ci`, datesoumission DATE NOT NULL, latitude DOUBLE PRECISION DEFAULT NULL, longitude DOUBLE PRECISION DEFAULT NULL, INDEX fk_project_agriculteur (agriculteur_id), PRIMARY KEY (idproject)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE rapport_journalier (id_rapport INT AUTO_INCREMENT NOT NULL, date_rapport DATE NOT NULL, type_mesure VARCHAR(50) CHARACTER SET latin1 NOT NULL COLLATE `latin1_swedish_ci`, moyenne DOUBLE PRECISION NOT NULL, min DOUBLE PRECISION NOT NULL, max DOUBLE PRECISION NOT NULL, id_capteur INT NOT NULL, idproject INT DEFAULT NULL, valeur_mesuree DOUBLE PRECISION DEFAULT NULL, INDEX fk_rapport_projet (idproject), PRIMARY KEY (id_rapport)) DEFAULT CHARACTER SET latin1 COLLATE `latin1_swedish_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE releve_terrain (id_releve INT AUTO_INCREMENT NOT NULL, type_mesure VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, valeur_mesuree DOUBLE PRECISION NOT NULL, unite VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, date_heure DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, id_capteur INT NOT NULL, INDEX idx_capteur (id_capteur), INDEX idx_date (date_heure), INDEX idx_type (type_mesure), PRIMARY KEY (id_releve)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE ressourceproject (idressource INT AUTO_INCREMENT NOT NULL, nomressource VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, typeressource ENUM(\'equipement\', \'materiaux\', \'service\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, quantite INT NOT NULL, cout NUMERIC(12, 2) NOT NULL, fournisseur VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, statut ENUM(\'prevu\', \'achete\') CHARACTER SET utf8mb4 DEFAULT \'prevu\' COLLATE `utf8mb4_general_ci`, dateajout DATE NOT NULL, idproject INT NOT NULL, INDEX idproject (idproject), PRIMARY KEY (idressource)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE statutenligne (utilisateur_id INT NOT NULL, est_en_ligne TINYINT DEFAULT 0, derniere_activite DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, INDEX idx_statut_utilisateur (utilisateur_id), PRIMARY KEY (utilisateur_id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE tokenreinitialisation (id INT AUTO_INCREMENT NOT NULL, utilisateur_id INT NOT NULL, token VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, date_expiration DATETIME NOT NULL, utilise TINYINT DEFAULT 0, date_utilisation DATETIME DEFAULT NULL, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP, INDEX utilisateur_id (utilisateur_id), INDEX idx_token (token), INDEX idx_expiration (date_expiration), UNIQUE INDEX token (token), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE analyse_risque_agricole ADD CONSTRAINT `analyse_risque_agricole_ibfk_1` FOREIGN KEY (banque_id) REFERENCES banque (id)');
        $this->addSql('ALTER TABLE analyse_risque_agricole ADD CONSTRAINT `analyse_risque_agricole_ibfk_2` FOREIGN KEY (agriculteur_id) REFERENCES agriculteur (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE capteur ADD CONSTRAINT `fk_capteur_projet` FOREIGN KEY (idproject) REFERENCES projectagricole (idproject) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('ALTER TABLE code2fa ADD CONSTRAINT `code2fa_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE conversation ADD CONSTRAINT `conversation_ibfk_1` FOREIGN KEY (utilisateur1_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE conversation ADD CONSTRAINT `conversation_ibfk_2` FOREIGN KEY (utilisateur2_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE decisionfinanciere ADD CONSTRAINT `decisionfinanciere_ibfk_1` FOREIGN KEY (idEvaluation) REFERENCES evaluationrisque (idEvaluation) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE decisionfinanciere ADD CONSTRAINT `fk_decision_banque` FOREIGN KEY (banqueId) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE document ADD CONSTRAINT `document_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE donnees_satellite ADD CONSTRAINT `donnees_satellite_ibfk_1` FOREIGN KEY (agriculteur_id) REFERENCES agriculteur (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE evaluationrisque ADD CONSTRAINT `fk_evaluation_banque` FOREIGN KEY (banqueId) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE evaluationrisque ADD CONSTRAINT `fk_evaluation_projectagricole` FOREIGN KEY (idProjet) REFERENCES projectagricole (idproject) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE historiqueconnexion ADD CONSTRAINT `historiqueconnexion_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE message ADD CONSTRAINT `message_ibfk_1` FOREIGN KEY (conversation_id) REFERENCES conversation (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE message ADD CONSTRAINT `message_ibfk_2` FOREIGN KEY (expediteur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE offre_financiere ADD CONSTRAINT `fk_offre_banque` FOREIGN KEY (banque_id) REFERENCES banque (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE offre_financiere ADD CONSTRAINT `fk_produit` FOREIGN KEY (id_produit) REFERENCES produit_financier (id_produit) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE parametres2fa ADD CONSTRAINT `parametres2fa_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE piecejointe ADD CONSTRAINT `piecejointe_ibfk_1` FOREIGN KEY (message_id) REFERENCES message (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE produit_financier ADD CONSTRAINT `fk_produit_banque` FOREIGN KEY (banque_id) REFERENCES banque (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE projectagricole ADD CONSTRAINT `fk_project_agriculteur` FOREIGN KEY (agriculteur_id) REFERENCES agriculteur (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE rapport_journalier ADD CONSTRAINT `fk_rapport_projet` FOREIGN KEY (idproject) REFERENCES projectagricole (idproject) ON UPDATE CASCADE ON DELETE SET NULL');
        $this->addSql('ALTER TABLE releve_terrain ADD CONSTRAINT `releve_terrain_ibfk_1` FOREIGN KEY (id_capteur) REFERENCES capteur (id_capteur) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE ressourceproject ADD CONSTRAINT `fk_ressource_project` FOREIGN KEY (idproject) REFERENCES projectagricole (idproject)');
        $this->addSql('ALTER TABLE statutenligne ADD CONSTRAINT `statutenligne_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE tokenreinitialisation ADD CONSTRAINT `tokenreinitialisation_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE historique_connexion DROP FOREIGN KEY FK_C018B2D4B981C689');
        $this->addSql('ALTER TABLE parametres2_fa DROP FOREIGN KEY FK_2014A37B981C689');
        $this->addSql('ALTER TABLE statut_en_ligne DROP FOREIGN KEY FK_35AC6C13B981C689');
        $this->addSql('DROP TABLE historique_connexion');
        $this->addSql('DROP TABLE parametres2_fa');
        $this->addSql('DROP TABLE statut_en_ligne');
        $this->addSql('ALTER TABLE admin DROP FOREIGN KEY FK_880E0D768347FF32');
        $this->addSql('DROP INDEX IDX_880E0D768347FF32 ON admin');
        $this->addSql('ALTER TABLE admin CHANGE utilsateur_id_id utilisateur_id INT NOT NULL');
        $this->addSql('ALTER TABLE admin ADD CONSTRAINT `admin_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('CREATE UNIQUE INDEX utilisateur_id ON admin (utilisateur_id)');
        $this->addSql('ALTER TABLE agriculteur DROP FOREIGN KEY FK_2366443BB981C689');
        $this->addSql('DROP INDEX IDX_2366443BB981C689 ON agriculteur');
        $this->addSql('ALTER TABLE agriculteur CHANGE statuscompte statuscompte VARCHAR(50) DEFAULT \'en_attente\', CHANGE compteverifie compteverifie TINYINT DEFAULT 0, CHANGE utilisateur_id_id utilisateur_id INT NOT NULL');
        $this->addSql('ALTER TABLE agriculteur ADD CONSTRAINT `agriculteur_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('CREATE UNIQUE INDEX utilisateur_id ON agriculteur (utilisateur_id)');
        $this->addSql('CREATE INDEX idx_status ON agriculteur (statuscompte)');
        $this->addSql('CREATE INDEX idx_verifie ON agriculteur (compteverifie)');
        $this->addSql('ALTER TABLE banque DROP FOREIGN KEY FK_B1F6CB3CB981C689');
        $this->addSql('DROP INDEX IDX_B1F6CB3CB981C689 ON banque');
        $this->addSql('ALTER TABLE banque ADD addresseSiege VARCHAR(255) DEFAULT NULL, ADD representantLegal VARCHAR(255) DEFAULT NULL, ADD adresseAgence VARCHAR(255) DEFAULT NULL, ADD statusCompte VARCHAR(50) DEFAULT \'en_attente\', DROP addresse_siege, DROP representant_legal, DROP adresse_agence, DROP status_compte, CHANGE compteverfiee compteVerfiee TINYINT DEFAULT 0, CHANGE utilisateur_id_id utilisateur_id INT NOT NULL');
        $this->addSql('ALTER TABLE banque ADD CONSTRAINT `banque_ibfk_1` FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX idx_code ON banque (codebanque)');
        $this->addSql('CREATE INDEX idx_status ON banque (statusCompte)');
        $this->addSql('CREATE UNIQUE INDEX codebanque ON banque (codebanque)');
        $this->addSql('CREATE UNIQUE INDEX utilisateur_id ON banque (utilisateur_id)');
        $this->addSql('ALTER TABLE utilisateur CHANGE date_inscrit date_inscrit DATETIME DEFAULT CURRENT_TIMESTAMP, CHANGE derniere_connexion derniere_connexion DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, CHANGE est_en_ligne est_en_ligne TINYINT DEFAULT 0');
        $this->addSql('CREATE UNIQUE INDEX email ON utilisateur (email)');
        $this->addSql('CREATE INDEX idx_email ON utilisateur (email)');
    }
}
