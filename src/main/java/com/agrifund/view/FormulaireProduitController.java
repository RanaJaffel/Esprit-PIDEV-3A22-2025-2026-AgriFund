package com.agrifund.view;

import com.agrifund.controller.ProduitFinancierController;
import com.agrifund.model.ProduitFinancier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FormulaireProduitController {

    // Title Bar elements
    @FXML private HBox titleBar;
    @FXML private Label lblWindowTitle;
    @FXML private Button btnClose;

    // Form elements
    @FXML private Label lblTitre;
    @FXML private Label lblSousTitre;
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtTaux;
    @FXML private TextField txtMontantMin;
    @FXML private TextField txtMontantMax;
    @FXML private TextArea txtRegles;
    @FXML private Button btnValider;

    private ProduitFinancierController produitController;
    private ProduitFinancier produitAModifier;
    private boolean modeModification = false;
    private Runnable onSuccess;

    // For dragging the window
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        produitController = new ProduitFinancierController();

        // Charger les types de financement
        cbType.setItems(FXCollections.observableArrayList(
                "Pret", "Credit", "Subvention", "Leasing", "Microfinance", "Autre"
        ));
    }

    // ==================== TITLE BAR CONTROLS ====================

    @FXML
    private void handleTitleBarPressed(MouseEvent event) {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        xOffset = stage.getX() - event.getScreenX();
        yOffset = stage.getY() - event.getScreenY();
    }

    @FXML
    private void handleTitleBarDragged(MouseEvent event) {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void handleFermer() {
        fermerFenetre();
    }

    @FXML
    private void onControlButtonHover(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #DDDDDD; -fx-text-fill: #333333; " +
                "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                "-fx-cursor: hand; -fx-background-radius: 3;");
    }

    @FXML
    private void onControlButtonExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; " +
                "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                "-fx-cursor: hand; -fx-background-radius: 3;");
    }

    @FXML
    private void onCloseButtonHover(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; " +
                "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                "-fx-cursor: hand; -fx-background-radius: 3;");
    }

    @FXML
    private void onCloseButtonExit(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; " +
                "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 12; " +
                "-fx-cursor: hand; -fx-background-radius: 3;");
    }

    // ==================== MODE CONFIGURATION ====================

    /**
     * Mode AJOUT - Nouveau produit
     */
    public void setModeAjout() {
        modeModification = false;
        lblWindowTitle.setText("Nouveau Produit");
        lblTitre.setText("NOUVEAU PRODUIT");
        if (lblSousTitre != null) {
            lblSousTitre.setText("Remplissez les informations du nouveau produit");
        }
        btnValider.setText("CREER LE PRODUIT");

        // Vider les champs
        txtNom.clear();
        cbType.setValue(null);
        txtTaux.clear();
        txtMontantMin.clear();
        txtMontantMax.clear();
        txtRegles.clear();
    }

    /**
     * Mode MODIFICATION - Modifier un produit existant
     */
    public void setModeModification(ProduitFinancier produit) {
        modeModification = true;
        produitAModifier = produit;

        lblWindowTitle.setText("Modifier Produit - " + produit.getNomProduit());
        lblTitre.setText("MODIFIER LE PRODUIT");
        if (lblSousTitre != null) {
            lblSousTitre.setText("Modifiez les informations du produit");
        }
        btnValider.setText("ENREGISTRER");

        // Remplir les champs avec les donnees du produit
        txtNom.setText(produit.getNomProduit());
        cbType.setValue(produit.getTypeFinancement());
        txtTaux.setText(String.valueOf(produit.getTauxInteret()));
        txtMontantMin.setText(String.valueOf(produit.getMontantMin()));
        txtMontantMax.setText(String.valueOf(produit.getMontantMax()));
        txtRegles.setText(produit.getReglesFinancieres());
    }

    /**
     * Callback pour rafraichir la liste apres succes
     */
    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    // ==================== FORM ACTIONS ====================

    @FXML
    private void handleValider() {
        if (!validerChamps()) {
            return;
        }

        try {
            if (modeModification) {
                // MODE MODIFICATION
                produitAModifier.setNomProduit(txtNom.getText().trim());
                produitAModifier.setTypeFinancement(cbType.getValue().trim());
                produitAModifier.setTauxInteret(Double.parseDouble(txtTaux.getText().trim()));
                produitAModifier.setMontantMin(Double.parseDouble(txtMontantMin.getText().trim()));
                produitAModifier.setMontantMax(Double.parseDouble(txtMontantMax.getText().trim()));
                produitAModifier.setReglesFinancieres(txtRegles.getText().trim());

                boolean success = produitController.modifierProduit(produitAModifier);

                if (success) {
                    showAlert("Succes", "Produit modifie avec succes!", Alert.AlertType.INFORMATION);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    fermerFenetre();
                } else {
                    showAlert("Erreur", "Impossible de modifier le produit!", Alert.AlertType.ERROR);
                }

            } else {
                // MODE AJOUT
                ProduitFinancier nouveauProduit = new ProduitFinancier();
                nouveauProduit.setNomProduit(txtNom.getText().trim());
                nouveauProduit.setTypeFinancement(cbType.getValue().trim());
                nouveauProduit.setTauxInteret(Double.parseDouble(txtTaux.getText().trim()));
                nouveauProduit.setMontantMin(Double.parseDouble(txtMontantMin.getText().trim()));
                nouveauProduit.setMontantMax(Double.parseDouble(txtMontantMax.getText().trim()));
                nouveauProduit.setReglesFinancieres(txtRegles.getText().trim());

                boolean success = produitController.ajouterProduit(nouveauProduit);

                if (success) {
                    showAlert("Succes", "Produit cree avec succes!", Alert.AlertType.INFORMATION);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    fermerFenetre();
                } else {
                    showAlert("Erreur", "Impossible de creer le produit!", Alert.AlertType.ERROR);
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Les valeurs numeriques sont incorrectes!", Alert.AlertType.ERROR);
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

        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            erreurs += "Le nom du produit est obligatoire\n";
        }

        if (cbType.getValue() == null || cbType.getValue().trim().isEmpty()) {
            erreurs += "Le type de financement est obligatoire\n";
        }

        if (txtTaux.getText() == null || txtTaux.getText().trim().isEmpty()) {
            erreurs += "Le taux d'interet est obligatoire\n";
        } else {
            try {
                double taux = Double.parseDouble(txtTaux.getText().trim());
                if (taux < 0 || taux > 100) {
                    erreurs += "Le taux doit etre entre 0 et 100%\n";
                }
            } catch (NumberFormatException e) {
                erreurs += "Le taux doit etre un nombre valide\n";
            }
        }

        if (txtMontantMin.getText() == null || txtMontantMin.getText().trim().isEmpty()) {
            erreurs += "Le montant minimum est obligatoire\n";
        }

        if (txtMontantMax.getText() == null || txtMontantMax.getText().trim().isEmpty()) {
            erreurs += "Le montant maximum est obligatoire\n";
        }

        try {
            if (!txtMontantMin.getText().trim().isEmpty() && !txtMontantMax.getText().trim().isEmpty()) {
                double min = Double.parseDouble(txtMontantMin.getText().trim());
                double max = Double.parseDouble(txtMontantMax.getText().trim());
                if (min >= max) {
                    erreurs += "Le montant maximum doit etre superieur au minimum\n";
                }
                if (min < 0) {
                    erreurs += "Le montant minimum ne peut pas etre negatif\n";
                }
            }
        } catch (NumberFormatException e) {
            erreurs += "Les montants doivent etre des nombres valides\n";
        }

        if (!erreurs.isEmpty()) {
            showAlert("Validation", erreurs, Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void fermerFenetre() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}