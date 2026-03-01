package com.agrifund.controller.admin;

import com.agrifund.model.OffreFinanciere;
import com.agrifund.services.OffreFinanciereService;
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

public class AdminOffreController {

    @FXML private TextField txtRecherche;

    @FXML private TableView<OffreFinanciere> tableOffres;
    @FXML private TableColumn<OffreFinanciere, Integer> colId;
    @FXML private TableColumn<OffreFinanciere, String> colNom;
    @FXML private TableColumn<OffreFinanciere, String> colProduit;
    @FXML private TableColumn<OffreFinanciere, String> colConditions;
    @FXML private TableColumn<OffreFinanciere, String> colStatut;
    @FXML private TableColumn<OffreFinanciere, String> colBanque;

    @FXML private Label lblTotalOffres;
    @FXML private Label lblActives;
    @FXML private Label lblEnPause;
    @FXML private Label lblExpirees;
    @FXML private Label lblCount;

    private OffreFinanciereService offreService;
    private BanqueService banqueService;
    private ObservableList<OffreFinanciere> offresList;

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation AdminOffreController...");

        try {
            offreService = new OffreFinanciereService();
            banqueService = new BanqueService();
            offresList = FXCollections.observableArrayList();

            // Configuration des colonnes
            colId.setCellValueFactory(new PropertyValueFactory<>("idOffre"));
            colNom.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
            colProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
            colConditions.setCellValueFactory(new PropertyValueFactory<>("conditions"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
            colBanque.setCellValueFactory(new PropertyValueFactory<>("nomBanque"));

            // Style pour la colonne statut avec couleurs AgriFund
            colStatut.setCellFactory(column -> new TableCell<OffreFinanciere, String>() {
                @Override
                protected void updateItem(String statut, boolean empty) {
                    super.updateItem(statut, empty);
                    if (empty || statut == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(statut);
                        switch (statut) {
                            case "Active":
                                setStyle("-fx-text-fill: #089647; -fx-font-weight: bold; " +
                                        "-fx-background-color: rgba(8, 150, 71, 0.1); -fx-background-radius: 5;");
                                break;
                            case "En pause":
                                setStyle("-fx-text-fill: #E1B323; -fx-font-weight: bold; " +
                                        "-fx-background-color: rgba(225, 179, 35, 0.1); -fx-background-radius: 5;");
                                break;
                            case "Expiree":
                                setStyle("-fx-text-fill: #848A86; -fx-font-weight: bold; " +
                                        "-fx-background-color: rgba(132, 138, 134, 0.1); -fx-background-radius: 5;");
                                break;
                            default:
                                setStyle("-fx-font-weight: bold;");
                        }
                    }
                }
            });

            // Style pour la colonne banque
            colBanque.setCellFactory(column -> new TableCell<OffreFinanciere, String>() {
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

            // Style pour la colonne produit
            colProduit.setCellFactory(column -> new TableCell<OffreFinanciere, String>() {
                @Override
                protected void updateItem(String produit, boolean empty) {
                    super.updateItem(produit, empty);
                    if (empty || produit == null || produit.isEmpty()) {
                        setText("N/A");
                        setStyle("-fx-text-fill: #848A86;");
                    } else {
                        setText("📦 " + produit);
                        setStyle("-fx-text-fill: #133D03;");
                    }
                }
            });

            chargerDonnees();

            System.out.println("✅ Initialisation AdminOffreController terminée!");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            initializeEmptyState();
        }
    }

    private void initializeEmptyState() {
        lblTotalOffres.setText("0");
        lblActives.setText("0");
        lblEnPause.setText("0");
        lblExpirees.setText("0");
        lblCount.setText("0 offres");
        tableOffres.setItems(FXCollections.observableArrayList());
    }

    private void chargerDonnees() {
        try {
            offresList = offreService.getAllOffresAvecProduit();
            tableOffres.setItems(offresList);
            mettreAJourStats();
            lblCount.setText(offresList.size() + " offres");

            System.out.println("✅ " + offresList.size() + " offres chargées (Admin)");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
            initializeEmptyState();
        }
    }

    private void mettreAJourStats() {
        int total = offresList.size();
        int actives = 0;
        int enPause = 0;
        int expirees = 0;

        for (OffreFinanciere o : offresList) {
            String statut = o.getStatut();
            if (statut != null) {
                switch (statut) {
                    case "Active":
                        actives++;
                        break;
                    case "En pause":
                        enPause++;
                        break;
                    case "Expiree":
                        expirees++;
                        break;
                }
            }
        }

        lblTotalOffres.setText(String.valueOf(total));
        lblActives.setText(String.valueOf(actives));
        lblEnPause.setText(String.valueOf(enPause));
        lblExpirees.setText(String.valueOf(expirees));
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtRecherche.getText().trim();
        if (keyword.isEmpty()) {
            chargerDonnees();
            return;
        }

        ObservableList<OffreFinanciere> resultats = offreService.rechercherOffres(keyword);
        offresList.clear();
        offresList.addAll(resultats);
        tableOffres.setItems(offresList);
        mettreAJourStats();
        lblCount.setText(offresList.size() + " résultats");
    }

    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerDonnees();
    }

    @FXML
    private void handleVoirDetails() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une offre", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/admin/AdminDetailsOffreView.fxml"));
            Parent root = loader.load();

            AdminDetailsOffreController controller = loader.getController();
            controller.setOffre(selected);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 700, 800));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showDetailsInAlert(selected);
        }
    }

    private void showDetailsInAlert(OffreFinanciere offre) {
        String statusIcon = "";
        switch (offre.getStatut()) {
            case "Active": statusIcon = "✅"; break;
            case "En pause": statusIcon = "⏸️"; break;
            case "Expiree": statusIcon = "⏱️"; break;
            default: statusIcon = "📋";
        }

        String details = "═══════════════════════════════════\n" +
                "       DÉTAILS DE L'OFFRE\n" +
                "═══════════════════════════════════\n\n" +
                "🆔 ID: #" + offre.getIdOffre() + "\n\n" +
                "🎁 Nom: " + offre.getNomOffre() + "\n\n" +
                "📦 Produit: " + (offre.getNomProduit() != null ? offre.getNomProduit() : "N/A") + "\n\n" +
                statusIcon + " Statut: " + offre.getStatut() + "\n\n" +
                "🏦 Banque: " + (offre.getNomBanque() != null ? offre.getNomBanque() : "Non assignée") + "\n\n" +
                "═══════════════════════════════════\n" +
                "       CONDITIONS\n" +
                "═══════════════════════════════════\n\n" +
                (offre.getConditions() != null ? offre.getConditions() : "Aucune condition spécifiée") + "\n\n" +
                "═══════════════════════════════════";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails - " + offre.getNomOffre());
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(18);
        textArea.setPrefColumnCount(45);
        textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13;");

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleVoirBanque() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une offre", Alert.AlertType.WARNING);
            return;
        }

        if (selected.getBanqueId() == 0) {
            showAlert("Cette offre n'est associée à aucune banque", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            Banque banque = null;
            for (Banque b : banqueService.afficherTous()) {
                if (b.getBanqueId() == selected.getBanqueId()) {
                    banque = b;
                    break;
                }
            }

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
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une offre", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = PDFGenerator.genererPDFOffre(selected);
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