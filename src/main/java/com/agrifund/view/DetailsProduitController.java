package com.agrifund.view;

import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.PDFGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DetailsProduitController {

    @FXML private Label lblNomProduit;
    @FXML private Label lblId;
    @FXML private Label lblType;
    @FXML private Label lblTaux;
    @FXML private Label lblMontantMin;
    @FXML private Label lblMontantMax;
    @FXML private TextArea txtRegles;
    @FXML private Label lblMontantMoyen;
    @FXML private Label lblEcart;

    private ProduitFinancier produit;

    public void setProduit(ProduitFinancier produit) {
        this.produit = produit;
        afficherDetails();
    }

    private void afficherDetails() {
        if (produit == null) {
            System.err.println("❌ Produit est null!");
            return;
        }

        try {
            lblNomProduit.setText(produit.getNomProduit());
            lblId.setText("#" + produit.getIdProduit());
            lblType.setText(produit.getTypeFinancement());

            // Afficher le taux avec le symbole %
            lblTaux.setText(String.format("%.2f %%", produit.getTauxInteret()));

            // Formater les montants
            lblMontantMin.setText(String.format("%,.2f DT", produit.getMontantMin()));
            lblMontantMax.setText(String.format("%,.2f DT", produit.getMontantMax()));

            String regles = produit.getReglesFinancieres();
            txtRegles.setText(regles != null && !regles.trim().isEmpty() ?
                    regles : "Aucune règle financière spécifiée pour ce produit.");

            // Calculs des statistiques
            double moyenne = (produit.getMontantMin() + produit.getMontantMax()) / 2;
            double ecart = produit.getMontantMax() - produit.getMontantMin();

            lblMontantMoyen.setText(String.format("%,.2f DT", moyenne));
            lblEcart.setText(String.format("%,.2f DT", ecart));

            System.out.println("✅ Détails affichés pour: " + produit.getNomProduit());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage des détails: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleTelechargerProduit() {
        if (produit == null) {
            showAlert("❌ Erreur", "Aucun produit à télécharger!", Alert.AlertType.ERROR);
            return;
        }

        try {

            System.out.println("📥 Génération du PDF pour: " + produit.getNomProduit());

            String fichier = PDFGenerator.genererPDFProduit(produit);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("✅ Téléchargement réussi");
            alert.setHeaderText("PDF généré avec succès !");
            alert.setContentText("📄 Fichier sauvegardé :\n" + fichier);

            alert.showAndWait();

            System.out.println("✅ PDF généré: " + fichier);;

        } catch (Exception e) {
            System.err.println("❌ Erreur génération PDF: " + e.getMessage());
            e.printStackTrace();

            showAlert("❌ Erreur", "Impossible de générer le PDF:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFermer() {
        try {
            Stage stage = (Stage) lblNomProduit.getScene().getWindow();
            stage.close();
            System.out.println("✅ Fenêtre de détails fermée");
        } catch (Exception e) {
            System.err.println("❌ Erreur fermeture: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
