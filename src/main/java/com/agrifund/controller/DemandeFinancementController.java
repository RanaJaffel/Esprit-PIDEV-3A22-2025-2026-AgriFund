package com.agrifund.controller;

import com.agrifund.model.ProduitFinancier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DemandeFinancementController {
    
    // Product information display
    @FXML private VBox vboxProduitInfo;
    @FXML private Label lblNomProduit;
    @FXML private Label lblTypeProduit;
    @FXML private Label lblTauxProduit;
    @FXML private Label lblMontantProduit;
    
    // External system configuration
    @FXML private TextField txtUrlSysteme;
    @FXML private ComboBox<String> cmbMethode;
    
    @FXML private Button btnRediriger;
    @FXML private Label lblStatut;
    
    // Store selected product
    private ProduitFinancier produitSelectionne;

    @FXML
    public void initialize() {
        // Set default values
        if (txtUrlSysteme != null) {
            txtUrlSysteme.setText("http://localhost:8080/demande");
        }
        if (cmbMethode != null) {
            cmbMethode.setItems(FXCollections.observableArrayList("POST", "GET", "Redirect"));
            cmbMethode.setValue("Redirect");
        }
        
        // If no product was set, hide the product info box
        if (produitSelectionne == null && vboxProduitInfo != null) {
            vboxProduitInfo.setVisible(false);
        }
    }
    
    /**
     * Set the selected product to display its information
     */
    public void setProduitSelectionne(ProduitFinancier produit) {
        this.produitSelectionne = produit;
        if (produit != null) {
            if (lblNomProduit != null) lblNomProduit.setText("Nom: " + produit.getNomProduit());
            if (lblTypeProduit != null) lblTypeProduit.setText("Type: " + produit.getTypeFinancement());
            if (lblTauxProduit != null) lblTauxProduit.setText("Taux: " + produit.getTauxInteret() + "%");
            if (lblMontantProduit != null) lblMontantProduit.setText("Montant: " + produit.getMontantMin() + " - " + produit.getMontantMax() + " MAD");
            if (vboxProduitInfo != null) vboxProduitInfo.setVisible(true);
        }
    }
    
    /**
     * Handle redirection to external system
     */
    @FXML
    private void handleRedirection() {
        String url = txtUrlSysteme.getText().trim();
        
        if (url.isEmpty()) {
            showError("Configuration manquante", "Veuillez configurer l'URL du système externe");
            return;
        }
        
        btnRediriger.setDisable(true);
        lblStatut.setText("⏳ Redirection...");
        lblStatut.setStyle("-fx-text-fill: #076A39;");
        
        try {
            // Build URL with product parameters
            String fullUrl = construireUrlAvecParametres(url);
            
            // Open in default browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(fullUrl));
                lblStatut.setText("✓ Redirection effectuée!");
                lblStatut.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else {
                // Fallback: show URL to copy
                afficherUrlPourCopie(fullUrl);
            }
            
        } catch (Exception e) {
            showError("Erreur de redirection", e.getMessage());
            lblStatut.setText("✖ Erreur");
            lblStatut.setStyle("-fx-text-fill: #f44336;");
            btnRediriger.setDisable(false);
        }
    }
    
    /**
     * Build URL with product parameters
     */
    private String construireUrlAvecParametres(String baseUrl) {
        StringBuilder url = new StringBuilder(baseUrl);
        
        if (produitSelectionne != null) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            url.append(separator).append("produit_id=").append(produitSelectionne.getIdProduit());
            url.append("&produit_nom=").append(URLEncoder.encode(produitSelectionne.getNomProduit(), StandardCharsets.UTF_8));
            url.append("&produit_type=").append(URLEncoder.encode(produitSelectionne.getTypeFinancement(), StandardCharsets.UTF_8));
            url.append("&taux=").append(produitSelectionne.getTauxInteret());
            url.append("&montant_min=").append(produitSelectionne.getMontantMin());
            url.append("&montant_max=").append(produitSelectionne.getMontantMax());
        }
        
        return url.toString();
    }
    
    /**
     * Show URL for manual copy in case browser opening fails
     */
    private void afficherUrlPourCopie(String urlStr) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("URL de Redirection");
        alert.setHeaderText("Copiez cette URL dans votre navigateur");
        
        TextArea textArea = new TextArea(urlStr);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(3);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
        
        lblStatut.setText("📋 URL affichée");
        lblStatut.setStyle("-fx-text-fill: #4CAF50;");
    }
    
    /**
     * Return to previous view
     */
    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/UtilisateurView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnRediriger.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AgriFund - Mon Espace");
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner: " + e.getMessage());
        }
    }
    
    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}