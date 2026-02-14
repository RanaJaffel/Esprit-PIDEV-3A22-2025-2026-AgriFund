package com.agrifund.view;

import com.agrifund.model.ProduitFinancier;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormulaireProduitController {

    @FXML private Label lblTitre;
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtTaux;
    @FXML private TextField txtMontantMin;
    @FXML private TextField txtMontantMax;
    @FXML private TextArea txtRegles;

    private ProduitFinancier produitAModifier;
    private boolean enregistre = false;
    private ProduitFinancier produitResultat;

    /**
     * Initialiser pour un nouveau produit
     */
    public void initNouveauProduit(ObservableList<String> typesDisponibles) {
        lblTitre.setText("➕ Nouveau Produit Financier");
        cbType.setItems(typesDisponibles);
        enregistre = false;
    }

    /**
     * Initialiser pour modifier un produit existant
     */
    public void initModifierProduit(ProduitFinancier produit, ObservableList<String> typesDisponibles) {
        lblTitre.setText("✏️ Modifier le Produit");
        cbType.setItems(typesDisponibles);

        this.produitAModifier = produit;

        // Remplir les champs
        txtNom.setText(produit.getNomProduit());
        cbType.setValue(produit.getTypeFinancement());
        txtTaux.setText(String.valueOf(produit.getTauxInteret()));
        txtMontantMin.setText(String.valueOf(produit.getMontantMin()));
        txtMontantMax.setText(String.valueOf(produit.getMontantMax()));
        txtRegles.setText(produit.getReglesFinancieres());

        enregistre = false;
    }

    @FXML
    private void handleEnregistrer() {
        // Validation
        if (!validerChamps()) {
            return;
        }

        try {
            // Créer ou modifier le produit
            if (produitAModifier == null) {
                // Nouveau produit
                produitResultat = new ProduitFinancier(
                        txtNom.getText().trim(),
                        cbType.getValue().trim(),
                        Double.parseDouble(txtTaux.getText().trim()),
                        Double.parseDouble(txtMontantMin.getText().trim()),
                        Double.parseDouble(txtMontantMax.getText().trim()),
                        txtRegles.getText().trim()
                );
            } else {
                // Modification
                produitAModifier.setNomProduit(txtNom.getText().trim());
                produitAModifier.setTypeFinancement(cbType.getValue().trim());
                produitAModifier.setTauxInteret(Double.parseDouble(txtTaux.getText().trim()));
                produitAModifier.setMontantMin(Double.parseDouble(txtMontantMin.getText().trim()));
                produitAModifier.setMontantMax(Double.parseDouble(txtMontantMax.getText().trim()));
                produitAModifier.setReglesFinancieres(txtRegles.getText().trim());
                produitResultat = produitAModifier;
            }

            enregistre = true;
            fermerFenetre();

        } catch (NumberFormatException e) {
            alert("❌ Erreur", "Les valeurs numériques sont incorrectes!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            alert("❌ Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAnnuler() {
        enregistre = false;
        fermerFenetre();
    }

    private boolean validerChamps() {
        if (txtNom.getText().trim().isEmpty()) {
            alert("⚠️ Validation", "Le nom du produit est obligatoire!", Alert.AlertType.WARNING);
            txtNom.requestFocus();
            return false;
        }

        if (cbType.getValue() == null || cbType.getValue().trim().isEmpty()) {
            alert("⚠️ Validation", "Le type de financement est obligatoire!", Alert.AlertType.WARNING);
            cbType.requestFocus();
            return false;
        }

        if (txtTaux.getText().trim().isEmpty()) {
            alert("⚠️ Validation", "Le taux d'intérêt est obligatoire!", Alert.AlertType.WARNING);
            txtTaux.requestFocus();
            return false;
        }

        if (txtMontantMin.getText().trim().isEmpty()) {
            alert("⚠️ Validation", "Le montant minimum est obligatoire!", Alert.AlertType.WARNING);
            txtMontantMin.requestFocus();
            return false;
        }

        if (txtMontantMax.getText().trim().isEmpty()) {
            alert("⚠️ Validation", "Le montant maximum est obligatoire!", Alert.AlertType.WARNING);
            txtMontantMax.requestFocus();
            return false;
        }

        try {
            double min = Double.parseDouble(txtMontantMin.getText().trim());
            double max = Double.parseDouble(txtMontantMax.getText().trim());

            if (min >= max) {
                alert("⚠️ Validation", "Le montant maximum doit être supérieur au minimum!", Alert.AlertType.WARNING);
                return false;
            }

            double taux = Double.parseDouble(txtTaux.getText().trim());
            if (taux < 0 || taux > 100) {
                alert("⚠️ Validation", "Le taux doit être entre 0 et 100%", Alert.AlertType.WARNING);
                return false;
            }

        } catch (NumberFormatException e) {
            alert("❌ Erreur", "Les montants et le taux doivent être des nombres valides!", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void fermerFenetre() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    private void alert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Getters
    public boolean isEnregistre() {
        return enregistre;
    }

    public ProduitFinancier getProduitResultat() {
        return produitResultat;
    }
}