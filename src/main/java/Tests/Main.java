package Tests;
import entities.*;
import Services.*;
import Utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principale - Application de Gestion des Utilisateurs
 * Menu interactif pour tester toutes les fonctionnalités JDBC
 */
public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static AuthService authService;
    private static UtilisateurService utilisateurService;
    private static AgriculteurService agriculteurService;
    private static BanqueService banqueService;
    private static AdminService adminService;
    private static DocumentService documentService;

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
            Utils.EmailService.testerConfiguration();
            System.out.println();

            // Initialiser les répertoires de stockage de fichiers
            Utils.FileManager.initialiserRepertoires();
            System.out.println();

            // Initialiser les services
            authService = new AuthService();
            utilisateurService = new UtilisateurService();
            agriculteurService = new AgriculteurService();
            banqueService = new BanqueService();
            adminService = new AdminService();
            documentService = new DocumentService();

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
            typeUtilisateur = authService.getTypeUtilisateur(utilisateurConnecte.getId());
            System.out.println("Type de compte: " + typeUtilisateur);
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
        System.out.println("1. Gérer les utilisateurs");
        System.out.println("2. Vérifier les comptes agriculteurs");
        System.out.println("3. Vérifier les comptes banques");
        System.out.println("4. Valider les documents");
        System.out.println("5. Statistiques");
        System.out.println("6. Mon profil");
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
        System.out.println("2. Modifier un utilisateur");
        System.out.println("3. Supprimer un utilisateur");
        System.out.print("Votre choix: ");

        int choix = lireEntier();

        if (choix == 1) {
            afficherListeUtilisateurs();
        } else if (choix == 2) {
            System.out.print("ID de l'utilisateur à modifier: ");
            int id = lireEntier();
            modifierUtilisateurParAdmin(id);
        } else if (choix == 3) {
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
                if (Utils.FileManager.fichierExiste(u.getPhoto())) {
                    long taille = Utils.FileManager.getTailleFichier(u.getPhoto());
                    System.out.println("│ 📷 Photo     : " + u.getPhoto());
                    System.out.println("│              (" + Utils.FileManager.formaterTaille(taille) + ")");
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
        System.out.println("1. Voir mon profil");
        System.out.println("2. Modifier mon profil");
        System.out.println("3. Modifier ma photo de profil");
        System.out.println("4. Gérer mes documents");
        System.out.println("5. Ajouter un document");
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
                if (Utils.FileManager.fichierExiste(agri.getPhoto())) {
                    long taille = Utils.FileManager.getTailleFichier(agri.getPhoto());
                    System.out.println("📷 Photo de profil : " + agri.getPhoto());
                    System.out.println("                    (" + Utils.FileManager.formaterTaille(taille) + ")");
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
        String cheminStocke = Utils.FileManager.uploadDocument(cheminFichier,
                utilisateurConnecte.getId(),
                type);

        if (cheminStocke != null) {
            // Enregistrer en base de données
            Document doc = new Document(utilisateurConnecte.getId(), nom, type, cheminStocke);
            doc.setTaille((int) Utils.FileManager.getTailleFichier(cheminStocke));

            documentService.ajouter(doc);

            System.out.println("\n✓ Document uploadé et enregistré avec succès!");
            System.out.println("  Fichier: " + cheminStocke);
            System.out.println("  Taille: " + Utils.FileManager.formaterTaille(doc.getTaille()));
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

            if (Utils.FileManager.fichierExiste(utilisateurConnecte.getPhoto())) {
                long taille = Utils.FileManager.getTailleFichier(utilisateurConnecte.getPhoto());
                System.out.println("Taille: " + Utils.FileManager.formaterTaille(taille));
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
        String cheminStocke = Utils.FileManager.uploadPhoto(cheminPhoto, utilisateurConnecte.getId());

        if (cheminStocke != null) {
            // Supprimer l'ancienne photo si elle existe
            if (utilisateurConnecte.getPhoto() != null && !utilisateurConnecte.getPhoto().isEmpty()) {
                Utils.FileManager.supprimerFichier(utilisateurConnecte.getPhoto());
            }

            // Mettre à jour en base de données
            utilisateurConnecte.setPhoto(cheminStocke);
            utilisateurService.modifier(utilisateurConnecte);

            System.out.println("\n✓ Photo de profil mise à jour avec succès!");
            System.out.println("  Fichier: " + cheminStocke);
            System.out.println("  Taille: " + Utils.FileManager.formaterTaille(
                    Utils.FileManager.getTailleFichier(cheminStocke)));
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
        System.out.println("1. Voir mon profil");
        System.out.println("2. Modifier mon profil");
        System.out.println("3. Modifier ma photo de profil");
        System.out.println("4. Gérer mes documents");
        System.out.println("5. Ajouter un document");
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
                if (Utils.FileManager.fichierExiste(banque.getPhoto())) {
                    long taille = Utils.FileManager.getTailleFichier(banque.getPhoto());
                    System.out.println("📷 Logo/Photo      : " + banque.getPhoto());
                    System.out.println("                    (" + Utils.FileManager.formaterTaille(taille) + ")");
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
        try {
            int valeur = Integer.parseInt(scanner.nextLine());
            return valeur;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}