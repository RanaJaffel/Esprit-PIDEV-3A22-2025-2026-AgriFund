package com.agrifund.view;

import com.agrifund.controller.OffreFinanciereController;
import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.AgrifundDBConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FormulaireOffreController {

    // Title Bar elements
    @FXML private HBox titleBar;
    @FXML private Label lblWindowTitle;
    @FXML private Button btnClose;
    @FXML private ImageView imgLogo;

    // Form elements
    @FXML private Label lblTitre;
    @FXML private Label lblSousTitre;
    @FXML private TextField txtNomOffre;
    @FXML private ComboBox<ProduitFinancier> cmbProduit;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private TextArea txtConditions;
    @FXML private Button btnValider;

    private OffreFinanciereController offreController;
    private OffreFinanciere offreAModifier;
    private boolean modeModification = false;
    private Runnable onSuccess;

    // For dragging the window
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        offreController = new OffreFinanciereController();

        // Charger les statuts
        cmbStatut.setItems(FXCollections.observableArrayList("Active", "En pause", "Expiree"));

        // Charger les produits
        chargerProduits();

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

    private void chargerProduits() {
        try {
            Connection conn = AgrifundDBConnection.getConnection();
            if (conn == null) return;

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
                p.setPrixFixe(rs.getDouble("prix_fixe"));
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

        } catch (Exception e) {
            e.printStackTrace();
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
    private void handleFermer() {
        fermerFenetre();
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

    // ==================== MODE CONFIGURATION ====================

    public void setModeAjout() {
        modeModification = false;

        if (lblWindowTitle != null) lblWindowTitle.setText("Nouvelle Offre");
        if (lblTitre != null) lblTitre.setText("NOUVELLE OFFRE");
        if (lblSousTitre != null) lblSousTitre.setText("Creez une nouvelle offre financiere");
        if (btnValider != null) btnValider.setText("CREER L'OFFRE");
    }

    public void setModeModification(OffreFinanciere offre) {
        modeModification = true;
        offreAModifier = offre;

        if (lblWindowTitle != null) lblWindowTitle.setText("Modifier Offre - " + offre.getNomOffre());
        if (lblTitre != null) lblTitre.setText("MODIFIER L'OFFRE");
        if (lblSousTitre != null) lblSousTitre.setText("Modifiez les informations de l'offre");
        if (btnValider != null) btnValider.setText("ENREGISTRER");

        // Remplir les champs
        if (txtNomOffre != null) txtNomOffre.setText(offre.getNomOffre());
        if (txtConditions != null) txtConditions.setText(offre.getConditions());
        if (cmbStatut != null) cmbStatut.setValue(offre.getStatut());

        // Selectionner le produit
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

    // ==================== FORM ACTIONS ====================

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
                    showAlert("Erreur", "Impossible de modifier l'offre!", Alert.AlertType.ERROR);
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
                    showAlert("Erreur", "Impossible de creer l'offre!", Alert.AlertType.ERROR);
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
            erreurs += "Veuillez selectionner un produit\n";
        }
        if (cmbStatut == null || cmbStatut.getValue() == null) {
            erreurs += "Veuillez selectionner un statut\n";
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

            if (stage != null) {
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
