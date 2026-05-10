package com.agrifund.controller.banque;

import com.agrifund.services.ProduitFinancierService;
import com.agrifund.model.ProduitFinancier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class BanqueFormulaireProduitController {

    @FXML private HBox titleBar;
    @FXML private Label lblWindowTitle;
    @FXML private Button btnClose;
    @FXML private ImageView imgLogo;

    @FXML private Label lblTitre;
    @FXML private Label lblSousTitre;
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtTaux;
    @FXML private TextField txtPrixFixe;
    @FXML private TextArea txtRegles;
    @FXML private Button btnValider;

    private ProduitFinancierService produitService;
    private ProduitFinancier produitAModifier;
    private boolean modeModification = false;
    private int banqueId;
    private Runnable onSuccess;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        produitService = new ProduitFinancierService();

        cbType.setItems(FXCollections.observableArrayList(
                "Pret", "Credit", "Subvention", "Leasing", "Microfinance", "Autre"
        ));

        try {
            if (imgLogo != null) {
                Image logo = new Image(getClass().getResourceAsStream("/com/agrifund/images/image_2026-01-25_212530957-removebg-preview.png"));
                imgLogo.setImage(logo);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Logo non chargé: " + e.getMessage());
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
    private void handleFermer() {
        fermerFenetre();
    }

    // Dans BanqueFormulaireOffreController et BanqueFormulaireProduitController

    @FXML
    private void onControlButtonHover(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setStyle("-fx-background-color: rgba(178,217,68,0.3); -fx-text-fill: #B2D944; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                    "-fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onControlButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                    "-fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onCloseButtonHover(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setStyle("-fx-background-color: #E1B323; -fx-text-fill: #133D03; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                    "-fx-cursor: hand; -fx-background-radius: 3;");
        }
    }

    @FXML
    private void onCloseButtonExit(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button btn = (Button) event.getSource();
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                    "-fx-cursor: hand; -fx-background-radius: 3;");
        }
    }
    public void setModeAjout(int banqueId) {
        this.banqueId = banqueId;
        modeModification = false;

        if (lblWindowTitle != null) lblWindowTitle.setText("Nouveau Produit");
        if (lblTitre != null) lblTitre.setText("NOUVEAU PRODUIT");
        if (lblSousTitre != null) lblSousTitre.setText("Remplissez les informations du nouveau produit");
        if (btnValider != null) btnValider.setText("CRÉER LE PRODUIT");

        if (txtNom != null) txtNom.clear();
        if (cbType != null) cbType.setValue(null);
        if (txtTaux != null) txtTaux.clear();
        if (txtPrixFixe != null) txtPrixFixe.clear();
        if (txtRegles != null) txtRegles.clear();
    }

    public void setModeModification(ProduitFinancier produit, int banqueId) {
        this.banqueId = banqueId;
        modeModification = true;
        produitAModifier = produit;

        if (lblWindowTitle != null) lblWindowTitle.setText("Modifier Produit - " + produit.getNomProduit());
        if (lblTitre != null) lblTitre.setText("MODIFIER LE PRODUIT");
        if (lblSousTitre != null) lblSousTitre.setText("Modifiez les informations du produit");
        if (btnValider != null) btnValider.setText("ENREGISTRER");

        if (txtNom != null) txtNom.setText(produit.getNomProduit());
        if (cbType != null) cbType.setValue(produit.getTypeFinancement());
        if (txtTaux != null) txtTaux.setText(String.valueOf(produit.getTauxInteret()));
        if (txtPrixFixe != null) txtPrixFixe.setText(String.valueOf(produit.getPrixFixe()));
        if (txtRegles != null) txtRegles.setText(produit.getReglesFinancieres());
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    private void handleValider() {
        if (!validerChamps()) {
            return;
        }

        try {
            if (modeModification) {
                produitAModifier.setNomProduit(txtNom.getText().trim());
                produitAModifier.setTypeFinancement(cbType.getValue().trim());
                produitAModifier.setTauxInteret(Double.parseDouble(txtTaux.getText().trim()));
                produitAModifier.setPrixFixe(Double.parseDouble(txtPrixFixe.getText().trim()));
                produitAModifier.setReglesFinancieres(txtRegles.getText().trim());
                produitAModifier.setBanqueId(banqueId);

                boolean success = produitService.modifierProduit(produitAModifier);

                if (success) {
                    showAlert("Succès", "Produit modifié avec succès!", Alert.AlertType.INFORMATION);
                    if (onSuccess != null) onSuccess.run();
                    fermerFenetre();
                } else {
                    showAlert("Erreur", "Impossible de modifier le produit!", Alert.AlertType.ERROR);
                }

            } else {
                ProduitFinancier nouveauProduit = new ProduitFinancier();
                nouveauProduit.setNomProduit(txtNom.getText().trim());
                nouveauProduit.setTypeFinancement(cbType.getValue().trim());
                nouveauProduit.setTauxInteret(Double.parseDouble(txtTaux.getText().trim()));
                nouveauProduit.setPrixFixe(Double.parseDouble(txtPrixFixe.getText().trim()));
                nouveauProduit.setReglesFinancieres(txtRegles.getText().trim());
                nouveauProduit.setBanqueId(banqueId);

                boolean success = produitService.ajouterProduit(nouveauProduit);

                if (success) {
                    showAlert("Succès", "Produit créé avec succès!", Alert.AlertType.INFORMATION);
                    if (onSuccess != null) onSuccess.run();
                    fermerFenetre();
                } else {
                    showAlert("Erreur", "Impossible de créer le produit!", Alert.AlertType.ERROR);
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Les valeurs numériques sont incorrectes!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private boolean validerChamps() {
        String erreurs = "";

        if (txtNom == null || txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            erreurs += "Le nom du produit est obligatoire\n";
        }

        if (cbType == null || cbType.getValue() == null || cbType.getValue().trim().isEmpty()) {
            erreurs += "Le type de financement est obligatoire\n";
        }

        if (txtTaux == null || txtTaux.getText() == null || txtTaux.getText().trim().isEmpty()) {
            erreurs += "Le taux d'intérêt est obligatoire\n";
        } else {
            try {
                double taux = Double.parseDouble(txtTaux.getText().trim());
                if (taux < 0 || taux > 100) {
                    erreurs += "Le taux doit être entre 0 et 100%\n";
                }
            } catch (NumberFormatException e) {
                erreurs += "Le taux doit être un nombre valide\n";
            }
        }

        if (txtPrixFixe == null || txtPrixFixe.getText() == null || txtPrixFixe.getText().trim().isEmpty()) {
            erreurs += "Le prix fixe est obligatoire\n";
        } else {
            try {
                double prix = Double.parseDouble(txtPrixFixe.getText().trim());
                if (prix < 0) {
                    erreurs += "Le prix fixe ne peut pas etre negatif\n";
                }
            } catch (NumberFormatException e) {
                erreurs += "Le prix fixe doit etre un nombre valide\n";
            }
        }
if (!erreurs.isEmpty()) {
            showAlert("Validation", erreurs, Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void fermerFenetre() {
        try {
            if (txtNom != null && txtNom.getScene() != null) {
                Stage stage = (Stage) txtNom.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            System.err.println("Erreur fermeture: " + e.getMessage());
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