package controller;

import model.ProduitFinancier;
import service.ProduitFinancierService;
import view.ProduitFinancierView;
import java.util.List;

public class ProduitFinancierController {
    private ProduitFinancierService service;
    private ProduitFinancierView view;

    public ProduitFinancierController() {
        this.service = new ProduitFinancierService();
        this.view = new ProduitFinancierView();
    }

    public void afficherMenu() {
        boolean continuer = true;

        while (continuer) {
            int choix = view.afficherMenuPrincipal();

            switch (choix) {
                case 1:
                    ajouterProduit();
                    break;
                case 2:
                    afficherTousProduits();
                    break;
                case 3:
                    rechercherProduit();
                    break;
                case 4:
                    modifierProduit();
                    break;
                case 5:
                    supprimerProduit();
                    break;
                case 6:
                    calculerInterets();
                    break;
                case 0:
                    continuer = false;
                    break;
                default:
                    view.afficherMessage("Option invalide!");
            }
        }
    }

    private void ajouterProduit() {
        ProduitFinancier produit = view.saisirProduit();
        if (service.ajouterProduit(produit)) {
            view.afficherMessage("Produit ajouté avec succès!");
        } else {
            view.afficherMessage("Erreur lors de l'ajout du produit.");
        }
    }

    private void afficherTousProduits() {
        List<ProduitFinancier> produits = service.obtenirTousProduits();
        view.afficherListeProduits(produits);
    }

    private void rechercherProduit() {
        int id = view.demanderIdProduit();
        ProduitFinancier produit = service.obtenirProduit(id);
        if (produit != null) {
            view.afficherProduit(produit);
        } else {
            view.afficherMessage("Produit non trouvé.");
        }
    }

    private void modifierProduit() {
        int id = view.demanderIdProduit();
        ProduitFinancier produit = service.obtenirProduit(id);
        if (produit != null) {
            view.afficherProduit(produit);
            ProduitFinancier produitModifie = view.modifierProduit(produit);
            if (service.modifierProduit(produitModifie)) {
                view.afficherMessage("Produit modifié avec succès!");
            } else {
                view.afficherMessage("Erreur lors de la modification.");
            }
        } else {
            view.afficherMessage("Produit non trouvé.");
        }
    }

    private void supprimerProduit() {
        int id = view.demanderIdProduit();
        if (view.confirmerSuppression()) {
            if (service.supprimerProduit(id)) {
                view.afficherMessage("Produit supprimé avec succès!");
            } else {
                view.afficherMessage("Erreur lors de la suppression.");
            }
        }
    }

    private void calculerInterets() {
        int id = view.demanderIdProduit();
        double montant = view.demanderMontant();
        int duree = view.demanderDuree();

        double interets = service.calculerInterets(id, montant, duree);
        view.afficherInterets(interets, montant, duree);
    }
}