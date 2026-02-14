package com.agrifund.view;

import com.agrifund.controller.OffreFinanciereController;
import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FormulaireOffreController {

    @FXML
    private Label lblTitre;
    @FXML
    private Label lblSousTitre;
    @FXML
    private TextField txtNomOffre;
    @FXML
    private ComboBox<ProduitFinancier> cmbProduit;
    @FXML
    private ComboBox<String> cmbStatut;
    @FXML
    private TextArea txtConditions;
    @FXML
    private Button btnValider;

    private OffreFinanciereController offreController;
    private OffreFinanciere offreAModifier;
    private boolean modeModification = false;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        offreController = new OffreFinanciereController();

        // Charger les statuts
        cmbStatut.setItems(FXCollections.observableArrayList("Active", "En pause", "Expiree"));

        // Charger les produits
        chargerProduits();
    }

    private void chargerProduits() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM produit_financier ORDER BY nom_produit";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            List<ProduitFinancier> produits = new ArrayList<>();
            while (rs.next()) {
                ProduitFinancier p = new ProduitFinancier();
                p.setIdProduit(rs.getInt("id_produit"));
                p.setNomProduit(rs.getString("nom_produit"));
                p.setTypeFinancement(rs.getString("type_financement"));
                p.setTauxInteret(rs.getDouble("taux_interet"));
                p.setMontantMin(rs.getDouble("montant_min"));
                p.setMontantMax(rs.getDouble("montant_max"));
                p.setReglesFinancieres(rs.getString("regles_financieres"));
                produits.add(p);
            }

            cmbProduit.setItems(FXCollections.observableArrayList(produits));
            cmbProduit.setConverter(new StringConverter<ProduitFinancier>() {
                @Override
                public String toString(ProduitFinancier p) {
                    if (p == null) return "";
                    return p.getIdProduit() + " - " + p.getNomProduit();
                }

                @Override
                public ProduitFinancier fromString(String string) {
                    return null;
                }
            });

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setModeAjout() {
        modeModification = false;
        lblTitre.setText("NOUVELLE OFFRE");
        lblSousTitre.setText("Creez une nouvelle offre financiere");
        btnValider.setText("CREER L OFFRE");
    }

    public void setModeModification(OffreFinanciere offre) {
        modeModification = true;
        offreAModifier = offre;

        lblTitre.setText("MODIFIER L OFFRE");
        lblSousTitre.setText("Modifiez les informations de l offre");
        btnValider.setText("ENREGISTRER");

        // Remplir les champs
        txtNomOffre.setText(offre.getNomOffre());
        txtConditions.setText(offre.getConditions());
        cmbStatut.setValue(offre.getStatut());

        // Selectionner le produit
        for (ProduitFinancier p : cmbProduit.getItems()) {
            if (p.getIdProduit() == offre.getIdProduit()) {
                cmbProduit.setValue(p);
                break;
            }
        }
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    private void handleValider() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            if (modeModification) {
                // Mode modification
                offreAModifier.setNomOffre(txtNomOffre.getText().trim());
                offreAModifier.setConditions(txtConditions.getText().trim());
                offreAModifier.setStatut(cmbStatut.getValue());
                offreAModifier.setIdProduit(cmbProduit.getValue().getIdProduit());

                boolean success = offreController.modifierOffre(offreAModifier);

                if (success) {
                    showAlert("Succes", "Offre modifiee avec succes!", Alert.AlertType.INFORMATION);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    fermerFenetre();
                } else {
                    showAlert("Erreur", "Impossible de modifier l offre!", Alert.AlertType.ERROR);
                }
            } else {
                // Mode ajout
                OffreFinanciere nouvelleOffre = new OffreFinanciere();
                nouvelleOffre.setNomOffre(txtNomOffre.getText().trim());
                nouvelleOffre.setConditions(txtConditions.getText().trim());
                nouvelleOffre.setStatut(cmbStatut.getValue());
                nouvelleOffre.setIdProduit(cmbProduit.getValue().getIdProduit());

                boolean success = offreController.ajouterOffre(nouvelleOffre);

                if (success) {
                    showAlert("Succes", "Offre creee avec succes!", Alert.AlertType.INFORMATION);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    fermerFenetre();
                } else {
                    showAlert("Erreur", "Impossible de creer l offre!", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
    }

    private boolean validerFormulaire() {
        String erreurs = "";

        if (txtNomOffre.getText() == null || txtNomOffre.getText().trim().isEmpty()) {
            erreurs = erreurs + "Le nom de l offre est obligatoire\n";
        }
        if (cmbProduit.getValue() == null) {
            erreurs = erreurs + "Veuillez selectionner un produit\n";
        }
        if (cmbStatut.getValue() == null) {
            erreurs = erreurs + "Veuillez selectionner un statut\n";
        }

        if (!erreurs.isEmpty()) {
            showAlert("Validation", erreurs, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void fermerFenetre() {
        Stage stage = (Stage) txtNomOffre.getScene().getWindow();
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