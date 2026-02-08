package controller;

import model.OffreFinanciere;
import model.ProduitFinancier;
import service.OffreFinanciereService;
import service.ProduitFinancierService;
import view.OffreFinanciereView;
import java.util.List;

public class OffreFinanciereController {
    private OffreFinanciereService service;
    private ProduitFinancierService produitService;
    private OffreFinanciereView view;

    public OffreFinanciereController() {
        this.service = new OffreFinanciereService();
        this.produitService = new ProduitFinancierService();
        this.view = new OffreFinanciereView();
    }

    public void afficherMenu() {
        boolean continuer = true;

        while (continuer) {
            int choix = view.afficherMenuPrincipal();

            switch (choix) {
                case 1:
                    ajouterOffre();
                    break;
                case 2:
                    afficherToutesOffres();
                    break;
                case 3:
                    rechercherOffre();
                    break;
                case 4:
                    afficherOffresParProduit();
                    break;
                case 5:
                    modifierOffre();
                    break;
                case 6:
                    changerStatutOffre();
                    break;
                case 7:
                    supprimerOffre();
                    break;
                case 0:
                    continuer = false;
                    break;
                default:
                    view.afficherMessage("Option invalide!");
            }
        }
    }

    private void ajouterOffre() {
        // Afficher les produits disponibles
        List<ProduitFinancier> produits = produitService.obtenirTousProduits();
        if (produits.isEmpty()) {
            view.afficherMessage("Aucun produit financier disponible. Veuillez d'abord créer un produit.");
            return;
        }

        view.afficherProduitsDisponibles(produits);
        OffreFinanciere offre = view.saisirOffre();

        // Vérifier que le produit existe
        ProduitFinancier produit = produitService.obtenirProduit(offre.getIdProduit());
        if (produit == null) {
            view.afficherMessage("ID de produit invalide!");
            return;
        }

        if (service.ajouterOffre(offre)) {
            view.afficherMessage("Offre ajoutée avec succès!");
        } else {
            view.afficherMessage("Erreur lors de l'ajout de l'offre.");
        }
    }

    private void afficherToutesOffres() {
        List<OffreFinanciere> offres = service.obtenirToutesOffres();
        view.afficherListeOffres(offres);
    }

    private void rechercherOffre() {
        int id = view.demanderIdOffre();
        OffreFinanciere offre = service.obtenirOffre(id);
        if (offre != null) {
            view.afficherOffre(offre);
        } else {
            view.afficherMessage("Offre non trouvée.");
        }
    }

    private void afficherOffresParProduit() {
        List<ProduitFinancier> produits = produitService.obtenirTousProduits();
        if (produits.isEmpty()) {
            view.afficherMessage("Aucun produit financier disponible.");
            return;
        }

        view.afficherProduitsDisponibles(produits);
        int idProduit = view.demanderIdProduit();

        List<OffreFinanciere> offres = service.obtenirOffresParProduit(idProduit);
        if (offres.isEmpty()) {
            view.afficherMessage("Aucune offre trouvée pour ce produit.");
        } else {
            view.afficherListeOffres(offres);
        }
    }

    private void modifierOffre() {
        int id = view.demanderIdOffre();
        OffreFinanciere offre = service.obtenirOffre(id);
        if (offre != null) {
            view.afficherOffre(offre);

            // Afficher les produits disponibles pour modification
            List<ProduitFinancier> produits = produitService.obtenirTousProduits();
            view.afficherProduitsDisponibles(produits);

            OffreFinanciere offreModifiee = view.modifierOffre(offre);
            if (service.modifierOffre(offreModifiee)) {
                view.afficherMessage("Offre modifiée avec succès!");
            } else {
                view.afficherMessage("Erreur lors de la modification.");
            }
        } else {
            view.afficherMessage("Offre non trouvée.");
        }
    }

    private void changerStatutOffre() {
        int id = view.demanderIdOffre();
        OffreFinanciere offre = service.obtenirOffre(id);
        if (offre != null) {
            view.afficherOffre(offre);
            String nouveauStatut = view.demanderNouveauStatut();
            if (service.changerStatutOffre(id, nouveauStatut)) {
                view.afficherMessage("Statut modifié avec succès!");
            } else {
                view.afficherMessage("Erreur lors du changement de statut.");
            }
        } else {
            view.afficherMessage("Offre non trouvée.");
        }
    }

    private void supprimerOffre() {
        int id = view.demanderIdOffre();
        if (view.confirmerSuppression()) {
            if (service.supprimerOffre(id)) {
                view.afficherMessage("Offre supprimée avec succès!");
            } else {
                view.afficherMessage("Erreur lors de la suppression.");
            }
        }
    }
}