package com.agrifund.services;

import com.agrifund.entities.Utilisateur;
import com.agrifund.entities.Admin;
import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Banque;

import java.sql.SQLException;

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

    public Utilisateur login(String email, String password) throws SQLException {
        Utilisateur user = utilisateurService.rechercherParEmail(email);

        if (user == null) {
            System.out.println("✗ Email non trouvé!");
            return null;
        }

        if (utilisateurService.verifierPassword(password, user.getPassword())) {
            System.out.println("✓ Connexion réussie! Bienvenue " + user.getPrenom() + " " + user.getNom());
            return user;
        } else {
            System.out.println("✗ Mot de passe incorrect!");
            return null;
        }
    }

    public Admin inscrireAdmin(String nom, String prenom, String email, String password, String tel)
            throws SQLException {
        if (utilisateurService.emailExiste(email)) {
            System.out.println("✗ Cet email est déjà utilisé!");
            return null;
        }
        Admin admin = new Admin(nom, prenom, email, password);
        admin.setTel(tel);
        int adminId = adminService.creer(admin);
        if (adminId > 0) {
            System.out.println("✓ Admin inscrit avec succès!");
            return admin;
        }
        return null;
    }

    public Agriculteur inscrireAgriculteur(Agriculteur agriculteur) throws SQLException {
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

    public Banque inscrireBanque(Banque banque) throws SQLException {
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

    public String getTypeUtilisateur(int utilisateurId) throws SQLException {
        if (adminService.estAdmin(utilisateurId)) {
            return "ADMIN";
        }
        Agriculteur agriculteur = agriculteurService.rechercherParUtilisateurId(utilisateurId);
        if (agriculteur != null) {
            return "AGRICULTEUR";
        }
        Banque banque = banqueService.rechercherParUtilisateurId(utilisateurId);
        if (banque != null) {
            return "BANQUE";
        }
        return "INCONNU";
    }

    public String demanderReinitialisationMotDePasse(String email) throws SQLException {
        Utilisateur user = utilisateurService.rechercherParEmail(email);
        if (user == null) {
            System.out.println("✗ Email non trouvé!");
            return null;
        }
        String token = tokenService.genererToken(user.getId());
        System.out.println("✓ Token de réinitialisation généré!");
        boolean emailEnvoye = com.agrifund.util.EmailService.envoyerEmailReinitialisation(email, token);
        if (emailEnvoye) {
            System.out.println("📧 Email de réinitialisation envoyé à: " + email);
            System.out.println("⏰ Le token est valide pendant 1 heure");
        } else {
            System.out.println("⚠️ Impossible d'envoyer l'email (vérifiez la configuration)");
            System.out.println("🔑 Token (à utiliser manuellement): " + token);
        }
        return token;
    }

    public boolean reinitialiserMotDePasse(String token, String nouveauPassword) throws SQLException {
        com.agrifund.entities.TokenReinitialisation tokenObj = tokenService.verifierToken(token);
        if (tokenObj == null) {
            System.out.println("✗ Token invalide!");
            return false;
        }
        if (!tokenObj.isValide()) {
            System.out.println("✗ Token expiré ou déjà utilisé!");
            return false;
        }
        Utilisateur user = utilisateurService.rechercherParId(tokenObj.getUtilisateurId());
        if (user == null) {
            System.out.println("✗ Utilisateur non trouvé!");
            return false;
        }
        utilisateurService.modifierMotDePasse(user.getId(), nouveauPassword);
        tokenService.marquerUtilise(token);
        System.out.println("✓ Mot de passe réinitialisé avec succès!");
        return true;
    }

    public void logout() {
        System.out.println("✓ Déconnexion réussie!");
    }
}