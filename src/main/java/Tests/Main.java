package Tests;

import entities.projectagricole;
import entities.ressourceproject;
import services.projectagricoleCRUD;
import services.ressourceprojectCRUD;
import utils.MyDatabase;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // ✅ Test Singleton (connection)
        MyDatabase db = MyDatabase.getInstance();

        // CRUD services
        projectagricoleCRUD projectCRUD = new projectagricoleCRUD();
        ressourceprojectCRUD ressourceCRUD = new ressourceprojectCRUD();

        try {
            // ==============================
            // 1️⃣ AJOUT PROJECT AGRICOLE
            // ==============================
            projectagricole p = new projectagricole(
                    "Projet Blé",
                    25.5f,
                    new BigDecimal("12000.00"),
                    "en cours",
                    Date.valueOf(LocalDate.now())
            );

            projectCRUD.ajouter(p);
            System.out.println("✅ Project ajouté : " + p);

            // ==============================
            // 2️⃣ AFFICHER PROJECTS
            // ==============================
            List<projectagricole> projects = projectCRUD.afficher();
            System.out.println("\n Liste des projets :");
            projects.forEach(System.out::println);

            // ==============================
            // 3️⃣ AJOUT RESSOURCE
            // ==============================
            ressourceproject r = new ressourceproject(
                    "Tracteur",
                    "equipement",
                    1,
                    new BigDecimal("8000.00"),
                    "Fournisseur A",
                    "prevu",
                    Date.valueOf(LocalDate.now()),
                    p.getIdproject()
            );

            ressourceCRUD.ajouter(r);
            System.out.println("\n Ressource ajoutée : " + r);

            // ==============================
            // 4️⃣ AFFICHER RESSOURCES
            // ==============================
            List<ressourceproject> ressources = ressourceCRUD.afficher();
            System.out.println("\n Liste des ressources :");
            ressources.forEach(System.out::println);

            // ==============================
            // 5️⃣ MODIFIER PROJECT
            // ==============================
            p.setStatut("accepte");
            projectCRUD.modifier(p);
            System.out.println("\n✏ Project modifié : " + p);

            // ==============================
            // 6️⃣ SUPPRIMER RESSOURCE (optionnel)
            // ==============================
            // ressourceCRUD.supprimer(r.getIdressource());
            // System.out.println("🗑️ Ressource supprimée");

        } catch (SQLException e) {
            System.out.println(" Erreur SQL : " + e.getMessage());
        }
    }
}
