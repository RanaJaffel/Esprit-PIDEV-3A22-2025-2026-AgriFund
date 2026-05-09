package com.agrifund.controller.banque;

import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.OffreFinanciereService;
import com.agrifund.services.ProduitFinancierService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

public class BanqueFormulaireOffreController {

    @FXML private HBox titleBar;
    @FXML private Label lblWindowTitle;
    @FXML private Button btnClose;
    @FXML private ImageView imgLogo;

    @FXML private Label lblTitre;
    @FXML private Label lblSousTitre;
    @FXML private TextField txtNomOffre;
    @FXML private ComboBox<ProduitFinancier> cmbProduit;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private TextArea txtConditions;
    @FXML private Button btnValider;

    private OffreFinanciereService offreService;
    private ProduitFinancierService produitService;
    private OffreFinanciere offreAModifier;
    private boolean modeModification = false;
    private int banqueId;
    private Runnable onSuccess;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        offreService = new OffreFinanciereService();
        produitService = new ProduitFinancierService();

        cmbStatut.setItems(FXCollections.observableArrayList("Active", "En pause", "Expiree"));

        try {
            if (imgLogo != null) {
                Image logo = new Image(getClass().getResourceAsStream("/com/agrifund/images/image_2026-01-25_212530957-removebg-preview.png"));
                imgLogo.setImage(logo);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Logo non chargé: " + e.getMessage());
        }
    }

    private void chargerProduits(int banqueId) {
        try {
            // Charger uniquement les produits de cette banque
            List<ProduitFinancier> produits = produitService.getProduitsByBanqueId(banqueId);

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

        } catch (Exception e) {
            e.printStackTrace();
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

    @FXML private void handleMinimize() {
        if (titleBar != null && titleBar.getScene() != null) {
            ((Stage) titleBar.getScene().getWindow()).setIconified(true);
        }
    }

    @FXML private void handleMaximize() {
        if (titleBar != null && titleBar.getScene() != null) {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        }
    }

    @FXML private void handleFermer() { fermerFenetre(); }

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

        chargerProduits(banqueId);

        if (lblWindowTitle != null) lblWindowTitle.setText("Nouvelle Offre");
        if (lblTitre != null) lblTitre.setText("NOUVELLE OFFRE");
        if (lblSousTitre != null) lblSousTitre.setText("Créez une nouvelle offre financière");
        if (btnValider != null) btnValider.setText("CRÉER L'OFFRE");
    }

    public void setModeModification(OffreFinanciere offre, int banqueId) {
        this.banqueId = banqueId;
        modeModification = true;
        offreAModifier = offre;

        chargerProduits(banqueId);

        if (lblWindowTitle != null) lblWindowTitle.setText("Modifier Offre - " + offre.getNomOffre());
        if (lblTitre != null) lblTitre.setText("MODIFIER L'OFFRE");
        if (lblSousTitre != null) lblSousTitre.setText("Modifiez les informations de l'offre");
        if (btnValider != null) btnValider.setText("ENREGISTRER");

        if (txtNomOffre != null) txtNomOffre.setText(offre.getNomOffre());
        if (txtConditions != null) txtConditions.setText(offre.getConditions());
        if (cmbStatut != null) cmbStatut.setValue(offre.getStatut());

        if (cmbProduit != null) {
            for (ProduitFinancier p : cmbProduit.getItems()) {
                if (p.getIdProduit() == offre.getIdProduit()) {
                    cmbProduit.setValue(p);
                    break;
                }
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
                offreAModifier.setNomOffre(txtNomOffre.getText().trim());
                offreAModifier.setConditions(txtConditions.getText().trim());
                offreAModifier.setStatut(cmbStatut.getValue());
                offreAModifier.setIdProduit(cmbProduit.getValue().getIdProduit());
                offreAModifier.setBanqueId(banqueId);

                boolean success = offreService.modifierOffre(offreAModifier);

                if (success) {
                    showAlert("Succès", "Offre modifiée avec succès!", Alert.AlertType.INFORMATION);
                    if (onSuccess != null) onSuccess.run();
                    fermerFenetre();
                } else {
                    showAlert("Erreur", "Impossible de modifier l'offre!", Alert.AlertType.ERROR);
                }
            } else {
                OffreFinanciere nouvelleOffre = new OffreFinanciere();
                nouvelleOffre.setNomOffre(txtNomOffre.getText().trim());
                nouvelleOffre.setConditions(txtConditions.getText().trim());
                nouvelleOffre.setStatut(cmbStatut.getValue());
                nouvelleOffre.setIdProduit(cmbProduit.getValue().getIdProduit());
                nouvelleOffre.setBanqueId(banqueId);

                boolean success = offreService.ajouterOffre(nouvelleOffre);

                if (success) {
                    showAlert("Succès", "Offre créée avec succès!", Alert.AlertType.INFORMATION);
                    if (onSuccess != null) onSuccess.run();
                    fermerFenetre();
                } else {
                    showAlert("Erreur", "Impossible de créer l'offre!", Alert.AlertType.ERROR);
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

        if (txtNomOffre == null || txtNomOffre.getText() == null || txtNomOffre.getText().trim().isEmpty()) {
            erreurs += "Le nom de l'offre est obligatoire\n";
        }
        if (cmbProduit == null || cmbProduit.getValue() == null) {
            erreurs += "Veuillez sélectionner un produit\n";
        }
        if (cmbStatut == null || cmbStatut.getValue() == null) {
            erreurs += "Veuillez sélectionner un statut\n";
        }

        if (!erreurs.isEmpty()) {
            showAlert("Validation", erreurs, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void fermerFenetre() {
        try {
            Stage stage = null;
            if (titleBar != null && titleBar.getScene() != null) {
                stage = (Stage) titleBar.getScene().getWindow();
            } else if (txtNomOffre != null && txtNomOffre.getScene() != null) {
                stage = (Stage) txtNomOffre.getScene().getWindow();
            }
            if (stage != null) stage.close();
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