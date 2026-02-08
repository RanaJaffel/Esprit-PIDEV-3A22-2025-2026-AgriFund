package service;

import dao.OffreFinanciereDAO;
import model.OffreFinanciere;
import java.util.List;

public class OffreFinanciereService {
    private OffreFinanciereDAO offreDAO;

    public OffreFinanciereService() {
        this.offreDAO = new OffreFinanciereDAO();
    }

    public boolean ajouterOffre(OffreFinanciere offre) {
        // Définir le statut par défaut
        if (offre.getStatut() == null || offre.getStatut().isEmpty()) {
            offre.setStatut("En attente");
        }
        return offreDAO.create(offre);
    }

    public OffreFinanciere obtenirOffre(int id) {
        return offreDAO.findById(id);
    }

    public List<OffreFinanciere> obtenirToutesOffres() {
        return offreDAO.findAll();
    }

    public List<OffreFinanciere> obtenirOffresParProduit(int idProduit) {
        return offreDAO.findByProduit(idProduit);
    }

    public boolean modifierOffre(OffreFinanciere offre) {
        return offreDAO.update(offre);
    }

    public boolean supprimerOffre(int id) {
        return offreDAO.delete(id);
    }

    public boolean changerStatutOffre(int idOffre, String nouveauStatut) {
        OffreFinanciere offre = offreDAO.findById(idOffre);
        if (offre != null) {
            offre.setStatut(nouveauStatut);
            return offreDAO.update(offre);
        }
        return false;
    }
}