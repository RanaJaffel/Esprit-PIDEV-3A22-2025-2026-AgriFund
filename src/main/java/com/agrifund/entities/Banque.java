package com.agrifund.entities;

/**
 * Entité Banque - hérite de Utilisateur
 * Correspond à la table Banque dans la base de données
 */
public class Banque extends Utilisateur {

    private int banqueId; // ID dans la table Banque
    private int utilisateurId; // Référence vers la table Utilisateur
    private String codeBanque;
    private String addresseSiege;
    private String representantLegal;
    private String adresseAgence;
    private String logo;
    private String siteWeb;
    private String statusCompte;
    private boolean compteVerifie;

    // Constructeurs
    public Banque() {
        super();
        this.statusCompte = "en_attente";
        this.compteVerifie = false;
    }

    public Banque(String nom, String prenom, String email, String password,
                  String codeBanque, String addresseSiege, String representantLegal) {
        super(nom, prenom, email, password);
        this.codeBanque = codeBanque;
        this.addresseSiege = addresseSiege;
        this.representantLegal = representantLegal;
        this.statusCompte = "en_attente";
        this.compteVerifie = false;
    }

    // Getters et Setters
    public int getBanqueId() {
        return banqueId;
    }

    public void setBanqueId(int banqueId) {
        this.banqueId = banqueId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(String codeBanque) {
        this.codeBanque = codeBanque;
    }

    public String getAddresseSiege() {
        return addresseSiege;
    }

    public void setAddresseSiege(String addresseSiege) {
        this.addresseSiege = addresseSiege;
    }

    public String getRepresentantLegal() {
        return representantLegal;
    }

    public void setRepresentantLegal(String representantLegal) {
        this.representantLegal = representantLegal;
    }

    public String getAdresseAgence() {
        return adresseAgence;
    }

    public void setAdresseAgence(String adresseAgence) {
        this.adresseAgence = adresseAgence;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getStatusCompte() {
        return statusCompte;
    }

    public void setStatusCompte(String statusCompte) {
        this.statusCompte = statusCompte;
    }

    public boolean isCompteVerifie() {
        return compteVerifie;
    }

    public void setCompteVerifie(boolean compteVerifie) {
        this.compteVerifie = compteVerifie;
    }

    @Override
    public String toString() {
        return "Banque{" +
                "banqueId=" + banqueId +
                ", codeBanque='" + codeBanque + '\'' +
                ", addresseSiege='" + addresseSiege + '\'' +
                ", representantLegal='" + representantLegal + '\'' +
                ", statusCompte='" + statusCompte + '\'' +
                ", compteVerifie=" + compteVerifie +
                ", " + super.toString() +
                '}';
    }
}