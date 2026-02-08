package service;

import dao.ProduitFinancierDAO;
import model.ProduitFinancier;
import java.util.List;

public class ProduitFinancierService {
    private ProduitFinancierDAO produitDAO;

    public ProduitFinancierService() {
        this.produitDAO = new ProduitFinancierDAO();
    }

    public boolean ajouterProduit(ProduitFinancier produit) {
        // Validation des données
        if (produit.getMontantMin() >= produit.getMontantMax()) {
            System.err.println("Le montant minimum doit être inférieur au montant maximum");
            return false;
        }
        if (produit.getTauxInteret() < 0) {
            System.err.println("Le taux d'intérêt ne peut pas être négatif");
            return false;
        }
        return produitDAO.create(produit);
    }

    public ProduitFinancier obtenirProduit(int id) {
        return produitDAO.findById(id);
    }

    public List<ProduitFinancier> obtenirTousProduits() {
        return produitDAO.findAll();
    }

    public boolean modifierProduit(ProduitFinancier produit) {
        return produitDAO.update(produit);
    }

    public boolean supprimerProduit(int id) {
        return produitDAO.delete(id);
    }

    public double calculerInterets(int idProduit, double montant, int dureeEnMois) {
        ProduitFinancier produit = produitDAO.findById(idProduit);
        if (produit != null) {
            return montant * (produit.getTauxInteret() / 100) * (dureeEnMois / 12.0);
        }
        return 0;
    }
}