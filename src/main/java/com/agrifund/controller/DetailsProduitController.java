package com.agrifund.controller;

import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.PDFGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DetailsProduitController {

    // Title Bar elements
    @FXML private HBox titleBar;
    @FXML private Label lblWindowTitle;
    @FXML private ImageView imgLogo;

    // Content elements
    @FXML private Label lblNomProduit;
    @FXML private Label lblId;
    @FXML private Label lblType;
    @FXML private Label lblTaux;
    @FXML private Label lblPrixFixe;
    @FXML private TextArea txtRegles;
    @FXML private Label lblMontantMoyen;
    @FXML private Label lblEcart;

    private ProduitFinancier produit;

    // For dragging the window
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Charger le logo
        try {
            if (imgLogo != null) {
                Image logo = new Image(getClass().getResourceAsStream("/com/agrifund/images/image_2026-01-25_212530957-removebg-preview.png"));
                imgLogo.setImage(logo);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Logo non charge: " + e.getMessage());
        }
    }

    // ==================== TITLE BAR CONTROLS ====================

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
        if (titleBar != null && titleBar.getScene() != null) {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setIconified(true);
        }
    }

    @FXML
    private void handleMaximize() {
        if (titleBar != null && titleBar.getScene() != null) {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        }
    }

    @FXML
    private void onControlButtonHover(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setStyle("-fx-background-color: #DDDDDD; -fx-text-fill: #333333; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                    "-fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onControlButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                    "-fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onCloseButtonHover(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                    "-fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onCloseButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                    "-fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    // ==================== PRODUIT METHODS ====================

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
            // Update window title
            if (lblWindowTitle != null) {
                lblWindowTitle.setText("Details - " + produit.getNomProduit());
            }

            if (lblNomProduit != null) {
                lblNomProduit.setText(produit.getNomProduit());
            }

            if (lblId != null) {
                lblId.setText("#" + produit.getIdProduit());
            }

            if (lblType != null) {
                lblType.setText(produit.getTypeFinancement());
            }

            // Afficher le taux avec le symbole %
            if (lblTaux != null) {
                lblTaux.setText(String.format("%.2f %%", produit.getTauxInteret()));
            }

            // Formater les montants
            if (lblPrixFixe != null) {
                lblPrixFixe.setText(String.format("%,.2f DT", produit.getPrixFixe()));
            }

            if (lblPrixFixe != null) {
                lblPrixFixe.setText(String.format("%,.2f DT", produit.getPrixFixe()));
            }

            if (txtRegles != null) {
                String regles = produit.getReglesFinancieres();
                txtRegles.setText(regles != null && !regles.trim().isEmpty() ?
                        regles : "Aucune regle financiere specifiee pour ce produit.");
            }

            // Calculs des statistiques
            double moyenne = produit.getPrixFixe();
            double ecart = 0;

            if (lblMontantMoyen != null) {
                lblMontantMoyen.setText(String.format("%,.2f DT", moyenne));
            }

            if (lblEcart != null) {
                lblEcart.setText(String.format("%,.2f DT", ecart));
            }

            System.out.println("✅ Details affiches pour: " + produit.getNomProduit());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage des details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTelechargerProduit() {
        if (produit == null) {
            showAlert("Erreur", "Aucun produit a telecharger!", Alert.AlertType.ERROR);
            return;
        }

        try {
            System.out.println("📥 Generation du PDF pour: " + produit.getNomProduit());

            String fichier = PDFGenerator.genererPDFProduit(produit);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Telechargement reussi");
            alert.setHeaderText("PDF genere avec succes!");
            alert.setContentText("Fichier sauvegarde:\n" + fichier);
            alert.showAndWait();

            System.out.println("✅ PDF genere: " + fichier);

        } catch (Exception e) {
            System.err.println("❌ Erreur generation PDF: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de generer le PDF:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFermer() {
        try {
            Stage stage = null;

            if (titleBar != null && titleBar.getScene() != null) {
                stage = (Stage) titleBar.getScene().getWindow();
            } else if (lblNomProduit != null && lblNomProduit.getScene() != null) {
                stage = (Stage) lblNomProduit.getScene().getWindow();
            }

            if (stage != null) {
                stage.close();
                System.out.println("✅ Fenetre de details fermee");
            }
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

