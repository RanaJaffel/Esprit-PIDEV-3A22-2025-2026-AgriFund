package com.agrifund.controller.admin;

import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.ProduitFinancierService;
import com.agrifund.services.BanqueService;
import com.agrifund.entities.Banque;
import com.agrifund.util.PDFGenerator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;

public class AdminProduitController {

    @FXML private TextField txtRecherche;

    @FXML private TableView<ProduitFinancier> tableView;
    @FXML private TableColumn<ProduitFinancier, Integer> colId;
    @FXML private TableColumn<ProduitFinancier, String> colNom;
    @FXML private TableColumn<ProduitFinancier, String> colType;
    @FXML private TableColumn<ProduitFinancier, Double> colTaux;
    @FXML private TableColumn<ProduitFinancier, Double> colMontantMin;
    @FXML private TableColumn<ProduitFinancier, Double> colMontantMax;
    @FXML private TableColumn<ProduitFinancier, String> colBanque; // NOUVELLE COLONNE

    @FXML private Label lblTotalProduits;
    @FXML private Label lblCount;

    private ProduitFinancierService produitService;
    private BanqueService banqueService;
    private ObservableList<ProduitFinancier> produitsList;

    @FXML
    public void initialize() {
        try {
            produitService = new ProduitFinancierService();
            banqueService = new BanqueService();
            produitsList = FXCollections.observableArrayList();

            // Configuration des colonnes
            colId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
            colNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
            colType.setCellValueFactory(new PropertyValueFactory<>("typeFinancement"));
            colTaux.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
            colMontantMin.setCellValueFactory(new PropertyValueFactory<>("montantMin"));
            colMontantMax.setCellValueFactory(new PropertyValueFactory<>("montantMax"));
            colBanque.setCellValueFactory(new PropertyValueFactory<>("nomBanque"));

            chargerDonnees();

        } catch (SQLException e) {
            showAlert("Erreur d'initialisation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void chargerDonnees() {
        // Charger TOUS les produits (admin voit tout)
        produitsList = produitService.getAllProduits();
        tableView.setItems(produitsList);
        lblTotalProduits.setText(String.valueOf(produitsList.size()));
        lblCount.setText(produitsList.size() + " produits");
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtRecherche.getText().trim();
        if (keyword.isEmpty()) {
            chargerDonnees();
            return;
        }

        ObservableList<ProduitFinancier> resultats = produitService.rechercherProduits(keyword);
        produitsList.clear();
        produitsList.addAll(resultats);
        tableView.setItems(produitsList);
        lblCount.setText(produitsList.size() + " résultats");
    }

    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerDonnees();
    }

    @FXML
    private void handleVoirDetails() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un produit", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/admin/AdminDetailsProduitView.fxml"));
            Parent root = loader.load();

            AdminDetailsProduitController controller = loader.getController();
            controller.setProduit(selected);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 700, 850));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleVoirBanque() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un produit", Alert.AlertType.WARNING);
            return;
        }

        if (selected.getBanqueId() == 0) {
            showAlert("Ce produit n'est associé à aucune banque", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            Banque banque = banqueService.rechercherParUtilisateurId(selected.getBanqueId());
            if (banque != null) {
                String details = "=== BANQUE ===\n\n" +
                        "Nom: " + banque.getNom() + "\n" +
                        "Code: " + banque.getCodeBanque() + "\n" +
                        "Représentant: " + banque.getRepresentantLegal() + "\n" +
                        "Siège: " + banque.getAddresseSiege() + "\n" +
                        "Site Web: " + (banque.getSiteWeb() != null ? banque.getSiteWeb() : "N/A") + "\n" +
                        "Statut: " + banque.getStatusCompte() + "\n" +
                        "Vérifié: " + (banque.isCompteVerifie() ? "Oui" : "Non");

                showAlert(details, Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDownloadPDF() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner un produit", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = PDFGenerator.genererPDFProduit(selected);
            showAlert("PDF généré: " + filePath, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRetour() {
        com.agrifund.Main.navigateTo("/com/agrifund/fxml/admin/admin-dashboard.fxml");
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}