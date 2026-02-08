package com.agrifund;

import controller.ProduitFinancierController;
import controller.OffreFinanciereController;
import util.DatabaseConnection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProduitFinancierController produitController = new ProduitFinancierController();
        OffreFinanciereController offreController = new OffreFinanciereController();

        System.out.println("╔════════════════════════════════╗");
        System.out.println("║     BIENVENUE DANS AGRIFUND   ║");
        System.out.println("║   Système de Gestion Agricole ║");
        System.out.println("╚════════════════════════════════╝");

        boolean continuer = true;

        while (continuer) {
            System.out.println("\n========== MENU PRINCIPAL ==========");
            System.out.println("1. Gestion des Produits Financiers");
            System.out.println("2. Gestion des Offres Financières");
            System.out.println("0. Quitter");
            System.out.print("Votre choix: ");

            int choix = scanner.nextInt();

            switch (choix) {
                case 1:
                    produitController.afficherMenu();
                    break;
                case 2:
                    offreController.afficherMenu();
                    break;
                case 0:
                    continuer = false;
                    System.out.println("\nMerci d'avoir utilisé AgriFund. Au revoir!");
                    DatabaseConnection.closeConnection();
                    break;
                default:
                    System.out.println("Option invalide!");
            }
        }

        scanner.close();
    }
}