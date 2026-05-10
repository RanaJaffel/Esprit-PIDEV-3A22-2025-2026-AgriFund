package com.agrifund.controller.admin;

import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.BanqueService;
import com.agrifund.entities.Banque;
import com.agrifund.util.PDFGenerator;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AdminDetailsProduitController {

    @FXML private HBox titleBar;
    @FXML private Label lblWindowTitle;

    @FXML private Label lblNomProduit;
    @FXML private Label lblId;
    @FXML private Label lblType;
    @FXML private Label lblTaux;
    @FXML private Label lblPrixFixe;
    @FXML private TextArea txtRegles;
    @FXML private Label lblMontantMoyen;
    @FXML private Label lblEcart;

    // Section Banque
    @FXML private VBox sectionBanque;
    @FXML private Label lblBanqueNom;
    @FXML private Label lblBanqueCode;
    @FXML private Label lblBanqueRepresentant;
    @FXML private Label lblBanqueSiege;
    @FXML private Label lblBanqueSiteWeb;
    @FXML private Label lblBanqueStatut;

    private ProduitFinancier produit;
    private BanqueService banqueService;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        try {
            banqueService = new BanqueService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setProduit(ProduitFinancier produit) {
        this.produit = produit;
        afficherDetails();
        chargerInfosBanque();
    }

    private void afficherDetails() {
        if (produit == null) return;

        lblWindowTitle.setText("Détails - " + produit.getNomProduit());
        lblNomProduit.setText(produit.getNomProduit());
        lblId.setText("#" + produit.getIdProduit());
        lblType.setText(produit.getTypeFinancement());
        lblTaux.setText(String.format("%.2f %%", produit.getTauxInteret()));
        lblPrixFixe.setText(String.format("%,.2f DT", produit.getPrixFixe()));

        String regles = produit.getReglesFinancieres();
        txtRegles.setText(regles != null && !regles.trim().isEmpty() ?
                regles : "Aucune règle financière spécifiée.");

        double moyenne = produit.getPrixFixe();
        double ecart = 0;
        lblMontantMoyen.setText(String.format("%,.2f DT", moyenne));
        lblEcart.setText(String.format("%,.2f DT", ecart));
    }

    private void chargerInfosBanque() {
        if (produit == null || produit.getBanqueId() == 0) {
            sectionBanque.setVisible(false);
            sectionBanque.setManaged(false);
            return;
        }

        try {
            // Rechercher la banque par l'ID de la banque (pas utilisateur_id)
            // On doit adapter la recherche
            Banque banque = null;
            for (Banque b : banqueService.afficherTous()) {
                if (b.getBanqueId() == produit.getBanqueId()) {
                    banque = b;
                    break;
                }
            }

            if (banque != null) {
                sectionBanque.setVisible(true);
                sectionBanque.setManaged(true);

                lblBanqueNom.setText(banque.getNom());
                lblBanqueCode.setText(banque.getCodeBanque());
                lblBanqueRepresentant.setText(banque.getRepresentantLegal());
                lblBanqueSiege.setText(banque.getAddresseSiege());
                lblBanqueSiteWeb.setText(banque.getSiteWeb() != null ? banque.getSiteWeb() : "Non renseigné");
                lblBanqueStatut.setText(banque.isCompteVerifie() ? "✓ Vérifiée" : "⏳ En attente");
            } else {
                sectionBanque.setVisible(false);
                sectionBanque.setManaged(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sectionBanque.setVisible(false);
            sectionBanque.setManaged(false);
        }
    }

    @FXML
    private void handleTitleBarPressed(MouseEvent event) {
        if (titleBar != null && titleBar.getScene() != null) {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        }
    }

    @FXML
    private void handleTitleBarDragged(MouseEvent event) {
        if (titleBar != null && titleBar.getScene() != null) {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        }
    }

    @FXML
    private void handleMinimize() {
        ((Stage) titleBar.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void handleTelechargerPDF() {
        try {
            String filePath = PDFGenerator.genererPDFProduit(produit);
            showAlert("PDF généré: " + filePath, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFermer() {
        ((Stage) titleBar.getScene().getWindow()).close();
    }

    @FXML
    private void onControlButtonHover(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            ((Button) event.getSource()).setStyle("-fx-background-color: #DDDDDD; -fx-text-fill: #333333; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onControlButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            ((Button) event.getSource()).setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onCloseButtonHover(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            ((Button) event.getSource()).setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onCloseButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            ((Button) event.getSource()).setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
