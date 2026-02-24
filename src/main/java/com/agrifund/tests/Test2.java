package com.agrifund.tests;
import com.agrifund.entities.*;
import com.agrifund.services.*;
import com.agrifund.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * Classe principale - Application de Gestion des Utilisateurs
 * Menu interactif pour tester toutes les fonctionnalités JDBC
 */
public class Test2 {

    private static Scanner scanner = new Scanner(System.in);
    private static AuthService authService;
    private static UtilisateurService utilisateurService;
    private static AgriculteurService agriculteurService;
    private static BanqueService banqueService;
    private static AdminService adminService;
    private static DocumentService documentService;
    private static MessagerieService messagerieService;
    private static TwoFactorAuthService twoFAService;

    private static Utilisateur utilisateurConnecte = null;
    private static String typeUtilisateur = null;

    public static void main(String[] args) {
        try {
            // Tester la connexion à la base de données
            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║   SYSTÈME DE GESTION DES UTILISATEURS - ESPRIT JAVA   ║");
            System.out.println("║              Projet JDBC 2024/2025                     ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println();

            DatabaseConnection.testConnection();
            System.out.println();

            // Tester la configuration email
            com.agrifund.util.EmailService.testerConfiguration();
            System.out.println();

            // Initialiser les répertoires de stockage de fichiers
            com.agrifund.util.FileManager.initialiserRepertoires();
            System.out.println();

            // Initialiser les services
            authService = new AuthService();
            utilisateurService = new UtilisateurService();
            agriculteurService = new AgriculteurService();
            banqueService = new BanqueService();
            adminService = new AdminService();
            documentService = new DocumentService();
            messagerieService = new MessagerieService();
            twoFAService = new TwoFactorAuthService();

            // Initialiser les répertoires de stockage
            com.agrifund.util.MessagerieFileManager.initialiserRepertoires();

            // Afficher le menu principal
            menuPrincipal();

        } catch (SQLException e) {
            System.err.println("✗ Erreur de base de données: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            DatabaseConnection.closeConnection();
        }
    }

    /**
     * Menu Principal
     */
    private static void menuPrincipal() throws SQLException {
        boolean continuer = true;

        while (continuer) {
            if (utilisateurConnecte == null) {
                // Menu non connecté
                System.out.println("\n═══════════════ MENU PRINCIPAL ═══════════════");
                System.out.println("1. Se connecter (Login)");
                System.out.println("2. S'inscrire");
                System.out.println("3. Mot de passe oublié");
                System.out.println("0. Quitter");
                System.out.println("════════════════════════════════════════════");
                System.out.print("Votre choix: ");

                int choix = lireEntier();

                switch (choix) {
                    case 1:
                        login();
                        break;
                    case 2:
                        menuInscription();
                        break;
                    case 3:
                        motDePasseOublie();
                        break;
                    case 0:
                        continuer = false;
                        System.out.println("👋 Au revoir!");
                        break;
                    default:
                        System.out.println("⚠ Choix invalide!");
                }
            } else {
                // Menu connecté selon le type d'utilisateur
                if (typeUtilisateur.equals("ADMIN")) {
                    menuAdmin();
                } else if (typeUtilisateur.equals("AGRICULTEUR")) {
                    menuAgriculteur();
                } else if (typeUtilisateur.equals("BANQUE")) {
                    menuBanque();
                }
            }
        }
    }

    /**
     * LOGIN
     */
    private static void login() throws SQLException {
        System.out.println("\n══════════ CONNEXION ══════════");
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Mot de passe: ");
        String password = scanner.nextLine();

        utilisateurConnecte = authService.login(email, password);

        if (utilisateurConnecte != null) {
            // Vérifier si 2FA est activée
            if (twoFAService.is2FAActive(utilisateurConnecte.getId())) {
                System.out.println("\n🔐 Authentification à Deux Facteurs Activée");

                // Envoyer le code 2FA
                com.agrifund.entities.Code2FA code = twoFAService.envoyerCode2FA(utilisateurConnecte.getId());

                if (code != null) {
                    System.out.println("✓ Un code de vérification a été envoyé à votre email");
                    System.out.println("⏰ Code valide pendant 5 minutes");

                    // Demander le code
                    System.out.print("\nEntrez le code de vérification: ");
                    String codeEntre = scanner.nextLine();

                    // Vérifier le code
                    if (twoFAService.verifierCode(utilisateurConnecte.getId(), codeEntre)) {
                        System.out.println("✓ Code vérifié avec succès!");
                        typeUtilisateur = authService.getTypeUtilisateur(utilisateurConnecte.getId());

                        // Enregistrer connexion réussie
                        twoFAService.enregistrerConnexion(utilisateurConnecte.getId(), true, "2fa");

                        System.out.println("✓ Connexion sécurisée réussie!");
                        System.out.println("Type de compte: " + typeUtilisateur);
                    } else {
                        System.out.println("✗ Code incorrect ou expiré!");

                        // Enregistrer tentative échouée
                        twoFAService.enregistrerConnexion(utilisateurConnecte.getId(), false, "2fa");

                        utilisateurConnecte = null;
                        typeUtilisateur = null;

                        System.out.print("\nNouvelle tentative? (o/n): ");
                        String retry = scanner.nextLine();
                        if (retry.equalsIgnoreCase("o")) {
                            login();
                        }
                    }
                } else {
                    System.out.println("✗ Erreur lors de l'envoi du code 2FA");
                    utilisateurConnecte = null;
                    typeUtilisateur = null;
                }
            } else {
                // Connexion sans 2FA
                typeUtilisateur = authService.getTypeUtilisateur(utilisateurConnecte.getId());

                // Enregistrer connexion réussie
                twoFAService.enregistrerConnexion(utilisateurConnecte.getId(), true, "password");

                System.out.println("✓ Connexion réussie!");
                System.out.println("Type de compte: " + typeUtilisateur);
                System.out.println("💡 Conseil: Activez la 2FA pour plus de sécurité (Menu Sécurité)");
            }
        }
    }

    /**
     * Menu INSCRIPTION
     */
    private static void menuInscription() throws SQLException {
        System.out.println("\n══════════ INSCRIPTION ══════════");
        System.out.println("1. S'inscrire comme Agriculteur");
        System.out.println("2. S'inscrire comme Banque");
        System.out.println("3. Créer un compte Admin (Admin uniquement)");
        System.out.println("0. Retour");
        System.out.print("Votre choix: ");

        int choix = lireEntier();

        switch (choix) {
            case 1:
                inscriptionAgriculteur();
                break;
            case 2:
                inscriptionBanque();
                break;
            case 3:
                inscriptionAdmin();
                break;
        }
    }

    /**
     * INSCRIPTION AGRICULTEUR
     */
    private static void inscriptionAgriculteur() throws SQLException {
        System.out.println("\n═══ INSCRIPTION AGRICULTEUR ═══");

        System.out.print("Nom: ");
        String nom = scanner.nextLine();

        System.out.print("Prénom: ");
        String prenom = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Mot de passe: ");
        String password = scanner.nextLine();

        System.out.print("Téléphone: ");
        String tel = scanner.nextLine();

        System.out.print("Adresse de la ferme: ");
        String adresseFerme = scanner.nextLine();

        System.out.print("Superficie (hectares): ");
        BigDecimal superficie = new BigDecimal(scanner.nextLine());

        System.out.print("Type de culture: ");
        String typeCulture = scanner.nextLine();

        Agriculteur agriculteur = new Agriculteur(nom, prenom, email, password,
                adresseFerme, superficie, typeCulture);
        agriculteur.setTel(tel);

        authService.inscrireAgriculteur(agriculteur);
    }

    /**
     * INSCRIPTION BANQUE
     */
    private static void inscriptionBanque() throws SQLException {
        System.out.println("\n═══ INSCRIPTION BANQUE ═══");

        System.out.print("Nom de la banque: ");
        String nom = scanner.nextLine();

        System.out.print("Représentant légal: ");
        String representant = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Mot de passe: ");
        String password = scanner.nextLine();

        System.out.print("Code banque: ");
        String codeBanque = scanner.nextLine();

        System.out.print("Adresse du siège: ");
        String addresseSiege = scanner.nextLine();

        Banque banque = new Banque(nom, "", email, password,
                codeBanque, addresseSiege, representant);

        authService.inscrireBanque(banque);
    }

    /**
     * INSCRIPTION ADMIN
     */
    private static void inscriptionAdmin() throws SQLException {
        System.out.println("\n═══ CRÉATION COMPTE ADMIN ═══");
        System.out.println("⚠ Cette action nécessite des privilèges admin");

        System.out.print("Nom: ");
        String nom = scanner.nextLine();

        System.out.print("Prénom: ");
        String prenom = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Mot de passe: ");
        String password = scanner.nextLine();

        System.out.print("Téléphone: ");
        String tel = scanner.nextLine();

        authService.inscrireAdmin(nom, prenom, email, password, tel);
    }

    /**
     * MOT DE PASSE OUBLIÉ
     */
    private static void motDePasseOublie() throws SQLException {
        System.out.println("\n══════════ RÉINITIALISATION MOT DE PASSE ══════════");
        System.out.print("Email: ");
        String email = scanner.nextLine();

        String token = authService.demanderReinitialisationMotDePasse(email);

        if (token != null) {
            System.out.print("\nEntrez le token reçu: ");
            String tokenSaisi = scanner.nextLine();

            System.out.print("Nouveau mot de passe: ");
            String nouveauPassword = scanner.nextLine();

            authService.reinitialiserMotDePasse(tokenSaisi, nouveauPassword);
        }
    }

    /**
     * MENU ADMIN
     */
    private static void menuAdmin() throws SQLException {
        System.out.println("\n═══════════════ MENU ADMIN ═══════════════");
        System.out.println("Bonjour " + utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom());

        // Afficher messages non lus
        int nbNonLus = messagerieService.compterMessagesNonLus(utilisateurConnecte.getId());
        if (nbNonLus > 0) {
            System.out.println("📬 Vous avez " + nbNonLus + " message(s) non lu(s)");
        }

        System.out.println("1. Gérer les utilisateurs");
        System.out.println("2. Vérifier les comptes agriculteurs");
        System.out.println("3. Vérifier les comptes banques");
        System.out.println("4. Valider les documents");
        System.out.println("5. Statistiques");
        System.out.println("6. Mon profil");
        System.out.println("7. 💬 Messagerie");
        System.out.println("8. 🔐 Sécurité (2FA)");
        System.out.println("0. Se déconnecter");
        System.out.println("══════════════════════════════════════════");
        System.out.print("Votre choix: ");

        int choix = lireEntier();

        switch (choix) {
            case 1:
                gererUtilisateurs();
                break;
            case 2:
                verifierComptesAgriculteurs();
                break;
            case 3:
                verifierComptesBanques();
                break;
            case 4:
                validerDocuments();
                break;
            case 5:
                afficherStatistiques();
                break;
            case 6:
                modifierProfilAdmin();
                break;
            case 7:
                menuMessagerie();
                break;
            case 8:
                menuSecurite();
                break;
            case 0:
                deconnexion();
                break;
            default:
                System.out.println("⚠ Choix invalide!");
        }
    }

    /**
     * GÉRER UTILISATEURS (Admin)
     */
    private static void gererUtilisateurs() throws SQLException {
        System.out.println("\n═══ GESTION DES UTILISATEURS ═══");
        System.out.println("1. Afficher tous les utilisateurs");
        System.out.println("2. Rechercher un utilisateur");
        System.out.println("3. Filtrer les utilisateurs");
        System.out.println("4. Modifier un utilisateur");
        System.out.println("5. Supprimer un utilisateur");
        System.out.print("Votre choix: ");

        int choix = lireEntier();

        if (choix == 1) {
            afficherListeUtilisateurs();
        } else if (choix == 2) {
            rechercherUtilisateur();
        } else if (choix == 3) {
            filtrerUtilisateurs();
        } else if (choix == 4) {
            System.out.print("ID de l'utilisateur à modifier: ");
            int id = lireEntier();
            modifierUtilisateurParAdmin(id);
        } else if (choix == 5) {
            System.out.print("ID de l'utilisateur à supprimer: ");
            int id = lireEntier();

            System.out.print("⚠ Confirmer la suppression? (o/n): ");
            String confirm = scanner.nextLine();

            if (confirm.equalsIgnoreCase("o")) {
                utilisateurService.supprimer(id);
            } else {
                System.out.println("✗ Suppression annulée");
            }
        }
    }

    /**
     * RECHERCHER UN UTILISATEUR
     */
    private static void rechercherUtilisateur() throws SQLException {
        System.out.println("\n═══ RECHERCHE D'UTILISATEUR ═══");
        System.out.println("1. Par nom/prénom");
        System.out.println("2. Par email");
        System.out.println("3. Par ID");
        System.out.print("Votre choix: ");

        int choix = lireEntier();
        List<Utilisateur> resultats = new ArrayList<>();

        if (choix == 1) {
            System.out.print("Entrez le nom ou prénom (partiel accepté): ");
            String recherche = scanner.nextLine().toLowerCase();

            List<Utilisateur> tous = utilisateurService.afficherTous();
            for (Utilisateur u : tous) {
                String nomComplet = (u.getPrenom() + " " + u.getNom()).toLowerCase();
                if (nomComplet.contains(recherche) ||
                        u.getNom().toLowerCase().contains(recherche) ||
                        u.getPrenom().toLowerCase().contains(recherche)) {
                    resultats.add(u);
                }
            }

        } else if (choix == 2) {
            System.out.print("Entrez l'email (partiel accepté): ");
            String email = scanner.nextLine().toLowerCase();

            List<Utilisateur> tous = utilisateurService.afficherTous();
            for (Utilisateur u : tous) {
                if (u.getEmail().toLowerCase().contains(email)) {
                    resultats.add(u);
                }
            }

        } else if (choix == 3) {
            System.out.print("Entrez l'ID: ");
            int id = lireEntier();

            Utilisateur u = utilisateurService.rechercherParId(id);
            if (u != null) {
                resultats.add(u);
            }
        }

        // Afficher les résultats
        afficherResultatsRecherche(resultats);
    }

    /**
     * FILTRER LES UTILISATEURS
     */
    private static void filtrerUtilisateurs() throws SQLException {
        System.out.println("\n═══ FILTRER LES UTILISATEURS ═══");
        System.out.println("1. Par type (Admin/Agriculteur/Banque)");
        System.out.println("2. Par statut de compte");
        System.out.println("3. Avec/Sans photo de profil");
        System.out.println("4. Avec 2FA activée");
        System.out.println("5. Inscrits cette semaine");
        System.out.print("Votre choix: ");

        int choix = lireEntier();
        List<Utilisateur> resultats = new ArrayList<>();
        List<Utilisateur> tous = utilisateurService.afficherTous();

        if (choix == 1) {
            System.out.println("\nType d'utilisateur :");
            System.out.println("1. Admin");
            System.out.println("2. Agriculteur");
            System.out.println("3. Banque");
            System.out.print("Choix: ");
            int typeChoix = lireEntier();

            String typeRecherche = "";
            switch (typeChoix) {
                case 1: typeRecherche = "ADMIN"; break;
                case 2: typeRecherche = "AGRICULTEUR"; break;
                case 3: typeRecherche = "BANQUE"; break;
            }

            for (Utilisateur u : tous) {
                try {
                    String type = authService.getTypeUtilisateur(u.getId());
                    if (type.equals(typeRecherche)) {
                        resultats.add(u);
                    }
                } catch (Exception e) {
                    // Ignorer
                }
            }

        } else if (choix == 2) {
            System.out.println("\nStatut de compte :");
            System.out.println("1. Actif");
            System.out.println("2. Inactif");
            System.out.println("3. Suspendu");
            System.out.print("Choix: ");
            int statutChoix = lireEntier();

            String statutRecherche = "";
            switch (statutChoix) {
                case 1: statutRecherche = "actif"; break;
                case 2: statutRecherche = "inactif"; break;
                case 3: statutRecherche = "suspendu"; break;
            }

            // Pour l'instant, tous les utilisateurs sont "actifs" par défaut
            // Cette fonctionnalité nécessiterait une colonne statut dans Utilisateur
            System.out.println("⚠ Fonctionnalité en développement");

        } else if (choix == 3) {
            System.out.println("\n1. Avec photo");
            System.out.println("2. Sans photo");
            System.out.print("Choix: ");
            int photoChoix = lireEntier();

            for (Utilisateur u : tous) {
                boolean aPhoto = u.getPhoto() != null && !u.getPhoto().isEmpty();

                if ((photoChoix == 1 && aPhoto) || (photoChoix == 2 && !aPhoto)) {
                    resultats.add(u);
                }
            }

        } else if (choix == 4) {
            System.out.println("\nRecherche des utilisateurs avec 2FA activée...");

            for (Utilisateur u : tous) {
                try {
                    if (twoFAService.is2FAActive(u.getId())) {
                        resultats.add(u);
                    }
                } catch (Exception e) {
                    // Ignorer
                }
            }

        } else if (choix == 5) {
            System.out.println("\nRecherche des inscriptions de cette semaine...");

            java.time.LocalDateTime maintenant = java.time.LocalDateTime.now();
            java.time.LocalDateTime debutSemaine = maintenant.minusDays(7);

            for (Utilisateur u : tous) {
                if (u.getDateInscrit() != null) {
                    if (u.getDateInscrit().isAfter(debutSemaine)) {
                        resultats.add(u);
                    }
                }
            }
        }

        // Afficher les résultats
        afficherResultatsRecherche(resultats);
    }

    /**
     * AFFICHER LES RÉSULTATS DE RECHERCHE
     */
    private static void afficherResultatsRecherche(List<Utilisateur> resultats) throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      RÉSULTATS DE LA RECHERCHE                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");

        if (resultats.isEmpty()) {
            System.out.println("\n❌ Aucun utilisateur trouvé");
            System.out.print("\nAppuyez sur Entrée pour continuer...");
            scanner.nextLine();
            return;
        }

        System.out.println("\nNombre de résultats : " + resultats.size() + "\n");

        for (Utilisateur u : resultats) {
            // Déterminer le type d'utilisateur
            String typeUser = "Utilisateur";
            try {
                String type = authService.getTypeUtilisateur(u.getId());
                switch (type) {
                    case "ADMIN": typeUser = "👨‍💼 Admin"; break;
                    case "AGRICULTEUR": typeUser = "🌾 Agriculteur"; break;
                    case "BANQUE": typeUser = "🏦 Banque"; break;
                }
            } catch (Exception e) {
                // Type inconnu
            }

            System.out.println("┌─────────────────────────────────────────────────────────────────────────┐");
            System.out.println("│ ID: " + u.getId() + " │ " + typeUser);
            System.out.println("├─────────────────────────────────────────────────────────────────────────┤");

            // Photo de profil
            if (u.getPhoto() != null && !u.getPhoto().isEmpty()) {
                if (com.agrifund.util.FileManager.fichierExiste(u.getPhoto())) {
                    long taille = com.agrifund.util.FileManager.getTailleFichier(u.getPhoto());
                    System.out.println("│ 📷 Photo     : " + u.getPhoto());
                    System.out.println("│              (" + com.agrifund.util.FileManager.formaterTaille(taille) + ")");
                } else {
                    System.out.println("│ 📷 Photo     : " + u.getPhoto() + " ⚠️ (fichier manquant)");
                }
            } else {
                System.out.println("│ 📷 Photo     : Aucune photo de profil");
            }

            // Informations
            System.out.println("│ 👤 Nom       : " + u.getPrenom() + " " + u.getNom());
            System.out.println("│ 📧 Email     : " + u.getEmail());
            System.out.println("│ 📱 Téléphone : " + (u.getTel() != null ? u.getTel() : "Non renseigné"));

            // 2FA
            try {
                if (twoFAService.is2FAActive(u.getId())) {
                    System.out.println("│ 🔐 2FA       : ✅ Activée");
                } else {
                    System.out.println("│ 🔐 2FA       : ❌ Désactivée");
                }
            } catch (Exception e) {
                System.out.println("│ 🔐 2FA       : Inconnue");
            }

            if (u.getDateInscrit() != null) {
                System.out.println("│ 📅 Inscrit   : " + u.getDateInscrit().toString().substring(0, 10));
            }

            System.out.println("└─────────────────────────────────────────────────────────────────────────┘");
            System.out.println();
        }

        System.out.println("\nActions disponibles :");
        System.out.println("1. Modifier un utilisateur de cette liste");
        System.out.println("2. Supprimer un utilisateur de cette liste");
        System.out.println("0. Retour");
        System.out.print("Votre choix: ");

        int action = lireEntier();

        if (action == 1) {
            System.out.print("ID de l'utilisateur à modifier: ");
            int id = lireEntier();

            // Vérifier que l'ID est dans les résultats
            boolean trouve = false;
            for (Utilisateur u : resultats) {
                if (u.getId() == id) {
                    trouve = true;
                    break;
                }
            }

            if (trouve) {
                modifierUtilisateurParAdmin(id);
            } else {
                System.out.println("✗ Cet ID n'est pas dans les résultats");
            }

        } else if (action == 2) {
            System.out.print("ID de l'utilisateur à supprimer: ");
            int id = lireEntier();

            boolean trouve = false;
            for (Utilisateur u : resultats) {
                if (u.getId() == id) {
                    trouve = true;
                    break;
                }
            }

            if (trouve) {
                System.out.print("⚠ Confirmer la suppression? (o/n): ");
                String confirm = scanner.nextLine();

                if (confirm.equalsIgnoreCase("o")) {
                    utilisateurService.supprimer(id);
                }
            } else {
                System.out.println("✗ Cet ID n'est pas dans les résultats");
            }
        }
    }

    /**
     * AFFICHER LA LISTE DES UTILISATEURS AVEC PHOTOS
     */
    private static void afficherListeUtilisateurs() throws SQLException {
        List<Utilisateur> users = utilisateurService.afficherTous();

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         LISTE DES UTILISATEURS                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("\nTotal: " + users.size() + " utilisateur(s)\n");

        for (Utilisateur u : users) {
            // Déterminer le type d'utilisateur
            String typeUser = "Utilisateur";
            try {
                String type = authService.getTypeUtilisateur(u.getId());
                switch (type) {
                    case "ADMIN": typeUser = "👨‍💼 Admin"; break;
                    case "AGRICULTEUR": typeUser = "🌾 Agriculteur"; break;
                    case "BANQUE": typeUser = "🏦 Banque"; break;
                }
            } catch (Exception e) {
                // Type inconnu
            }

            System.out.println("┌─────────────────────────────────────────────────────────────────────────┐");
            System.out.println("│ ID: " + u.getId() + " │ " + typeUser);
            System.out.println("├─────────────────────────────────────────────────────────────────────────┤");

            // Photo de profil
            if (u.getPhoto() != null && !u.getPhoto().isEmpty()) {
                if (com.agrifund.util.FileManager.fichierExiste(u.getPhoto())) {
                    long taille = com.agrifund.util.FileManager.getTailleFichier(u.getPhoto());
                    System.out.println("│ 📷 Photo     : " + u.getPhoto());
                    System.out.println("│              (" + com.agrifund.util.FileManager.formaterTaille(taille) + ")");
                } else {
                    System.out.println("│ 📷 Photo     : " + u.getPhoto() + " ⚠️ (fichier manquant)");
                }
            } else {
                System.out.println("│ 📷 Photo     : Aucune photo de profil");
            }

            // Informations
            System.out.println("│ 👤 Nom       : " + u.getPrenom() + " " + u.getNom());
            System.out.println("│ 📧 Email     : " + u.getEmail());
            System.out.println("│ 📱 Téléphone : " + (u.getTel() != null ? u.getTel() : "Non renseigné"));

            if (u.getDateInscrit() != null) {
                System.out.println("│ 📅 Inscrit   : " + u.getDateInscrit().toString().substring(0, 10));
            }

            System.out.println("└─────────────────────────────────────────────────────────────────────────┘");
            System.out.println();
        }
    }

    /**
     * MODIFIER UN UTILISATEUR (Admin)
     */
    private static void modifierUtilisateurParAdmin(int userId) throws SQLException {
        Utilisateur user = utilisateurService.rechercherParId(userId);

        if (user == null) {
            System.out.println("✗ Utilisateur non trouvé!");
            return;
        }

        System.out.println("\n═══ MODIFICATION UTILISATEUR ═══");
        System.out.println("Utilisateur actuel: " + user.getPrenom() + " " + user.getNom());
        System.out.println("Email: " + user.getEmail());

        System.out.print("Nouveau nom (Entrée pour garder '" + user.getNom() + "'): ");
        String nom = scanner.nextLine();
        if (!nom.trim().isEmpty()) {
            user.setNom(nom);
        }

        System.out.print("Nouveau prénom (Entrée pour garder '" + user.getPrenom() + "'): ");
        String prenom = scanner.nextLine();
        if (!prenom.trim().isEmpty()) {
            user.setPrenom(prenom);
        }

        System.out.print("Nouveau email (Entrée pour garder '" + user.getEmail() + "'): ");
        String email = scanner.nextLine();
        if (!email.trim().isEmpty()) {
            // Vérifier si le nouvel email n'existe pas déjà
            if (!email.equals(user.getEmail()) && utilisateurService.emailExiste(email)) {
                System.out.println("✗ Cet email est déjà utilisé!");
                return;
            }
            user.setEmail(email);
        }

        System.out.print("Nouveau téléphone (Entrée pour garder '" + user.getTel() + "'): ");
        String tel = scanner.nextLine();
        if (!tel.trim().isEmpty()) {
            user.setTel(tel);
        }

        System.out.print("Réinitialiser le mot de passe? (o/n): ");
        String resetPwd = scanner.nextLine();
        if (resetPwd.equalsIgnoreCase("o")) {
            System.out.print("Nouveau mot de passe: ");
            String newPwd = scanner.nextLine();
            utilisateurService.modifierMotDePasse(user.getId(), newPwd);
        } else {
            utilisateurService.modifier(user);
        }

        System.out.println("✓ Utilisateur modifié avec succès!");
    }

    /**
     * VÉRIFIER COMPTES AGRICULTEURS
     */
    private static void verifierComptesAgriculteurs() throws SQLException {
        List<Agriculteur> agriculteurs = agriculteurService.afficherTous();

        System.out.println("\n═══ AGRICULTEURS EN ATTENTE ═══");
        for (Agriculteur a : agriculteurs) {
            if (!a.isCompteVerifie()) {
                System.out.println("\nID: " + a.getAgriculteurId());
                System.out.println("Nom: " + a.getPrenom() + " " + a.getNom());
                System.out.println("Email: " + a.getEmail());
                System.out.println("Ferme: " + a.getAdresseFerme());
                System.out.println("Superficie: " + a.getSuperficieFerme() + " ha");
                System.out.println("Culture: " + a.getTypeCulture());

                System.out.print("Vérifier ce compte? (o/n/s pour sauter): ");
                String reponse = scanner.nextLine();

                if (reponse.equalsIgnoreCase("o")) {
                    agriculteurService.verifierCompte(a.getAgriculteurId(), true);
                } else if (reponse.equalsIgnoreCase("n")) {
                    agriculteurService.verifierCompte(a.getAgriculteurId(), false);
                }
            }
        }
    }

    /**
     * VÉRIFIER COMPTES BANQUES
     */
    private static void verifierComptesBanques() throws SQLException {
        List<Banque> banques = banqueService.afficherTous();

        System.out.println("\n═══ BANQUES EN ATTENTE ═══");
        for (Banque b : banques) {
            if (!b.isCompteVerifie()) {
                System.out.println("\nID: " + b.getBanqueId());
                System.out.println("Banque: " + b.getNom());
                System.out.println("Code: " + b.getCodeBanque());
                System.out.println("Représentant: " + b.getRepresentantLegal());
                System.out.println("Siège: " + b.getAddresseSiege());

                System.out.print("Vérifier ce compte? (o/n/s): ");
                String reponse = scanner.nextLine();

                if (reponse.equalsIgnoreCase("o")) {
                    banqueService.verifierCompte(b.getBanqueId(), true);
                } else if (reponse.equalsIgnoreCase("n")) {
                    banqueService.verifierCompte(b.getBanqueId(), false);
                }
            }
        }
    }

    /**
     * VALIDER DOCUMENTS
     */
    private static void validerDocuments() throws SQLException {
        List<Document> documents = documentService.afficherEnAttente();

        System.out.println("\n═══ DOCUMENTS EN ATTENTE ═══");
        System.out.println(documents.size() + " document(s) en attente");

        for (Document doc : documents) {
            System.out.println("\nID: " + doc.getId());
            System.out.println("Utilisateur ID: " + doc.getUtilisateurId());
            System.out.println("Nom: " + doc.getNom());
            System.out.println("Type: " + doc.getTypeDocument());
            System.out.println("Date: " + doc.getDateUpload());

            System.out.print("Action (v=valider, r=rejeter, s=sauter): ");
            String action = scanner.nextLine();

            if (action.equalsIgnoreCase("v")) {
                documentService.validerDocument(doc.getId(), "valide");
            } else if (action.equalsIgnoreCase("r")) {
                documentService.validerDocument(doc.getId(), "rejete");
            }
        }
    }

    /**
     * AFFICHER STATISTIQUES
     */
    private static void afficherStatistiques() throws SQLException {
        System.out.println("\n═══════════ STATISTIQUES ═══════════");

        List<Utilisateur> users = utilisateurService.afficherTous();
        List<Agriculteur> agriculteurs = agriculteurService.afficherTous();
        List<Banque> banques = banqueService.afficherTous();
        List<Admin> admins = adminService.afficherTous();

        System.out.println("Total utilisateurs: " + users.size());
        System.out.println("  - Agriculteurs: " + agriculteurs.size());
        System.out.println("  - Banques: " + banques.size());
        System.out.println("  - Admins: " + admins.size());

        long agricultureursVerifies = agriculteurs.stream()
                .filter(Agriculteur::isCompteVerifie)
                .count();
        System.out.println("\nAgriculteurs vérifiés: " + agricultureursVerifies + "/" + agriculteurs.size());

        long banquesVerifiees = banques.stream()
                .filter(Banque::isCompteVerifie)
                .count();
        System.out.println("Banques vérifiées: " + banquesVerifiees + "/" + banques.size());
    }

    /**
     * MODIFIER PROFIL ADMIN
     */
    private static void modifierProfilAdmin() throws SQLException {
        System.out.println("\n═══ MON PROFIL ADMIN ═══");
        System.out.println("Nom: " + utilisateurConnecte.getNom());
        System.out.println("Prénom: " + utilisateurConnecte.getPrenom());
        System.out.println("Email: " + utilisateurConnecte.getEmail());
        System.out.println("Téléphone: " + utilisateurConnecte.getTel());

        if (utilisateurConnecte.getPhoto() != null && !utilisateurConnecte.getPhoto().isEmpty()) {
            System.out.println("Photo: " + utilisateurConnecte.getPhoto());
        }

        System.out.println("\n1. Modifier mes informations");
        System.out.println("2. Changer mon mot de passe");
        System.out.println("3. Modifier ma photo de profil");
        System.out.println("0. Retour");
        System.out.print("Votre choix: ");

        int choix = lireEntier();

        if (choix == 1) {
            System.out.print("Nouveau nom (Entrée pour garder '" + utilisateurConnecte.getNom() + "'): ");
            String nom = scanner.nextLine();
            if (!nom.trim().isEmpty()) {
                utilisateurConnecte.setNom(nom);
            }

            System.out.print("Nouveau prénom (Entrée pour garder '" + utilisateurConnecte.getPrenom() + "'): ");
            String prenom = scanner.nextLine();
            if (!prenom.trim().isEmpty()) {
                utilisateurConnecte.setPrenom(prenom);
            }

            System.out.print("Nouveau téléphone (Entrée pour garder '" + utilisateurConnecte.getTel() + "'): ");
            String tel = scanner.nextLine();
            if (!tel.trim().isEmpty()) {
                utilisateurConnecte.setTel(tel);
            }

            utilisateurService.modifier(utilisateurConnecte);
            System.out.println("✓ Profil modifié avec succès!");

        } else if (choix == 2) {
            System.out.print("Mot de passe actuel: ");
            String oldPwd = scanner.nextLine();

            if (utilisateurService.verifierPassword(oldPwd, utilisateurConnecte.getPassword())) {
                System.out.print("Nouveau mot de passe: ");
                String newPwd = scanner.nextLine();

                System.out.print("Confirmer le nouveau mot de passe: ");
                String confirmPwd = scanner.nextLine();

                if (newPwd.equals(confirmPwd)) {
                    utilisateurService.modifierMotDePasse(utilisateurConnecte.getId(), newPwd);
                } else {
                    System.out.println("✗ Les mots de passe ne correspondent pas!");
                }
            } else {
                System.out.println("✗ Mot de passe actuel incorrect!");
            }

        } else if (choix == 3) {
            changerPhotoProfil();
        }
    }

    /**
     * MENU AGRICULTEUR
     */
    private static void menuAgriculteur() throws SQLException {
        System.out.println("\n═══════════════ MENU AGRICULTEUR ═══════════════");
        System.out.println("Bonjour " + utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom());

        // Afficher messages non lus
        int nbNonLus = messagerieService.compterMessagesNonLus(utilisateurConnecte.getId());
        if (nbNonLus > 0) {
            System.out.println("📬 Vous avez " + nbNonLus + " message(s) non lu(s)");
        }

        System.out.println("1. Voir mon profil");
        System.out.println("2. Modifier mon profil");
        System.out.println("3. Modifier ma photo de profil");
        System.out.println("4. Gérer mes documents");
        System.out.println("5. Ajouter un document");
        System.out.println("6. 💬 Messagerie");
        System.out.println("7. 🔐 Sécurité (2FA)");
        System.out.println("0. Se déconnecter");
        System.out.println("════════════════════════════════════════════════");
        System.out.print("Votre choix: ");

        int choix = lireEntier();

        switch (choix) {
            case 1:
                afficherProfilAgriculteur();
                break;
            case 2:
                modifierProfilAgriculteur();
                break;
            case 3:
                changerPhotoProfil();
                break;
            case 4:
                gererMesDocuments();
                break;
            case 5:
                ajouterDocument();
                break;
            case 6:
                menuMessagerie();
                break;
            case 7:
                menuSecurite();
                break;
            case 0:
                deconnexion();
                break;
        }
    }

    /**
     * AFFICHER PROFIL AGRICULTEUR
     */
    private static void afficherProfilAgriculteur() throws SQLException {
        Agriculteur agri = agriculteurService.rechercherParUtilisateurId(utilisateurConnecte.getId());

        if (agri != null) {
            System.out.println("\n╔════════════════════════════════════════════════════╗");
            System.out.println("║              MON PROFIL AGRICULTEUR                ║");
            System.out.println("╚════════════════════════════════════════════════════╝");

            // Photo de profil
            System.out.println();
            if (agri.getPhoto() != null && !agri.getPhoto().isEmpty()) {
                if (com.agrifund.util.FileManager.fichierExiste(agri.getPhoto())) {
                    long taille = com.agrifund.util.FileManager.getTailleFichier(agri.getPhoto());
                    System.out.println("📷 Photo de profil : " + agri.getPhoto());
                    System.out.println("                    (" + com.agrifund.util.FileManager.formaterTaille(taille) + ")");
                } else {
                    System.out.println("📷 Photo de profil : " + agri.getPhoto() + " ⚠️ (fichier manquant)");
                }
            } else {
                System.out.println("📷 Photo de profil : Aucune photo");
            }

            System.out.println();
            System.out.println("👤 Nom              : " + agri.getPrenom() + " " + agri.getNom());
            System.out.println("📧 Email            : " + agri.getEmail());
            System.out.println("📱 Téléphone        : " + (agri.getTel() != null ? agri.getTel() : "Non renseigné"));
            System.out.println();
            System.out.println("🌾 INFORMATIONS FERME");
            System.out.println("───────────────────────────────────────────────────");
            System.out.println("📍 Adresse          : " + agri.getAdresseFerme());
            System.out.println("📏 Superficie       : " + agri.getSuperficieFerme() + " hectares");
            System.out.println("🌱 Type de culture  : " + agri.getTypeCulture());
            System.out.println();
            System.out.println("📋 STATUT DU COMPTE");
            System.out.println("───────────────────────────────────────────────────");
            System.out.println("🔒 Statut           : " + agri.getStatusCompte());
            System.out.println("✓  Compte vérifié   : " + (agri.isCompteVerifie() ? "Oui ✓" : "Non ✗"));

            if (agri.getDateInscrit() != null) {
                System.out.println("📅 Inscrit le       : " + agri.getDateInscrit().toString().substring(0, 10));
            }
            System.out.println();
        }
    }

    /**
     * MODIFIER PROFIL AGRICULTEUR
     */
    private static void modifierProfilAgriculteur() throws SQLException {
        Agriculteur agri = agriculteurService.rechercherParUtilisateurId(utilisateurConnecte.getId());

        if (agri == null) {
            System.out.println("✗ Profil agriculteur non trouvé!");
            return;
        }

        System.out.println("\n═══ MODIFIER MON PROFIL ═══");
        System.out.println("Laissez vide pour conserver la valeur actuelle\n");

        // Modification des informations utilisateur
        System.out.print("Nom (" + agri.getNom() + "): ");
        String nom = scanner.nextLine();
        if (!nom.trim().isEmpty()) {
            agri.setNom(nom);
            utilisateurConnecte.setNom(nom);
        }

        System.out.print("Prénom (" + agri.getPrenom() + "): ");
        String prenom = scanner.nextLine();
        if (!prenom.trim().isEmpty()) {
            agri.setPrenom(prenom);
            utilisateurConnecte.setPrenom(prenom);
        }

        System.out.print("Téléphone (" + agri.getTel() + "): ");
        String tel = scanner.nextLine();
        if (!tel.trim().isEmpty()) {
            agri.setTel(tel);
            utilisateurConnecte.setTel(tel);
        }

        // Modification des informations spécifiques agriculteur
        System.out.print("Adresse ferme (" + agri.getAdresseFerme() + "): ");
        String adresseFerme = scanner.nextLine();
        if (!adresseFerme.trim().isEmpty()) {
            agri.setAdresseFerme(adresseFerme);
        }

        System.out.print("Superficie en hectares (" + agri.getSuperficieFerme() + "): ");
        String superficieStr = scanner.nextLine();
        if (!superficieStr.trim().isEmpty()) {
            try {
                BigDecimal superficie = new BigDecimal(superficieStr);
                agri.setSuperficieFerme(superficie);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Superficie invalide, valeur non modifiée");
            }
        }

        System.out.print("Type de culture (" + agri.getTypeCulture() + "): ");
        String typeCulture = scanner.nextLine();
        if (!typeCulture.trim().isEmpty()) {
            agri.setTypeCulture(typeCulture);
        }

        // Changement de mot de passe (optionnel)
        System.out.print("Changer le mot de passe? (o/n): ");
        String changePwd = scanner.nextLine();
        if (changePwd.equalsIgnoreCase("o")) {
            System.out.print("Mot de passe actuel: ");
            String oldPwd = scanner.nextLine();

            if (utilisateurService.verifierPassword(oldPwd, utilisateurConnecte.getPassword())) {
                System.out.print("Nouveau mot de passe: ");
                String newPwd = scanner.nextLine();

                System.out.print("Confirmer le nouveau mot de passe: ");
                String confirmPwd = scanner.nextLine();

                if (newPwd.equals(confirmPwd)) {
                    utilisateurService.modifierMotDePasse(utilisateurConnecte.getId(), newPwd);
                } else {
                    System.out.println("✗ Les mots de passe ne correspondent pas!");
                }
            } else {
                System.out.println("✗ Mot de passe actuel incorrect!");
            }
        }

        // Sauvegarder les modifications
        agriculteurService.modifier(agri);
        System.out.println("✓ Profil modifié avec succès!");
    }

    /**
     * GÉRER MES DOCUMENTS
     */
    private static void gererMesDocuments() throws SQLException {
        List<Document> docs = documentService.afficherParUtilisateur(utilisateurConnecte.getId());

        System.out.println("\n═══ MES DOCUMENTS ═══");
        System.out.println(docs.size() + " document(s)");

        for (Document doc : docs) {
            System.out.println("\n- " + doc.getNom());
            System.out.println("  Type: " + doc.getTypeDocument());
            System.out.println("  Statut: " + doc.getStatut());
            System.out.println("  Date: " + doc.getDateUpload());
        }
    }

    /**
     * AJOUTER UN DOCUMENT
     */
    private static void ajouterDocument() throws SQLException {
        System.out.println("\n═══ AJOUTER UN DOCUMENT ═══");

        System.out.print("Nom du document: ");
        String nom = scanner.nextLine();

        System.out.println("\nType de document:");
        System.out.println("1. CNI (Carte Nationale d'Identité)");
        System.out.println("2. Certificat Biologique");
        System.out.println("3. Cadastre");
        System.out.println("4. Passeport");
        System.out.println("5. Agrément");
        System.out.println("6. KBIS");
        System.out.println("7. Autre");
        System.out.print("Choix: ");
        int choixType = lireEntier();

        String type = "autre";
        switch (choixType) {
            case 1: type = "cni"; break;
            case 2: type = "certificat_bio"; break;
            case 3: type = "cadastre"; break;
            case 4: type = "passeport"; break;
            case 5: type = "agrement"; break;
            case 6: type = "kbis"; break;
            case 7: type = "autre"; break;
        }

        System.out.print("\nChemin complet du fichier (ex: C:/Documents/cni.pdf): ");
        String cheminFichier = scanner.nextLine();

        // Upload du fichier
        String cheminStocke = com.agrifund.util.FileManager.uploadDocument(cheminFichier,
                utilisateurConnecte.getId(),
                type);

        if (cheminStocke != null) {
            // Enregistrer en base de données
            Document doc = new Document(utilisateurConnecte.getId(), nom, type, cheminStocke);
            doc.setTaille((int) com.agrifund.util.FileManager.getTailleFichier(cheminStocke));

            documentService.ajouter(doc);

            System.out.println("\n✓ Document uploadé et enregistré avec succès!");
            System.out.println("  Fichier: " + cheminStocke);
            System.out.println("  Taille: " + com.agrifund.util.FileManager.formaterTaille(doc.getTaille()));
            System.out.println("  Statut: en attente de validation");
        } else {
            System.out.println("✗ Échec de l'upload du document");
        }
    }

    /**
     * CHANGER LA PHOTO DE PROFIL
     */
    private static void changerPhotoProfil() throws SQLException {
        System.out.println("\n═══ MODIFIER MA PHOTO DE PROFIL ═══");

        if (utilisateurConnecte.getPhoto() != null && !utilisateurConnecte.getPhoto().isEmpty()) {
            System.out.println("Photo actuelle: " + utilisateurConnecte.getPhoto());

            if (com.agrifund.util.FileManager.fichierExiste(utilisateurConnecte.getPhoto())) {
                long taille = com.agrifund.util.FileManager.getTailleFichier(utilisateurConnecte.getPhoto());
                System.out.println("Taille: " + com.agrifund.util.FileManager.formaterTaille(taille));
            }
        } else {
            System.out.println("Aucune photo de profil actuellement");
        }

        System.out.println("\nFormats acceptés: JPG, JPEG, PNG, GIF");
        System.out.println("Taille maximale: 5 Mo");
        System.out.println();
        System.out.print("Chemin de la nouvelle photo (ex: C:/Images/photo.jpg): ");
        String cheminPhoto = scanner.nextLine();

        if (cheminPhoto.trim().isEmpty()) {
            System.out.println("Annulé");
            return;
        }

        // Upload de la photo
        String cheminStocke = com.agrifund.util.FileManager.uploadPhoto(cheminPhoto, utilisateurConnecte.getId());

        if (cheminStocke != null) {
            // Supprimer l'ancienne photo si elle existe
            if (utilisateurConnecte.getPhoto() != null && !utilisateurConnecte.getPhoto().isEmpty()) {
                com.agrifund.util.FileManager.supprimerFichier(utilisateurConnecte.getPhoto());
            }

            // Mettre à jour en base de données
            utilisateurConnecte.setPhoto(cheminStocke);
            utilisateurService.modifier(utilisateurConnecte);

            System.out.println("\n✓ Photo de profil mise à jour avec succès!");
            System.out.println("  Fichier: " + cheminStocke);
            System.out.println("  Taille: " + com.agrifund.util.FileManager.formaterTaille(
                    com.agrifund.util.FileManager.getTailleFichier(cheminStocke)));
        } else {
            System.out.println("✗ Échec de l'upload de la photo");
        }
    }

    /**
     * MENU BANQUE
     */
    private static void menuBanque() throws SQLException {
        System.out.println("\n═══════════════ MENU BANQUE ═══════════════");
        System.out.println("Bonjour " + utilisateurConnecte.getNom());

        // Afficher messages non lus
        int nbNonLus = messagerieService.compterMessagesNonLus(utilisateurConnecte.getId());
        if (nbNonLus > 0) {
            System.out.println("📬 Vous avez " + nbNonLus + " message(s) non lu(s)");
        }

        System.out.println("1. Voir mon profil");
        System.out.println("2. Modifier mon profil");
        System.out.println("3. Modifier ma photo de profil");
        System.out.println("4. Gérer mes documents");
        System.out.println("5. Ajouter un document");
        System.out.println("6. 💬 Messagerie");
        System.out.println("7. 🔐 Sécurité (2FA)");
        System.out.println("0. Se déconnecter");
        System.out.println("═══════════════════════════════════════════");
        System.out.print("Votre choix: ");

        int choix = lireEntier();

        switch (choix) {
            case 1:
                afficherProfilBanque();
                break;
            case 2:
                modifierProfilBanque();
                break;
            case 3:
                changerPhotoProfil();
                break;
            case 4:
                gererMesDocuments();
                break;
            case 5:
                ajouterDocument();
                break;
            case 6:
                menuMessagerie();
                break;
            case 7:
                menuSecurite();
                break;
            case 0:
                deconnexion();
                break;
        }
    }

    /**
     * AFFICHER PROFIL BANQUE
     */
    private static void afficherProfilBanque() throws SQLException {
        Banque banque = banqueService.rechercherParUtilisateurId(utilisateurConnecte.getId());

        if (banque != null) {
            System.out.println("\n╔════════════════════════════════════════════════════╗");
            System.out.println("║                 PROFIL BANQUE                      ║");
            System.out.println("╚════════════════════════════════════════════════════╝");

            // Photo/Logo
            System.out.println();
            if (banque.getPhoto() != null && !banque.getPhoto().isEmpty()) {
                if (com.agrifund.util.FileManager.fichierExiste(banque.getPhoto())) {
                    long taille = com.agrifund.util.FileManager.getTailleFichier(banque.getPhoto());
                    System.out.println("📷 Logo/Photo      : " + banque.getPhoto());
                    System.out.println("                    (" + com.agrifund.util.FileManager.formaterTaille(taille) + ")");
                } else {
                    System.out.println("📷 Logo/Photo      : " + banque.getPhoto() + " ⚠️ (fichier manquant)");
                }
            } else {
                System.out.println("📷 Logo/Photo      : Aucune photo");
            }

            System.out.println();
            System.out.println("🏦 INFORMATIONS GÉNÉRALES");
            System.out.println("───────────────────────────────────────────────────");
            System.out.println("🏢 Nom banque      : " + banque.getNom());
            System.out.println("🔢 Code banque     : " + banque.getCodeBanque());
            System.out.println("📧 Email           : " + banque.getEmail());
            System.out.println("📱 Téléphone       : " + (banque.getTel() != null ? banque.getTel() : "Non renseigné"));
            System.out.println();
            System.out.println("👔 REPRÉSENTATION");
            System.out.println("───────────────────────────────────────────────────");
            System.out.println("👤 Représentant    : " + banque.getRepresentantLegal());
            System.out.println();
            System.out.println("📍 ADRESSES");
            System.out.println("───────────────────────────────────────────────────");
            System.out.println("🏛️  Siège           : " + banque.getAddresseSiege());
            System.out.println("🏪 Agence          : " + (banque.getAdresseAgence() != null ? banque.getAdresseAgence() : "Non renseignée"));
            System.out.println("🌐 Site web        : " + (banque.getSiteWeb() != null ? banque.getSiteWeb() : "Non renseigné"));
            System.out.println();
            System.out.println("📋 STATUT DU COMPTE");
            System.out.println("───────────────────────────────────────────────────");
            System.out.println("🔒 Statut          : " + banque.getStatusCompte());
            System.out.println("✓  Compte vérifié  : " + (banque.isCompteVerifie() ? "Oui ✓" : "Non ✗"));

            if (banque.getDateInscrit() != null) {
                System.out.println("📅 Inscrit le      : " + banque.getDateInscrit().toString().substring(0, 10));
            }
            System.out.println();
        }
    }

    /**
     * MODIFIER PROFIL BANQUE
     */
    private static void modifierProfilBanque() throws SQLException {
        Banque banque = banqueService.rechercherParUtilisateurId(utilisateurConnecte.getId());

        if (banque == null) {
            System.out.println("✗ Profil banque non trouvé!");
            return;
        }

        System.out.println("\n═══ MODIFIER MON PROFIL BANQUE ═══");
        System.out.println("Laissez vide pour conserver la valeur actuelle\n");

        // Modification des informations utilisateur
        System.out.print("Nom de la banque (" + banque.getNom() + "): ");
        String nom = scanner.nextLine();
        if (!nom.trim().isEmpty()) {
            banque.setNom(nom);
            utilisateurConnecte.setNom(nom);
        }

        System.out.print("Téléphone (" + banque.getTel() + "): ");
        String tel = scanner.nextLine();
        if (!tel.trim().isEmpty()) {
            banque.setTel(tel);
            utilisateurConnecte.setTel(tel);
        }

        // Modification des informations spécifiques banque
        System.out.print("Représentant légal (" + banque.getRepresentantLegal() + "): ");
        String representant = scanner.nextLine();
        if (!representant.trim().isEmpty()) {
            banque.setRepresentantLegal(representant);
        }

        System.out.print("Adresse du siège (" + banque.getAddresseSiege() + "): ");
        String siege = scanner.nextLine();
        if (!siege.trim().isEmpty()) {
            banque.setAddresseSiege(siege);
        }

        System.out.print("Adresse agence (" + banque.getAdresseAgence() + "): ");
        String agence = scanner.nextLine();
        if (!agence.trim().isEmpty()) {
            banque.setAdresseAgence(agence);
        }

        System.out.print("Site web (" + banque.getSiteWeb() + "): ");
        String siteWeb = scanner.nextLine();
        if (!siteWeb.trim().isEmpty()) {
            banque.setSiteWeb(siteWeb);
        }

        System.out.print("Logo (chemin) (" + banque.getLogo() + "): ");
        String logo = scanner.nextLine();
        if (!logo.trim().isEmpty()) {
            banque.setLogo(logo);
        }

        // Changement de mot de passe (optionnel)
        System.out.print("Changer le mot de passe? (o/n): ");
        String changePwd = scanner.nextLine();
        if (changePwd.equalsIgnoreCase("o")) {
            System.out.print("Mot de passe actuel: ");
            String oldPwd = scanner.nextLine();

            if (utilisateurService.verifierPassword(oldPwd, utilisateurConnecte.getPassword())) {
                System.out.print("Nouveau mot de passe: ");
                String newPwd = scanner.nextLine();

                System.out.print("Confirmer le nouveau mot de passe: ");
                String confirmPwd = scanner.nextLine();

                if (newPwd.equals(confirmPwd)) {
                    utilisateurService.modifierMotDePasse(utilisateurConnecte.getId(), newPwd);
                } else {
                    System.out.println("✗ Les mots de passe ne correspondent pas!");
                }
            } else {
                System.out.println("✗ Mot de passe actuel incorrect!");
            }
        }

        // Sauvegarder les modifications
        banqueService.modifier(banque);
    }

    /**
     * DÉCONNEXION
     */
    private static void deconnexion() {
        authService.logout();
        utilisateurConnecte = null;
        typeUtilisateur = null;
    }

    /**
     * Utilitaire pour lire un entier
     */
    private static int lireEntier() {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.print("⚠ Veuillez entrer un nombre valide: ");
            }
        }
    }

    // ============================================
    // MESSAGERIE
    // ============================================

    /**
     * MENU MESSAGERIE
     */
    private static void menuMessagerie() throws SQLException {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║                     💬 MESSAGERIE                      ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");

            int nbNonLus = messagerieService.compterMessagesNonLus(utilisateurConnecte.getId());
            if (nbNonLus > 0) {
                System.out.println("\n📬 Vous avez " + nbNonLus + " message(s) non lu(s)");
            }

            System.out.println("\n1. Mes conversations");
            System.out.println("2. Nouvelle conversation");
            System.out.println("3. Liste des emojis");
            System.out.println("0. Retour");
            System.out.print("\nVotre choix: ");

            int choix = lireEntier();

            if (choix == 1) {
                afficherConversations();
            } else if (choix == 2) {
                nouvelleConversation();
            } else if (choix == 3) {
                com.agrifund.util.EmojiManager.afficherListeEmojis();
                System.out.print("\nAppuyez sur Entrée pour continuer...");
                scanner.nextLine();
            } else if (choix == 0) {
                break;
            }
        }
    }

    /**
     * AFFICHER LES CONVERSATIONS
     */
    private static void afficherConversations() throws SQLException {
        List<com.agrifund.entities.Conversation> conversations = messagerieService.listerConversations(utilisateurConnecte.getId());

        if (conversations.isEmpty()) {
            System.out.println("\n📭 Aucune conversation. Créez-en une nouvelle!");
            System.out.print("\nAppuyez sur Entrée pour continuer...");
            scanner.nextLine();
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                  MES CONVERSATIONS                     ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();

        for (int i = 0; i < conversations.size(); i++) {
            com.agrifund.entities.Conversation conv = conversations.get(i);

            System.out.println("┌─────────────────────────────────────────────────────┐");
            System.out.println("│ " + (i + 1) + ". 👤 " + conv.getNomCorrespondant());
            System.out.println("├─────────────────────────────────────────────────────┤");

            if (conv.getDernierMessage() != null && !conv.getDernierMessage().isEmpty()) {
                System.out.println("│ 💬 " + conv.getDernierMessage());
            } else {
                System.out.println("│ 💬 Aucun message");
            }

            if (conv.getNbMessagesNonLus() > 0) {
                System.out.println("│ 📬 " + conv.getNbMessagesNonLus() + " nouveau(x) message(s)");
            }

            System.out.println("└─────────────────────────────────────────────────────┘");
            System.out.println();
        }

        System.out.print("Entrez le numéro de la conversation à ouvrir (0 pour annuler): ");
        int choix = lireEntier();

        if (choix > 0 && choix <= conversations.size()) {
            ouvrirConversation(conversations.get(choix - 1));
        }
    }

    /**
     * NOUVELLE CONVERSATION
     */
    private static void nouvelleConversation() throws SQLException {
        System.out.println("\n═══ NOUVELLE CONVERSATION ═══");

        // Lister les utilisateurs disponibles
        List<Utilisateur> users = utilisateurService.afficherTous();
        List<Utilisateur> autresUsers = new ArrayList<>();

        for (Utilisateur u : users) {
            if (u.getId() != utilisateurConnecte.getId()) {
                autresUsers.add(u);
            }
        }

        if (autresUsers.isEmpty()) {
            System.out.println("✗ Aucun autre utilisateur disponible");
            return;
        }

        System.out.println("\nUtilisateurs disponibles:");
        for (int i = 0; i < autresUsers.size(); i++) {
            Utilisateur u = autresUsers.get(i);
            String type = "";
            try {
                type = authService.getTypeUtilisateur(u.getId());
                switch (type) {
                    case "ADMIN": type = "👨‍💼 Admin"; break;
                    case "AGRICULTEUR": type = "🌾 Agriculteur"; break;
                    case "BANQUE": type = "🏦 Banque"; break;
                }
            } catch (Exception e) {
                type = "Utilisateur";
            }

            System.out.println((i + 1) + ". " + type + " - " + u.getPrenom() + " " + u.getNom() + " (" + u.getEmail() + ")");
        }

        System.out.print("\nChoisir un utilisateur (0 pour annuler): ");
        int choix = lireEntier();

        if (choix > 0 && choix <= autresUsers.size()) {
            Utilisateur destinataire = autresUsers.get(choix - 1);
            com.agrifund.entities.Conversation conv = messagerieService.creerOuRecupererConversation(
                    utilisateurConnecte.getId(),
                    destinataire.getId()
            );

            ouvrirConversation(conv);
        }
    }

    /**
     * OUVRIR UNE CONVERSATION
     */
    private static void ouvrirConversation(com.agrifund.entities.Conversation conv) throws SQLException {
        while (true) {
            // Récupérer le nom du correspondant
            int correspondantId = conv.getCorrespondantId(utilisateurConnecte.getId());
            Utilisateur correspondant = utilisateurService.rechercherParId(correspondantId);
            String nomCorrespondant = correspondant.getPrenom() + " " + correspondant.getNom();

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║      CONVERSATION AVEC " + nomCorrespondant.toUpperCase());
            System.out.println("╚════════════════════════════════════════════════════════╝");

            // Afficher les messages
            List<com.agrifund.entities.Message> messages = messagerieService.listerMessages(conv.getId(), utilisateurConnecte.getId());

            if (messages.isEmpty()) {
                System.out.println("\n📭 Aucun message. Envoyez le premier!\n");
            } else {
                System.out.println();
                for (com.agrifund.entities.Message msg : messages) {
                    afficherMessage(msg);
                }
            }

            System.out.println("───────────────────────────────────────────────────────");
            System.out.println("1. ✉️  Envoyer un message texte");
            System.out.println("2. 🖼️  Envoyer une image");
            System.out.println("3. 📄 Envoyer un document");
            System.out.println("4. 🎵 Envoyer un audio");
            System.out.println("5. ✏️  Modifier un de mes messages");
            System.out.println("6. 🗑️  Supprimer un de mes messages");
            System.out.println("7. 🔄 Actualiser");
            System.out.println("8. 😊 Voir les emojis");
            System.out.println("0. ⬅️  Retour");
            System.out.print("\nVotre choix: ");

            int choix = lireEntier();

            if (choix == 1) {
                envoyerMessage(conv.getId());
            } else if (choix == 2) {
                envoyerFichier(conv.getId(), "image");
            } else if (choix == 3) {
                envoyerFichier(conv.getId(), "document");
            } else if (choix == 4) {
                envoyerFichier(conv.getId(), "audio");
            } else if (choix == 5) {
                modifierMessage(messages);
            } else if (choix == 6) {
                supprimerMessage(messages);
            } else if (choix == 7) {
                // Actualiser (continue la boucle)
                continue;
            } else if (choix == 8) {
                com.agrifund.util.EmojiManager.afficherListeEmojis();
                System.out.print("\nAppuyez sur Entrée pour continuer...");
                scanner.nextLine();
            } else if (choix == 0) {
                break;
            }
        }
    }

    /**
     * AFFICHER UN MESSAGE
     */
    private static void afficherMessage(com.agrifund.entities.Message msg) throws SQLException {
        String dateStr = msg.getDateEnvoi().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        boolean estMoi = msg.getExpediteurId() == utilisateurConnecte.getId();

        if (estMoi) {
            System.out.println("📅 " + dateStr);
            System.out.println("👨‍💼 Vous");
            System.out.println("💬 " + msg.getContenu());
            if (msg.estModifie()) {
                System.out.println("   (modifié)");
            }
        } else {
            System.out.println("📅 " + dateStr);
            System.out.println("👤 " + msg.getNomExpediteur());
            System.out.println("💬 " + msg.getContenu());
        }

        // Afficher les pièces jointes si présentes
        if (msg.isaPieceJointe() && msg.getNbPiecesJointes() > 0) {
            try {
                List<com.agrifund.entities.PieceJointe> pieces = messagerieService.listerPiecesJointes(msg.getId());

                if (!pieces.isEmpty()) {
                    System.out.println("   📎 Pièce(s) jointe(s) (" + pieces.size() + "):");
                    for (com.agrifund.entities.PieceJointe pj : pieces) {
                        System.out.println("      " + pj.getIcone() + " " + pj.getNomOriginal() +
                                " (" + pj.getTailleFormatee() + ")");
                    }
                }
            } catch (Exception e) {
                System.out.println("   ⚠️ Erreur chargement pièces jointes");
            }
        }

        System.out.println();
    }

    /**
     * ENVOYER UN MESSAGE
     */
    private static void envoyerMessage(int conversationId) throws SQLException {
        System.out.println("\n═══ ENVOYER UN MESSAGE ═══");
        System.out.println("💡 Utilisez les codes emoji! Ex: :smile: :heart: :farm:");
        System.out.print("\nVotre message: ");
        String contenu = scanner.nextLine();

        if (contenu.trim().isEmpty()) {
            System.out.println("✗ Message vide, annulé");
            return;
        }

        // Convertir les emojis
        String contenuAvecEmojis = com.agrifund.util.EmojiManager.convertirEmojis(contenu);

        com.agrifund.entities.Message msg = messagerieService.envoyerMessage(
                conversationId,
                utilisateurConnecte.getId(),
                contenuAvecEmojis
        );

        if (msg != null) {
            System.out.println("✓ Message envoyé!");
        }
    }

    /**
     * MODIFIER UN MESSAGE
     */
    private static void modifierMessage(List<com.agrifund.entities.Message> messages) throws SQLException {
        // Filtrer seulement mes messages
        List<com.agrifund.entities.Message> mesMessages = new ArrayList<>();
        for (com.agrifund.entities.Message msg : messages) {
            if (msg.getExpediteurId() == utilisateurConnecte.getId()) {
                mesMessages.add(msg);
            }
        }

        if (mesMessages.isEmpty()) {
            System.out.println("\n✗ Vous n'avez aucun message à modifier");
            System.out.print("\nAppuyez sur Entrée pour continuer...");
            scanner.nextLine();
            return;
        }

        System.out.println("\n═══ MODIFIER UN MESSAGE ═══");
        System.out.println("Vos messages:");

        for (int i = 0; i < mesMessages.size(); i++) {
            com.agrifund.entities.Message msg = mesMessages.get(i);
            System.out.println((i + 1) + ". " + msg.getApercu());
        }

        System.out.print("\nNuméro du message à modifier (0 pour annuler): ");
        int choix = lireEntier();

        if (choix > 0 && choix <= mesMessages.size()) {
            com.agrifund.entities.Message msg = mesMessages.get(choix - 1);

            System.out.println("Message actuel: " + msg.getContenu());
            System.out.print("Nouveau contenu: ");
            String nouveauContenu = scanner.nextLine();

            if (!nouveauContenu.trim().isEmpty()) {
                String nouveauAvecEmojis = com.agrifund.util.EmojiManager.convertirEmojis(nouveauContenu);
                messagerieService.modifierMessage(msg.getId(), utilisateurConnecte.getId(), nouveauAvecEmojis);
            }
        }
    }

    /**
     * SUPPRIMER UN MESSAGE
     */
    private static void supprimerMessage(List<com.agrifund.entities.Message> messages) throws SQLException {
        // Filtrer seulement mes messages
        List<com.agrifund.entities.Message> mesMessages = new ArrayList<>();
        for (com.agrifund.entities.Message msg : messages) {
            if (msg.getExpediteurId() == utilisateurConnecte.getId()) {
                mesMessages.add(msg);
            }
        }

        if (mesMessages.isEmpty()) {
            System.out.println("\n✗ Vous n'avez aucun message à supprimer");
            System.out.print("\nAppuyez sur Entrée pour continuer...");
            scanner.nextLine();
            return;
        }

        System.out.println("\n═══ SUPPRIMER UN MESSAGE ═══");
        System.out.println("Vos messages:");

        for (int i = 0; i < mesMessages.size(); i++) {
            com.agrifund.entities.Message msg = mesMessages.get(i);
            System.out.println((i + 1) + ". " + msg.getApercu());
        }

        System.out.print("\nNuméro du message à supprimer (0 pour annuler): ");
        int choix = lireEntier();

        if (choix > 0 && choix <= mesMessages.size()) {
            com.agrifund.entities.Message msg = mesMessages.get(choix - 1);

            System.out.print("⚠️ Confirmer la suppression? (o/n): ");
            String confirm = scanner.nextLine();

            if (confirm.equalsIgnoreCase("o")) {
                messagerieService.supprimerMessage(msg.getId(), utilisateurConnecte.getId());
            }
        }
    }

    // ============================================
    // SÉCURITÉ & 2FA
    // ============================================

    /**
     * MENU SÉCURITÉ
     */
    private static void menuSecurite() throws SQLException {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║              🔐 PARAMÈTRES DE SÉCURITÉ                 ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");

            // Obtenir les paramètres actuels
            com.agrifund.entities.Parametres2FA params = twoFAService.getParametres(utilisateurConnecte.getId());

            if (params != null && params.isEstActive()) {
                System.out.println("\n✅ Authentification à deux facteurs : ACTIVÉE");
                System.out.println("📧 Méthode : EMAIL (" + utilisateurConnecte.getEmail() + ")");
            } else {
                System.out.println("\n❌ Authentification à deux facteurs : DÉSACTIVÉE");
                System.out.println("⚠️  Votre compte n'est protégé que par un mot de passe");
            }

            System.out.println("\n1. Activer/Désactiver la 2FA");
            System.out.println("2. Voir l'historique des connexions");
            System.out.println("3. Tester l'envoi d'un code 2FA");
            System.out.println("0. Retour");
            System.out.print("\nVotre choix: ");

            int choix = lireEntier();

            if (choix == 1) {
                activer2FA();
            } else if (choix == 2) {
                afficherHistoriqueConnexions();
            } else if (choix == 3) {
                testerCode2FA();
            } else if (choix == 0) {
                break;
            }
        }
    }

    /**
     * ACTIVER/DÉSACTIVER LA 2FA
     */
    private static void activer2FA() throws SQLException {
        System.out.println("\n═══ CONFIGURATION 2FA ═══");

        com.agrifund.entities.Parametres2FA params = twoFAService.getParametres(utilisateurConnecte.getId());

        if (params == null) {
            params = new com.agrifund.entities.Parametres2FA(utilisateurConnecte.getId());
        }

        if (params.isEstActive()) {
            System.out.print("La 2FA est actuellement activée. Désactiver? (o/n): ");
            String choix = scanner.nextLine();

            if (choix.equalsIgnoreCase("o")) {
                params.setEstActive(false);
                twoFAService.sauvegarderParametres(params);
                System.out.println("❌ 2FA désactivée");
                System.out.println("⚠️  Votre compte est moins sécurisé");
            }
        } else {
            System.out.print("Activer l'authentification à deux facteurs? (o/n): ");
            String choix = scanner.nextLine();

            if (choix.equalsIgnoreCase("o")) {
                params.setEstActive(true);
                params.setMethodePreferee("email");

                twoFAService.sauvegarderParametres(params);

                System.out.println("\n✅ 2FA activée avec succès!");
                System.out.println("📧 Les codes seront envoyés à: " + utilisateurConnecte.getEmail());
                System.out.println("🔐 Votre compte est maintenant mieux protégé");
                System.out.println("💡 À chaque connexion, vous recevrez un code de vérification par email");
            }
        }
    }

    /**
     * CHANGER LA MÉTHODE 2FA
     */

    /**
     * AFFICHER L'HISTORIQUE DES CONNEXIONS
     */
    private static void afficherHistoriqueConnexions() throws SQLException {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║          📊 HISTORIQUE DES CONNEXIONS                  ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        String sql = "SELECT date_connexion, connexion_reussie, methode_auth " +
                "FROM HistoriqueConnexion " +
                "WHERE utilisateur_id = ? " +
                "ORDER BY date_connexion DESC " +
                "LIMIT 10";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = com.agrifund.util.DatabaseConnection.getConnection().prepareStatement(sql);
            pst.setInt(1, utilisateurConnecte.getId());

            rs = pst.executeQuery();

            System.out.println("\nDernières connexions :\n");

            boolean found = false;
            while (rs.next()) {
                found = true;
                java.sql.Timestamp dateConn = rs.getTimestamp("date_connexion");
                boolean reussie = rs.getBoolean("connexion_reussie");
                String methode = rs.getString("methode_auth");

                String dateStr = dateConn.toLocalDateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                String statut = reussie ? "✅ Réussie" : "❌ Échouée";
                String methodeStr = "";

                switch (methode) {
                    case "2fa": methodeStr = "🔐 2FA"; break;
                    case "password": methodeStr = "🔑 Password"; break;
                    case "token": methodeStr = "🎫 Token"; break;
                }

                System.out.println("📅 " + dateStr + "  |  " + statut + "  |  " + methodeStr);
            }

            if (!found) {
                System.out.println("Aucun historique disponible");
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        System.out.print("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    /**
     * TESTER L'ENVOI D'UN CODE 2FA
     */
    private static void testerCode2FA() throws SQLException {
        com.agrifund.entities.Parametres2FA params = twoFAService.getParametres(utilisateurConnecte.getId());

        if (params == null || !params.isEstActive()) {
            System.out.println("✗ La 2FA n'est pas activée");
            System.out.print("Activer maintenant? (o/n): ");
            String choix = scanner.nextLine();
            if (choix.equalsIgnoreCase("o")) {
                activer2FA();
            }
            return;
        }

        System.out.println("\n═══ TEST D'ENVOI CODE 2FA ═══");
        System.out.println("Un code de test va être envoyé à : " +
                ("email".equals(params.getMethodePreferee()) ?
                        utilisateurConnecte.getEmail() :
                        params.getTelephone2fa()));

        System.out.print("Continuer? (o/n): ");
        String choix = scanner.nextLine();

        if (choix.equalsIgnoreCase("o")) {
            com.agrifund.entities.Code2FA code = twoFAService.envoyerCode2FA(utilisateurConnecte.getId());

            if (code != null) {
                System.out.println("✓ Code envoyé avec succès!");
                System.out.println("⏰ Valide pendant 5 minutes");
            }
        }
    }

    /**
     * ENVOYER UN FICHIER (Image, Document, Audio)
     */
    private static void envoyerFichier(int conversationId, String typeFichier) throws SQLException {
        System.out.println("\n═══ ENVOYER UN FICHIER ═══");

        String typeLabel = "";
        String icone = "";
        switch (typeFichier) {
            case "image":
                typeLabel = "Image";
                icone = "🖼️";
                System.out.println("📋 Formats acceptés: JPG, PNG, GIF, BMP, WEBP");
                System.out.println("📏 Taille maximale: 10 Mo");
                break;
            case "document":
                typeLabel = "Document";
                icone = "📄";
                System.out.println("📋 Formats acceptés: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT");
                System.out.println("📏 Taille maximale: 25 Mo");
                break;
            case "audio":
                typeLabel = "Audio";
                icone = "🎵";
                System.out.println("📋 Formats acceptés: MP3, WAV, OGG, M4A, AAC");
                System.out.println("📏 Taille maximale: 15 Mo");
                break;
        }

        System.out.print("\nChemin du fichier: ");
        String cheminSource = scanner.nextLine();

        java.io.File source = new java.io.File(cheminSource);
        if (!source.exists()) {
            System.out.println("✗ Fichier introuvable: " + cheminSource);
            return;
        }

        // Vérifier la taille
        long taille = source.length();
        System.out.println("📦 Taille du fichier: " + com.agrifund.util.FileManager.formaterTaille(taille));

        System.out.print("\nMessage d'accompagnement (optionnel, Entrée pour passer): ");
        String message = scanner.nextLine();
        if (message.trim().isEmpty()) {
            message = icone + " [" + typeLabel + "]";
        }

        try {
            // 1. Envoyer le message
            com.agrifund.entities.Message msg = messagerieService.envoyerMessage(
                    conversationId,
                    utilisateurConnecte.getId(),
                    com.agrifund.util.EmojiManager.convertirEmojis(message)
            );

            if (msg == null) {
                System.out.println("✗ Erreur lors de l'envoi du message");
                return;
            }

            // 2. Uploader le fichier
            String cheminDestination = null;

            switch (typeFichier) {
                case "image":
                    cheminDestination = com.agrifund.util.MessagerieFileManager.uploadImage(cheminSource, msg.getId());
                    break;
                case "document":
                    cheminDestination = com.agrifund.util.MessagerieFileManager.uploadDocument(cheminSource, msg.getId());
                    break;
                case "audio":
                    cheminDestination = com.agrifund.util.MessagerieFileManager.uploadAudio(cheminSource, msg.getId());
                    break;
            }

            if (cheminDestination == null) {
                System.out.println("✗ Erreur lors de l'upload du fichier");
                return;
            }

            // 3. Enregistrer la pièce jointe
            com.agrifund.entities.PieceJointe pj = messagerieService.ajouterPieceJointe(
                    msg.getId(),
                    cheminDestination,
                    source.getName()
            );

            if (pj != null) {
                System.out.println("\n✅ Fichier envoyé avec succès!");
                System.out.println("   " + icone + " " + source.getName());
                System.out.println("   📦 Taille: " + com.agrifund.util.FileManager.formaterTaille(taille));
                System.out.println("   💾 Stocké: " + cheminDestination);
            } else {
                System.out.println("✗ Erreur lors de l'enregistrement de la pièce jointe");
            }

        } catch (java.io.IOException e) {
            System.out.println("✗ Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
    }
}