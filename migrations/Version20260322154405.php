<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260322154405 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('DROP TABLE analyse_risque_agricole');
        $this->addSql('DROP TABLE banque');
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
        $this->addSql('ALTER TABLE admin CHANGE id id INT AUTO_INCREMENT NOT NULL, CHANGE utilisateur_id utilsateur_id_id INT NOT NULL, ADD PRIMARY KEY (id)');
        $this->addSql('ALTER TABLE admin ADD CONSTRAINT FK_880E0D768347FF32 FOREIGN KEY (utilsateur_id_id) REFERENCES utilisateur (id)');
        $this->addSql('CREATE INDEX IDX_880E0D768347FF32 ON admin (utilsateur_id_id)');
        $this->addSql('ALTER TABLE agriculteur CHANGE id id INT AUTO_INCREMENT NOT NULL, CHANGE statuscompte statuscompte VARCHAR(50) DEFAULT NULL, CHANGE compteverifie compteverifie TINYINT DEFAULT NULL, CHANGE utilisateur_id utilisateur_id_id INT NOT NULL, ADD PRIMARY KEY (id)');
        $this->addSql('ALTER TABLE agriculteur ADD CONSTRAINT FK_2366443BB981C689 FOREIGN KEY (utilisateur_id_id) REFERENCES utilisateur (id)');
        $this->addSql('CREATE INDEX IDX_2366443BB981C689 ON agriculteur (utilisateur_id_id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE analyse_risque_agricole (id INT NOT NULL, banque_id INT NOT NULL, agriculteur_id INT DEFAULT NULL, region VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, score_risque INT DEFAULT NULL COMMENT \'Score de 0 à 100\', niveau_risque VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, facteurs_risque TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, recommandations TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci`, date_analyse DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE banque (id INT NOT NULL, utilisateur_id INT NOT NULL, codebanque VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, addresseSiege VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, representantLegal VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, adresseAgence VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, logo VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, siteweb VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, statusCompte VARCHAR(50) CHARACTER SET utf8mb4 DEFAULT \'en_attente\' COLLATE `utf8mb4_general_ci`, compteVerfiee TINYINT DEFAULT 0) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE capteur (id_capteur INT NOT NULL, typeCapteur VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, localisation VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, statut VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT \'ACTIF\' NOT NULL COLLATE `utf8mb4_general_ci`, date_installation DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, idproject INT DEFAULT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE code2fa (id INT NOT NULL, utilisateur_id INT NOT NULL, code VARCHAR(6) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP, date_expiration DATETIME NOT NULL, est_utilise TINYINT DEFAULT 0, date_utilisation DATETIME DEFAULT NULL, type_envoi ENUM(\'email\', \'sms\') CHARACTER SET utf8mb4 DEFAULT \'email\' COLLATE `utf8mb4_general_ci`) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE conversation (id INT NOT NULL, utilisateur1_id INT NOT NULL, utilisateur2_id INT NOT NULL, utilisateur_min INT DEFAULT NULL, utilisateur_max INT DEFAULT NULL, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP, derniere_activite DATETIME DEFAULT CURRENT_TIMESTAMP) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE decisionfinanciere (idDecision INT NOT NULL, statut VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, justification TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, dateDecision DATETIME NOT NULL, idEvaluation INT NOT NULL, banqueId INT DEFAULT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE document (id INT NOT NULL, utilisateur_id INT NOT NULL, nom VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, type_document VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, chemin_fichier VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, taille INT DEFAULT NULL, date_upload DATETIME DEFAULT CURRENT_TIMESTAMP, date_expiration DATE DEFAULT NULL, statut VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT \'en_attente\' COLLATE `utf8mb4_general_ci`) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE donnees_satellite (id INT NOT NULL, agriculteur_id INT DEFAULT NULL, latitude DOUBLE PRECISION NOT NULL, longitude DOUBLE PRECISION NOT NULL, date_mesure DATE NOT NULL, ndvi DOUBLE PRECISION DEFAULT NULL COMMENT \'Indice de végétation normalisé (-1 à 1)\', temperature_moyenne DOUBLE PRECISION DEFAULT NULL COMMENT \'Température moyenne en °C\', precipitation DOUBLE PRECISION DEFAULT NULL COMMENT \'Précipitations en mm\', humidite DOUBLE PRECISION DEFAULT NULL COMMENT \'Humidité relative en %\', indice_secheresse DOUBLE PRECISION DEFAULT NULL COMMENT \'Indice de sécheresse (0-100)\', risque_agricole VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_unicode_ci` COMMENT \'faible, moyen, eleve, critique\', donnees_brutes JSON DEFAULT NULL COMMENT \'Données JSON brutes de l\'\'API\', date_creation DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE evaluationrisque (idEvaluation INT NOT NULL, scoreGlobal INT NOT NULL, niveauRisque VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, fiabiliteDonnees VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, facteurPrincipal TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, recommandation INT NOT NULL, dateEvaluation DATETIME NOT NULL, idProjet INT NOT NULL, banqueId INT DEFAULT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE historiqueconnexion (id INT NOT NULL, utilisateur_id INT NOT NULL, date_connexion DATETIME DEFAULT CURRENT_TIMESTAMP, adresse_ip VARCHAR(45) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, navigateur VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, systeme_exploitation VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, connexion_reussie TINYINT DEFAULT 1, methode_auth ENUM(\'password\', \'2fa\', \'token\') CHARACTER SET utf8mb4 DEFAULT \'password\' COLLATE `utf8mb4_general_ci`, localisation VARCHAR(255) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE message (id INT NOT NULL, conversation_id INT NOT NULL, expediteur_id INT NOT NULL, contenu TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, a_piece_jointe TINYINT DEFAULT 0, nb_pieces_jointes INT DEFAULT 0, date_envoi DATETIME DEFAULT CURRENT_TIMESTAMP, date_modification DATETIME DEFAULT NULL, est_lu TINYINT DEFAULT 0, est_supprime TINYINT DEFAULT 0, date_lecture DATETIME DEFAULT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE offre_financiere (id_offre INT NOT NULL, nom_offre VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, conditions TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, statut VARCHAR(30) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, id_produit INT NOT NULL, banque_id INT DEFAULT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE parametres2fa (id INT NOT NULL, utilisateur_id INT NOT NULL, est_active TINYINT DEFAULT 0, methode_preferee ENUM(\'email\', \'sms\', \'desactive\') CHARACTER SET utf8mb4 DEFAULT \'email\' COLLATE `utf8mb4_general_ci`, telephone_2fa VARCHAR(20) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, date_activation DATETIME DEFAULT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE piecejointe (id INT NOT NULL, message_id INT NOT NULL, type_fichier ENUM(\'image\', \'document\', \'audio\', \'video\', \'autre\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, nom_original VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, nom_stockage VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, chemin_fichier VARCHAR(500) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, taille_octets BIGINT NOT NULL, extension VARCHAR(10) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, mime_type VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, date_upload DATETIME DEFAULT CURRENT_TIMESTAMP) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE produit_financier (id_produit INT NOT NULL, nom_produit VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, type_financement VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, taux_interet DOUBLE PRECISION NOT NULL, montant_min DOUBLE PRECISION NOT NULL, montant_max DOUBLE PRECISION NOT NULL, regles_financieres TEXT CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, banque_id INT DEFAULT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE projectagricole (idproject INT NOT NULL, agriculteur_id INT NOT NULL, nomproject VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, surface FLOAT NOT NULL, budgetdemande NUMERIC(12, 2) NOT NULL, statut ENUM(\'en cours\', \'accepte\', \'refuse\') CHARACTER SET utf8mb4 DEFAULT \'en cours\' NOT NULL COLLATE `utf8mb4_general_ci`, datesoumission DATE NOT NULL, latitude DOUBLE PRECISION DEFAULT NULL, longitude DOUBLE PRECISION DEFAULT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE rapport_journalier (id_rapport INT NOT NULL, date_rapport DATE NOT NULL, type_mesure VARCHAR(50) CHARACTER SET latin1 NOT NULL COLLATE `latin1_swedish_ci`, moyenne DOUBLE PRECISION NOT NULL, min DOUBLE PRECISION NOT NULL, max DOUBLE PRECISION NOT NULL, id_capteur INT NOT NULL, idproject INT DEFAULT NULL, valeur_mesuree DOUBLE PRECISION DEFAULT NULL) DEFAULT CHARACTER SET latin1 COLLATE `latin1_swedish_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE releve_terrain (id_releve INT NOT NULL, type_mesure VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, valeur_mesuree DOUBLE PRECISION NOT NULL, unite VARCHAR(20) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, date_heure DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL, id_capteur INT NOT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE ressourceproject (idressource INT NOT NULL, nomressource VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, typeressource ENUM(\'equipement\', \'materiaux\', \'service\') CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, quantite INT NOT NULL, cout NUMERIC(12, 2) NOT NULL, fournisseur VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL COLLATE `utf8mb4_general_ci`, statut ENUM(\'prevu\', \'achete\') CHARACTER SET utf8mb4 DEFAULT \'prevu\' COLLATE `utf8mb4_general_ci`, dateajout DATE NOT NULL, idproject INT NOT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE statutenligne (utilisateur_id INT NOT NULL, est_en_ligne TINYINT DEFAULT 0, derniere_activite DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE tokenreinitialisation (id INT NOT NULL, utilisateur_id INT NOT NULL, token VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, date_expiration DATETIME NOT NULL, utilise TINYINT DEFAULT 0, date_utilisation DATETIME DEFAULT NULL, date_creation DATETIME DEFAULT CURRENT_TIMESTAMP) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE admin DROP FOREIGN KEY FK_880E0D768347FF32');
        $this->addSql('DROP INDEX IDX_880E0D768347FF32 ON admin');
        $this->addSql('ALTER TABLE admin MODIFY id INT NOT NULL');
        $this->addSql('ALTER TABLE admin CHANGE id id INT NOT NULL, CHANGE utilsateur_id_id utilisateur_id INT NOT NULL, DROP PRIMARY KEY');
        $this->addSql('ALTER TABLE agriculteur DROP FOREIGN KEY FK_2366443BB981C689');
        $this->addSql('DROP INDEX IDX_2366443BB981C689 ON agriculteur');
        $this->addSql('ALTER TABLE agriculteur MODIFY id INT NOT NULL');
        $this->addSql('ALTER TABLE agriculteur CHANGE id id INT NOT NULL, CHANGE statuscompte statuscompte VARCHAR(50) DEFAULT \'en_attente\', CHANGE compteverifie compteverifie TINYINT DEFAULT 0, CHANGE utilisateur_id_id utilisateur_id INT NOT NULL, DROP PRIMARY KEY');
    }
}
