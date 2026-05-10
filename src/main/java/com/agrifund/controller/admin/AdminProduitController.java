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
    @FXML private TableColumn<ProduitFinancier, Double> colPrixFixe;
    @FXML private TableColumn<ProduitFinancier, String> colBanque;

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
            colPrixFixe.setCellValueFactory(new PropertyValueFactory<>("prixFixe"));
            colBanque.setCellValueFactory(new PropertyValueFactory<>("nomBanque"));

            // Style colonne Type
            colType.setCellFactory(column -> new TableCell<ProduitFinancier, String>() {
                @Override
                protected void updateItem(String type, boolean empty) {
                    super.updateItem(type, empty);
                    if (empty || type == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(type);
                        setStyle("-fx-text-fill: #089647; -fx-font-weight: bold;");
                    }
                }
            });

            // Style colonne Taux
            colTaux.setCellFactory(column -> new TableCell<ProduitFinancier, Double>() {
                @Override
                protected void updateItem(Double taux, boolean empty) {
                    super.updateItem(taux, empty);
                    if (empty || taux == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.format("%.2f%%", taux));
                        setStyle("-fx-text-fill: #E1B323; -fx-font-weight: bold;");
                    }
                }
            });

            // Style colonne Banque
            colBanque.setCellFactory(column -> new TableCell<ProduitFinancier, String>() {
                @Override
                protected void updateItem(String banque, boolean empty) {
                    super.updateItem(banque, empty);
                    if (empty || banque == null || banque.isEmpty()) {
                        setText("Non assignée");
                        setStyle("-fx-text-fill: #848A86; -fx-font-style: italic;");
                    } else {
                        setText("🏦 " + banque);
                        setStyle("-fx-text-fill: #476C1A; -fx-font-weight: bold;");
                    }
                }
            });

            // Style colonnes montants
            colPrixFixe.setCellFactory(column -> new TableCell<ProduitFinancier, Double>() {
                @Override
                protected void updateItem(Double montant, boolean empty) {
                    super.updateItem(montant, empty);
                    if (empty || montant == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f DT", montant));
                        setStyle("-fx-text-fill: #133D03;");
                    }
                }
            });

            colPrixFixe.setCellFactory(column -> new TableCell<ProduitFinancier, Double>() {
                @Override
                protected void updateItem(Double montant, boolean empty) {
                    super.updateItem(montant, empty);
                    if (empty || montant == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f DT", montant));
                        setStyle("-fx-text-fill: #133D03; -fx-font-weight: bold;");
                    }
                }
            });

            chargerDonnees();

        } catch (SQLException e) {
            showAlert("Erreur d'initialisation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void chargerDonnees() {
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
            // Fallback: afficher dans une alerte stylisée
            showDetailsInAlert(selected);
        }
    }

    private void showDetailsInAlert(ProduitFinancier produit) {
        String details = "═══════════════════════════════════\n" +
                "       DÉTAILS DU PRODUIT\n" +
                "═══════════════════════════════════\n\n" +
                "🆔 ID: #" + produit.getIdProduit() + "\n\n" +
                "📦 Nom: " + produit.getNomProduit() + "\n\n" +
                "📋 Type: " + produit.getTypeFinancement() + "\n\n" +
                "💰 Taux d'intérêt: " + String.format("%.2f%%", produit.getTauxInteret()) + "\n\n" +
                "📊 Prix Fixe: " + String.format("%,.0f DT", produit.getPrixFixe()) + "\n\n" +
                "🏦 Banque: " + (produit.getNomBanque() != null ? produit.getNomBanque() : "Non assignée") + "\n\n" +
                "═══════════════════════════════════";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails - " + produit.getNomProduit());
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        textArea.setPrefColumnCount(45);
        textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13;");

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
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
                String details = "═══════════════════════════════════\n" +
                        "       🏦 BANQUE PROPRIÉTAIRE\n" +
                        "═══════════════════════════════════\n\n" +
                        "🏛️ Nom: " + banque.getNom() + "\n\n" +
                        "📋 Code: " + banque.getCodeBanque() + "\n\n" +
                        "👤 Représentant: " + banque.getRepresentantLegal() + "\n\n" +
                        "📍 Siège: " + banque.getAddresseSiege() + "\n\n" +
                        "📧 Email: " + banque.getEmail() + "\n\n" +
                        "📱 Tél: " + (banque.getTel() != null ? banque.getTel() : "N/A") + "\n\n" +
                        "🌐 Site Web: " + (banque.getSiteWeb() != null ? banque.getSiteWeb() : "N/A") + "\n\n" +
                        "📊 Statut: " + banque.getStatusCompte() + "\n\n" +
                        "✓ Vérifié: " + (banque.isCompteVerifie() ? "Oui ✅" : "Non ⏳") + "\n\n" +
                        "═══════════════════════════════════";

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Banque - " + banque.getNom());
                alert.setHeaderText(null);

                TextArea textArea = new TextArea(details);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefRowCount(14);
                textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13;");

                alert.getDialogPane().setContent(textArea);
                alert.showAndWait();
            } else {
                showAlert("Banque introuvable", Alert.AlertType.WARNING);
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
            showAlert("✅ PDF généré avec succès!\n\n📁 Fichier: " + filePath, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("❌ Erreur PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
