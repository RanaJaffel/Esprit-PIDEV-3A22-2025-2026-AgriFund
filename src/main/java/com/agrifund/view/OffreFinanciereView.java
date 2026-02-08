package view;

import model.OffreFinanciere;
import model.ProduitFinancier;
import java.util.List;
import java.util.Scanner;

public class OffreFinanciereView {
    private Scanner scanner;

    public OffreFinanciereView() {
        this.scanner = new Scanner(System.in);
    }

    public int afficherMenuPrincipal() {
        System.out.println("\n========== GESTION DES OFFRES FINANCIÈRES ==========");
        System.out.println("1. Créer une offre");
        System.out.println("2. Afficher toutes les offres");
        System.out.println("3. Rechercher une offre");
        System.out.println("4. Afficher les offres par produit");
        System.out.println("5. Modifier une offre");
        System.out.println("6. Changer le statut d'une offre");
        System.out.println("7. Supprimer une offre");
        System.out.println("0. Retour");
        System.out.print("Votre choix: ");

        return scanner.nextInt();
    }

    public OffreFinanciere saisirOffre() {
        scanner.nextLine(); // Consommer le retour à la ligne

        System.out.println("\n--- Nouvelle Offre Financière ---");

        System.out.print("Nom de l'offre: ");
        String nom = scanner.nextLine();

        System.out.print("Conditions: ");
        String conditions = scanner.nextLine();

        System.out.println("Statuts disponibles: En attente, Approuvée, Rejetée, En cours");
        System.out.print("Statut (Entrée pour 'En attente'): ");
        String statut = scanner.nextLine();
        if (statut.isEmpty()) {
            statut = "En attente";
        }

        System.out.print("ID du produit financier: ");
        int idProduit = scanner.nextInt();

        return new OffreFinanciere(nom, conditions, statut, idProduit);
    }

    public void afficherListeOffres(List<OffreFinanciere> offres) {
        if (offres.isEmpty()) {
            System.out.println("Aucune offre financière trouvée.");
            return;
        }

        System.out.println("\n========== LISTE DES OFFRES ==========");
        System.out.printf("%-5s %-30s %-15s %-30s %-10s%n",
                "ID", "Nom de l'offre", "Statut", "Produit", "ID Produit");
        System.out.println("-".repeat(90));

        for (OffreFinanciere o : offres) {
            String nomProduit = (o.getProduitFinancier() != null) ?
                    o.getProduitFinancier().getNomProduit() : "N/A";

            System.out.printf("%-5d %-30s %-15s %-30s %-10d%n",
                    o.getIdOffre(),
                    o.getNomOffre(),
                    o.getStatut(),
                    nomProduit,
                    o.getIdProduit());
        }
    }

    public void afficherOffre(OffreFinanciere offre) {
        System.out.println("\n--- Détails de l'Offre ---");
        System.out.println("ID: " + offre.getIdOffre());
        System.out.println("Nom: " + offre.getNomOffre());
        System.out.println("Conditions: " + offre.getConditions());
        System.out.println("Statut: " + offre.getStatut());
        System.out.println("ID Produit: " + offre.getIdProduit());

        if (offre.getProduitFinancier() != null) {
            System.out.println("\n--- Produit Associé ---");
            System.out.println("Nom du produit: " + offre.getProduitFinancier().getNomProduit());
            System.out.println("Type: " + offre.getProduitFinancier().getTypeFinancement());
            System.out.println("Taux d'intérêt: " + offre.getProduitFinancier().getTauxInteret() + "%");
            System.out.println("Montant: " + offre.getProduitFinancier().getMontantMin() +
                    " - " + offre.getProduitFinancier().getMontantMax());
        }
    }

    public void afficherProduitsDisponibles(List<ProduitFinancier> produits) {
        System.out.println("\n--- Produits Disponibles ---");
        System.out.printf("%-5s %-30s %-20s %-10s%n", "ID", "Nom", "Type", "Taux");
        System.out.println("-".repeat(65));

        for (ProduitFinancier p : produits) {
            System.out.printf("%-5d %-30s %-20s %-10.2f%n",
                    p.getIdProduit(),
                    p.getNomProduit(),
                    p.getTypeFinancement(),
                    p.getTauxInteret());
        }
    }

    public OffreFinanciere modifierOffre(OffreFinanciere offre) {
        scanner.nextLine();

        System.out.println("\n--- Modification de l'Offre ---");
        System.out.println("(Appuyez sur Entrée pour conserver la valeur actuelle)");

        System.out.print("Nom [" + offre.getNomOffre() + "]: ");
        String nom = scanner.nextLine();
        if (!nom.isEmpty()) offre.setNomOffre(nom);

        System.out.print("Conditions [" + offre.getConditions() + "]: ");
        String conditions = scanner.nextLine();
        if (!conditions.isEmpty()) offre.setConditions(conditions);

        System.out.print("Statut [" + offre.getStatut() + "]: ");
        String statut = scanner.nextLine();
        if (!statut.isEmpty()) offre.setStatut(statut);

        System.out.print("ID Produit [" + offre.getIdProduit() + "]: ");
        String idProduitStr = scanner.nextLine();
        if (!idProduitStr.isEmpty()) offre.setIdProduit(Integer.parseInt(idProduitStr));

        return offre;
    }

    public String demanderNouveauStatut() {
        scanner.nextLine();
        System.out.println("\nStatuts disponibles:");
        System.out.println("1. En attente");
        System.out.println("2. Approuvée");
        System.out.println("3. Rejetée");
        System.out.println("4. En cours");
        System.out.println("5. Terminée");
        System.out.print("Choisissez le nouveau statut (1-5): ");

        int choix = scanner.nextInt();
        switch (choix) {
            case 1: return "En attente";
            case 2: return "Approuvée";
            case 3: return "Rejetée";
            case 4: return "En cours";
            case 5: return "Terminée";
            default: return "En attente";
        }
    }

    public int demanderIdOffre() {
        System.out.print("Entrez l'ID de l'offre: ");
        return scanner.nextInt();
    }

    public int demanderIdProduit() {
        System.out.print("Entrez l'ID du produit: ");
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
}