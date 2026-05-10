package com.agrifund.controller.banque;

import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.BanqueService;
import com.agrifund.services.OffreFinanciereService;
import com.agrifund.services.ProduitFinancierService;
import com.agrifund.util.SessionManager;
import com.agrifund.util.PDFGenerator;
import com.agrifund.entities.Banque;
import com.agrifund.entities.Utilisateur;

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
import java.util.Optional;

public class BanqueOffreController {

    @FXML private TextField txtRecherche;

    @FXML private TableView<OffreFinanciere> tableOffres;
    @FXML private TableColumn<OffreFinanciere, Integer> colId;
    @FXML private TableColumn<OffreFinanciere, String> colNom;
    @FXML private TableColumn<OffreFinanciere, String> colProduit;
    @FXML private TableColumn<OffreFinanciere, String> colConditions;
    @FXML private TableColumn<OffreFinanciere, String> colStatut;

    @FXML private Label lblTotalOffres;
    @FXML private Label lblActives;
    @FXML private Label lblEnPause;
    @FXML private Label lblExpirees;
    @FXML private Label lblBanqueName;

    private OffreFinanciereService offreService;
    private ProduitFinancierService produitService;
    private BanqueService banqueService;
    private ObservableList<OffreFinanciere> offresList;

    private Utilisateur currentUser;
    private Banque currentBanque;

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation BanqueOffreController...");

        try {
            offreService = new OffreFinanciereService();
            produitService = new ProduitFinancierService();
            banqueService = new BanqueService();
            offresList = FXCollections.observableArrayList();

            // Récupérer la banque connectée
            currentUser = SessionManager.getInstance().getUtilisateurConnecte();
            currentBanque = banqueService.rechercherParUtilisateurId(currentUser.getId());

            if (currentBanque == null) {
                showAlert("Erreur", "Impossible de récupérer les informations de la banque", Alert.AlertType.ERROR);
                return;
            }

            lblBanqueName.setText("Offres de: " + currentUser.getNom());

            // Configuration des colonnes
            colId.setCellValueFactory(new PropertyValueFactory<>("idOffre"));
            colNom.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
            colProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
            colConditions.setCellValueFactory(new PropertyValueFactory<>("conditions"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

            // Style pour la colonne statut
            colStatut.setCellFactory(column -> new TableCell<OffreFinanciere, String>() {
                @Override
                protected void updateItem(String statut, boolean empty) {
                    super.updateItem(statut, empty);
                    if (empty || statut == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(statut);
                        if (statut.equals("Active")) {
                            setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        } else if (statut.equals("En pause")) {
                            setStyle("-fx-text-fill: #F9A825; -fx-font-weight: bold;");
                        } else if (statut.equals("Expiree")) {
                            setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-font-weight: bold;");
                        }
                    }
                }
            });

            chargerDonnees();

            System.out.println("✅ Initialisation terminée!");

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
        tableOffres.setItems(FXCollections.observableArrayList());
    }

    private void chargerDonnees() {
        try {
            // Charger uniquement les offres de cette banque
            offresList = offreService.getOffresByBanqueId(currentBanque.getBanqueId());
            tableOffres.setItems(offresList);
            mettreAJourStats();

            System.out.println("✅ " + offresList.size() + " offres chargées");

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
                if (statut.equals("Active")) actives++;
                else if (statut.equals("En pause")) enPause++;
                else if (statut.equals("Expiree")) expirees++;
            }
        }

        lblTotalOffres.setText(String.valueOf(total));
        lblActives.setText(String.valueOf(actives));
        lblEnPause.setText(String.valueOf(enPause));
        lblExpirees.setText(String.valueOf(expirees));
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/banque/BanqueFormulaireOffreView.fxml"));
            Parent root = loader.load();

            BanqueFormulaireOffreController controller = loader.getController();
            controller.setModeAjout(currentBanque.getBanqueId());
            controller.setOnSuccess(() -> chargerDonnees());

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 580, 680));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleModifier() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une offre à modifier!", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/banque/BanqueFormulaireOffreView.fxml"));
            Parent root = loader.load();

            BanqueFormulaireOffreController controller = loader.getController();
            controller.setModeModification(selected, currentBanque.getBanqueId());
            controller.setOnSuccess(() -> chargerDonnees());

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 580, 680));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSupprimer() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une offre à supprimer!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'offre?");
        confirm.setContentText("Voulez-vous vraiment supprimer: " + selected.getNomOffre() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = offreService.supprimerOffre(selected.getIdOffre());

            if (success) {
                chargerDonnees();
                showAlert("Succès", "Offre supprimée avec succès!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Impossible de supprimer l'offre!", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtRecherche.getText().trim();
        if (keyword.isEmpty()) {
            chargerDonnees();
            return;
        }

        ObservableList<OffreFinanciere> resultats = offreService.rechercherOffresByBanque(keyword, currentBanque.getBanqueId());
        offresList.clear();
        offresList.addAll(resultats);
        tableOffres.setItems(offresList);
    }

    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerDonnees();
    }

    @FXML
    private void handleDownloadPDF() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une offre!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = PDFGenerator.genererPDFOffre(selected);
            showAlert("Succès", "PDF téléchargé avec succès!\n\nFichier: " + filePath, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le PDF:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleVoirDetails() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner une offre!", Alert.AlertType.WARNING);
            return;
        }

        String details = "Nom: " + selected.getNomOffre() + "\n\n" +
                "ID: #" + selected.getIdOffre() + "\n\n" +
                "Produit: " + selected.getNomProduit() + "\n\n" +
                "Statut: " + selected.getStatut() + "\n\n" +
                "Conditions: " +
                (selected.getConditions() != null ? selected.getConditions() : "Aucune");

        showAlert("Détails - " + selected.getNomOffre(), details, Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleRetour() {
        com.agrifund.Main.navigateTo("/com/agrifund/fxml/banque/banque-dashboard.fxml");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
