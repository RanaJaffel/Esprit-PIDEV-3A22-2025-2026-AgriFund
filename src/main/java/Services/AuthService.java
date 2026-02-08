package Services;

import entities.Utilisateur;
import entities.Admin;
import entities.Agriculteur;
import entities.Banque;

import java.sql.SQLException;

/**
 * Service d'Authentification
 * Gère le login et l'inscription des utilisateurs
 */
public class AuthService {

    private UtilisateurService utilisateurService;
    private AdminService adminService;
    private AgriculteurService agriculteurService;
    private BanqueService banqueService;
    private TokenReinitialisationService tokenService;

    public AuthService() throws SQLException {
        this.utilisateurService = new UtilisateurService();
        this.adminService = new AdminService();
        this.agriculteurService = new AgriculteurService();
        this.banqueService = new BanqueService();
        this.tokenService = new TokenReinitialisationService();
    }

    /**
     * LOGIN - Authentification d'un utilisateur
     * @param email Email de l'utilisateur
     * @param password Mot de passe
     * @return Utilisateur si authentification réussie, null sinon
     */
    public Utilisateur login(String email, String password) throws SQLException {
        // Rechercher l'utilisateur par email
        Utilisateur user = utilisateurService.rechercherParEmail(email);

        if (user == null) {
            System.out.println("✗ Email non trouvé!");
            return null;
        }

        // Vérifier le mot de passe
        if (utilisateurService.verifierPassword(password, user.getPassword())) {
            System.out.println("✓ Connexion réussie! Bienvenue " + user.getPrenom() + " " + user.getNom());
            return user;
        } else {
            System.out.println("✗ Mot de passe incorrect!");
            return null;
        }
    }

    /**
     * INSCRIPTION - Admin
     */
    public Admin inscrireAdmin(String nom, String prenom, String email, String password, String tel)
            throws SQLException {

        // Vérifier si l'email existe déjà
        if (utilisateurService.emailExiste(email)) {
            System.out.println("✗ Cet email est déjà utilisé!");
            return null;
        }

        // Créer l'admin
        Admin admin = new Admin(nom, prenom, email, password);
        admin.setTel(tel);

        int adminId = adminService.creer(admin);

        if (adminId > 0) {
            System.out.println("✓ Admin inscrit avec succès!");
            return admin;
        }

        return null;
    }

    /**
     * INSCRIPTION - Agriculteur
     */
    public Agriculteur inscrireAgriculteur(Agriculteur agriculteur) throws SQLException {

        // Vérifier si l'email existe déjà
        if (utilisateurService.emailExiste(agriculteur.getEmail())) {
            System.out.println("✗ Cet email est déjà utilisé!");
            return null;
        }

        int agriculteurId = agriculteurService.inscrire(agriculteur);

        if (agriculteurId > 0) {
            System.out.println("✓ Agriculteur inscrit avec succès!");
            System.out.println("⚠ Votre compte est en attente de vérification par un administrateur.");
            return agriculteur;
        }

        return null;
    }

    /**
     * INSCRIPTION - Banque
     */
    public Banque inscrireBanque(Banque banque) throws SQLException {

        // Vérifier si l'email existe déjà
        if (utilisateurService.emailExiste(banque.getEmail())) {
            System.out.println("✗ Cet email est déjà utilisé!");
            return null;
        }

        int banqueId = banqueService.inscrire(banque);

        if (banqueId > 0) {
            System.out.println("✓ Banque inscrite avec succès!");
            System.out.println("⚠ Votre compte est en attente de vérification par un administrateur.");
            return banque;
        }

        return null;
    }

    /**
     * DÉTERMINER le type d'utilisateur
     */
    public String getTypeUtilisateur(int utilisateurId) throws SQLException {
        // Vérifier si c'est un admin
        if (adminService.estAdmin(utilisateurId)) {
            return "ADMIN";
        }

        // Vérifier si c'est un agriculteur
        Agriculteur agriculteur = agriculteurService.rechercherParUtilisateurId(utilisateurId);
        if (agriculteur != null) {
            return "AGRICULTEUR";
        }

        // Vérifier si c'est une banque
        Banque banque = banqueService.rechercherParUtilisateurId(utilisateurId);
        if (banque != null) {
            return "BANQUE";
        }

        return "INCONNU";
    }

    /**
     * DEMANDER une réinitialisation de mot de passe
     */
    public String demanderReinitialisationMotDePasse(String email) throws SQLException {
        Utilisateur user = utilisateurService.rechercherParEmail(email);

        if (user == null) {
            System.out.println("✗ Email non trouvé!");
            return null;
        }

        // Générer un token
        String token = tokenService.genererToken(user.getId());

        System.out.println("✓ Token de réinitialisation généré!");

        // Envoyer l'email
        boolean emailEnvoye = Utils.EmailService.envoyerEmailReinitialisation(email, token);

        if (emailEnvoye) {
            System.out.println("📧 Email de réinitialisation envoyé à: " + email);
            System.out.println("⏰ Le token est valide pendant 1 heure");
        } else {
            System.out.println("⚠️ Impossible d'envoyer l'email (vérifiez la configuration)");
            System.out.println("🔑 Token (à utiliser manuellement): " + token);
        }

        return token;
    }

    /**
     * RÉINITIALISER le mot de passe avec un token
     */
    public boolean reinitialiserMotDePasse(String token, String nouveauPassword) throws SQLException {
        // Vérifier le token
        entities.TokenReinitialisation tokenObj = tokenService.verifierToken(token);

        if (tokenObj == null) {
            System.out.println("✗ Token invalide!");
            return false;
        }

        if (!tokenObj.isValide()) {
            System.out.println("✗ Token expiré ou déjà utilisé!");
            return false;
        }

        // Récupérer l'utilisateur
        Utilisateur user = utilisateurService.rechercherParId(tokenObj.getUtilisateurId());

        if (user == null) {
            System.out.println("✗ Utilisateur non trouvé!");
            return false;
        }

        // Mettre à jour le mot de passe avec la nouvelle méthode
        utilisateurService.modifierMotDePasse(user.getId(), nouveauPassword);

        // Marquer le token comme utilisé
        tokenService.marquerUtilise(token);

        System.out.println("✓ Mot de passe réinitialisé avec succès!");
        return true;
    }

    /**
     * LOGOUT - Déconnexion (simple affichage)
     */
    public void logout() {
        System.out.println("✓ Déconnexion réussie!");
    }
}