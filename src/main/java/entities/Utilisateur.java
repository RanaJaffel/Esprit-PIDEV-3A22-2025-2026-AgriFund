package entities;

import java.time.LocalDateTime;

/**
 * Entité Utilisateur - Classe mère pour Admin, Agriculteur et Banque
 * Correspond à la table Utilisateur dans la base de données
 */
public class Utilisateur {

    // Attributs correspondant aux colonnes de la table
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String tel;
    private LocalDateTime dateInscrit;
    private String photo;

    // Constructeurs
    public Utilisateur() {
        this.dateInscrit = LocalDateTime.now();
    }

    public Utilisateur(String nom, String prenom, String email, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.dateInscrit = LocalDateTime.now();
    }

    public Utilisateur(int id, String nom, String prenom, String email, String password,
                       String tel, LocalDateTime dateInscrit, String photo) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.tel = tel;
        this.dateInscrit = dateInscrit;
        this.photo = photo;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public LocalDateTime getDateInscrit() {
        return dateInscrit;
    }

    public void setDateInscrit(LocalDateTime dateInscrit) {
        this.dateInscrit = dateInscrit;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", tel='" + tel + '\'' +
                ", dateInscrit=" + dateInscrit +
                '}';
    }
}