package com.agrifund.controller.admin;

import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.BanqueService;
import com.agrifund.services.ProduitFinancierService;
import com.agrifund.entities.Banque;
import com.agrifund.util.PDFGenerator;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AdminDetailsOffreController {

    @FXML private HBox titleBar;
    @FXML private Label lblWindowTitle;

    @FXML private Label lblNomOffre;
    @FXML private Label lblId;
    @FXML private Label lblStatut;
    @FXML private Label lblProduitNom;
    @FXML private Label lblProduitType;
    @FXML private Label lblProduitTaux;
    @FXML private TextArea txtConditions;

    // Section Banque
    @FXML private VBox sectionBanque;
    @FXML private Label lblBanqueNom;
    @FXML private Label lblBanqueCode;
    @FXML private Label lblBanqueRepresentant;
    @FXML private Label lblBanqueSiege;
    @FXML private Label lblBanqueSiteWeb;
    @FXML private Label lblBanqueStatut;

    private OffreFinanciere offre;
    private BanqueService banqueService;
    private ProduitFinancierService produitService;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        try {
            banqueService = new BanqueService();
            produitService = new ProduitFinancierService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setOffre(OffreFinanciere offre) {
        this.offre = offre;
        afficherDetails();
        chargerInfosProduit();
        chargerInfosBanque();
    }

    private void afficherDetails() {
        if (offre == null) return;

        lblWindowTitle.setText("Détails - " + offre.getNomOffre());
        lblNomOffre.setText(offre.getNomOffre());
        lblId.setText("#" + offre.getIdOffre());

        // Statut avec couleur
        String statut = offre.getStatut();
        lblStatut.setText(statut);
        switch (statut) {
            case "Active":
                lblStatut.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-size: 16; " +
                        "-fx-background-color: #E8F5E9; -fx-padding: 5 15; -fx-background-radius: 10;");
                break;
            case "En pause":
                lblStatut.setStyle("-fx-text-fill: #F57F17; -fx-font-weight: bold; -fx-font-size: 16; " +
                        "-fx-background-color: #FFF8E1; -fx-padding: 5 15; -fx-background-radius: 10;");
                break;
            case "Expiree":
                lblStatut.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold; -fx-font-size: 16; " +
                        "-fx-background-color: #FFEBEE; -fx-padding: 5 15; -fx-background-radius: 10;");
                break;
            default:
                lblStatut.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        }

        // Conditions
        String conditions = offre.getConditions();
        txtConditions.setText(conditions != null && !conditions.trim().isEmpty() ?
                conditions : "Aucune condition spécifiée pour cette offre.");
    }

    private void chargerInfosProduit() {
        if (offre == null) return;

        // Si le produit est déjà chargé
        ProduitFinancier produit = offre.getProduitFinancier();

        if (produit == null && offre.getIdProduit() > 0) {
            produit = produitService.getProduitById(offre.getIdProduit());
        }

        if (produit != null) {
            lblProduitNom.setText(produit.getNomProduit());
            lblProduitType.setText(produit.getTypeFinancement());
            lblProduitTaux.setText(String.format("%.2f %%", produit.getTauxInteret()));
        } else {
            lblProduitNom.setText("N/A");
            lblProduitType.setText("N/A");
            lblProduitTaux.setText("N/A");
        }
    }

    private void chargerInfosBanque() {
        if (offre == null || offre.getBanqueId() == 0) {
            sectionBanque.setVisible(false);
            sectionBanque.setManaged(false);
            return;
        }

        try {
            Banque banque = null;
            for (Banque b : banqueService.afficherTous()) {
                if (b.getBanqueId() == offre.getBanqueId()) {
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
            String filePath = PDFGenerator.genererPDFOffre(offre);
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
            ((Button) event.getSource()).setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onControlButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            ((Button) event.getSource()).setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onCloseButtonHover(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            ((Button) event.getSource()).setStyle("-fx-background-color: #E53935; -fx-text-fill: white; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onCloseButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            ((Button) event.getSource()).setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}