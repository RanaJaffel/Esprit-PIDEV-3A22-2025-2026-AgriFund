package view;

import model.ProduitFinancier;
import java.util.List;
import java.util.Scanner;

public class ProduitFinancierView {
    private Scanner scanner;

    public ProduitFinancierView() {
        this.scanner = new Scanner(System.in);
    }

    public int afficherMenuPrincipal() {
        System.out.println("\n========== GESTION DES PRODUITS FINANCIERS ==========");
        System.out.println("1. Ajouter un produit");
        System.out.println("2. Afficher tous les produits");
        System.out.println("3. Rechercher un produit");
        System.out.println("4. Modifier un produit");
        System.out.println("5. Supprimer un produit");
        System.out.println("6. Calculer les intérêts");
        System.out.println("0. Retour");
        System.out.print("Votre choix: ");

        return scanner.nextInt();
    }

    public ProduitFinancier saisirProduit() {
        scanner.nextLine(); // Consommer le retour à la ligne

        System.out.println("\n--- Nouveau Produit Financier ---");

        System.out.print("Nom du produit: ");
        String nom = scanner.nextLine();

        System.out.print("Type de financement: ");
        String type = scanner.nextLine();

        System.out.print("Taux d'intérêt (%): ");
        double taux = scanner.nextDouble();

        System.out.print("Montant minimum: ");
        double min = scanner.nextDouble();

        System.out.print("Montant maximum: ");
        double max = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Règles financières: ");
        String regles = scanner.nextLine();

        return new ProduitFinancier(nom, type, taux, min, max, regles);
    }

    public void afficherListeProduits(List<ProduitFinancier> produits) {
        if (produits.isEmpty()) {
            System.out.println("Aucun produit financier trouvé.");
            return;
        }

        System.out.println("\n========== LISTE DES PRODUITS ==========");
        System.out.printf("%-5s %-30s %-20s %-10s %-15s %-15s%n",
                "ID", "Nom", "Type", "Taux", "Min", "Max");
        System.out.println("-".repeat(95));

        for (ProduitFinancier p : produits) {
            System.out.printf("%-5d %-30s %-20s %-10.2f %-15.2f %-15.2f%n",
                    p.getIdProduit(),
                    p.getNomProduit(),
                    p.getTypeFinancement(),
                    p.getTauxInteret(),
                    p.getMontantMin(),
                    p.getMontantMax());
        }
    }

    public void afficherProduit(ProduitFinancier produit) {
        System.out.println("\n--- Détails du Produit ---");
        System.out.println("ID: " + produit.getIdProduit());
        System.out.println("Nom: " + produit.getNomProduit());
        System.out.println("Type: " + produit.getTypeFinancement());
        System.out.println("Taux d'intérêt: " + produit.getTauxInteret() + "%");
        System.out.println("Montant min: " + produit.getMontantMin());
        System.out.println("Montant max: " + produit.getMontantMax());
        System.out.println("Règles: " + produit.getReglesFinancieres());
    }

    public ProduitFinancier modifierProduit(ProduitFinancier produit) {
        scanner.nextLine();

        System.out.println("\n--- Modification du Produit ---");
        System.out.println("(Appuyez sur Entrée pour conserver la valeur actuelle)");

        System.out.print("Nom [" + produit.getNomProduit() + "]: ");
        String nom = scanner.nextLine();
        if (!nom.isEmpty()) produit.setNomProduit(nom);

        System.out.print("Type [" + produit.getTypeFinancement() + "]: ");
        String type = scanner.nextLine();
        if (!type.isEmpty()) produit.setTypeFinancement(type);

        System.out.print("Taux [" + produit.getTauxInteret() + "]: ");
        String tauxStr = scanner.nextLine();
        if (!tauxStr.isEmpty()) produit.setTauxInteret(Double.parseDouble(tauxStr));

        System.out.print("Montant min [" + produit.getMontantMin() + "]: ");
        String minStr = scanner.nextLine();
        if (!minStr.isEmpty()) produit.setMontantMin(Double.parseDouble(minStr));

        System.out.print("Montant max [" + produit.getMontantMax() + "]: ");
        String maxStr = scanner.nextLine();
        if (!maxStr.isEmpty()) produit.setMontantMax(Double.parseDouble(maxStr));

        System.out.print("Règles [" + produit.getReglesFinancieres() + "]: ");
        String regles = scanner.nextLine();
        if (!regles.isEmpty()) produit.setReglesFinancieres(regles);

        return produit;
    }

    public int demanderIdProduit() {
        System.out.print("Entrez l'ID du produit: ");
        return scanner.nextInt();
    }

    public double demanderMontant() {
        System.out.print("Entrez le montant: ");
        return scanner.nextDouble();
    }

    public int demanderDuree() {
        System.out.print("Entrez la durée (en mois): ");
        return scanner.nextInt();
    }

    public boolean confirmerSuppression() {
        System.out.print("Êtes-vous sûr de vouloir supprimer ? (o/n): ");
        scanner.nextLine();
        String reponse = scanner.nextLine();
        return reponse.equalsIgnoreCase("o") || reponse.equalsIgnoreCase("oui");
    }

    public void afficherMessage(String message) {
        System.out.println("\n>>> " + message);
    }

    public void afficherInterets(double interets, double montant, int duree) {
        System.out.println("\n--- Calcul des Intérêts ---");
        System.out.println("Montant emprunté: " + montant);
        System.out.println("Durée: " + duree + " mois");
        System.out.println("Intérêts: " + interets);
        System.out.println("Total à rembourser: " + (montant + interets));
    }
}