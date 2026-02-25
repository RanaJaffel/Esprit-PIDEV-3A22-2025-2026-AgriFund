-- =====================================================
-- AgriFund Database - agrifund1.sql
-- Base de données pour la plateforme de financement agricole
-- Date: 25/02/2026
-- =====================================================

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS agrifund
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE agrifund;

-- =====================================================
-- TABLE: produit_financier
-- Produits de financement disponibles
-- =====================================================
DROP TABLE IF EXISTS offre_financiere;
DROP TABLE IF EXISTS produit_financier;

CREATE TABLE produit_financier (
    id_produit       INT AUTO_INCREMENT PRIMARY KEY,
    nom_produit      VARCHAR(255) NOT NULL,
    type_financement VARCHAR(100) NOT NULL,
    taux_interet     DOUBLE       NOT NULL,
    montant_min      DOUBLE       NOT NULL,
    montant_max      DOUBLE       NOT NULL,
    regles_financieres TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABLE: offre_financiere
-- Offres spéciales liées aux produits financiers
-- =====================================================
CREATE TABLE offre_financiere (
    id_offre    INT AUTO_INCREMENT PRIMARY KEY,
    nom_offre   VARCHAR(255) NOT NULL,
    conditions  TEXT,
    statut      VARCHAR(50)  NOT NULL,
    id_produit  INT          NOT NULL,
    CONSTRAINT fk_offre_produit
        FOREIGN KEY (id_produit) REFERENCES produit_financier(id_produit)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DONNÉES DE DÉMONSTRATION : produit_financier
-- =====================================================
INSERT INTO produit_financier (nom_produit, type_financement, taux_interet, montant_min, montant_max, regles_financieres) VALUES
('Crédit Équipement Agricole',   'Crédit',       4.50, 10000,  500000,  'Destiné à l\'achat de matériel agricole. Durée max 7 ans. Garantie sur équipement.'),
('Prêt Saisonnier Récolte',     'Prêt',         3.80, 5000,   200000,  'Financement de campagne agricole. Remboursement après récolte. Taux préférentiel.'),
('Leasing Tracteur & Machines', 'Leasing',       5.20, 50000,  1500000, 'Location avec option d\'achat. Maintenance incluse. Durée 3 à 10 ans.'),
('Subvention Jeunes Agriculteurs','Subvention',   0.00, 20000,  300000,  'Aide non remboursable pour les agriculteurs de moins de 35 ans. Dossier ONCA requis.'),
('Microfinance Petites Exploitations','Microfinance',6.00, 1000, 50000,  'Micro-crédit pour petites exploitations familiales. Pas de garantie exigée.'),
('Crédit Irrigation Moderne',    'Crédit',       4.00, 15000,  800000,  'Installation de systèmes d\'irrigation goutte-à-goutte. Subventionné à 60%.'),
('Prêt Élevage & Bétail',       'Prêt',         4.75, 8000,   400000,  'Acquisition de cheptel et aménagement d\'étables. Période de grâce de 6 mois.'),
('Leasing Serres Agricoles',     'Leasing',      5.50, 100000, 2000000, 'Construction de serres multi-chapelles. Contrat de 5 à 15 ans.'),
('Crédit Stockage & Froid',      'Crédit',       3.50, 30000,  600000,  'Chambres froides et unités de stockage. Financement jusqu\'à 80% du projet.'),
('Microfinance Apiculture',      'Microfinance', 5.00, 2000,   80000,   'Développement de ruchers. Formation incluse. Remboursement flexible.');

-- =====================================================
-- DONNÉES DE DÉMONSTRATION : offre_financiere
-- =====================================================
INSERT INTO offre_financiere (nom_offre, conditions, statut, id_produit) VALUES
('Offre Printemps 2026',             'Taux réduit de 1% sur les crédits équipement jusqu\'au 30 avril 2026.',           'Active',   1),
('Promo Campagne Agricole',          'Aucun frais de dossier pour les prêts saisonniers. Offre limitée.',                'Active',   2),
('Leasing Zéro Apport',             'Pas d\'apport initial sur le leasing tracteur pendant le mois de mars.',           'Active',   3),
('Bonus Jeunes Agriculteurs',        'Prime supplémentaire de 10 000 MAD pour les moins de 30 ans.',                     'Active',   4),
('Micro-Crédit Express',            'Déblocage sous 48h pour les montants inférieurs à 20 000 MAD.',                    'Active',   5),
('Offre Irrigation Verte',          'Subvention additionnelle de 15% pour systèmes éco-responsables.',                  'Active',   6),
('Pack Élevage Complet',            'Crédit bétail + assurance troupeau incluse. Offre valable en 2026.',              'En attente',7),
('Serres Nouvelle Génération',       'Accompagnement technique gratuit pour les 20 premiers contrats.',                  'Active',   8),
('Stockage Solidaire',              'Taux préférentiel pour coopératives de plus de 10 membres.',                       'Active',   9),
('Abeilles & Miel — Offre Spéciale','Kit de démarrage offert pour tout micro-crédit apicole supérieur à 10 000 MAD.',  'Active',  10);

-- =====================================================
-- INDEX pour les performances
-- =====================================================
CREATE INDEX idx_produit_type ON produit_financier(type_financement);
CREATE INDEX idx_offre_statut ON offre_financiere(statut);
CREATE INDEX idx_offre_produit ON offre_financiere(id_produit);
