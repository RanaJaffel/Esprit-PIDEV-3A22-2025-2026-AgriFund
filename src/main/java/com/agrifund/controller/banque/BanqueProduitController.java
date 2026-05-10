package com.agrifund.controller.banque;

import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.BanqueService;
import com.agrifund.services.ProduitFinancierService;
import com.agrifund.util.SessionManager;
import com.agrifund.util.PDFGenerator;
import com.agrifund.entities.Banque;
import com.agrifund.entities.Utilisateur;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.Optional;

public class BanqueProduitController {

    @FXML private TextField txtRecherche;
    @FXML private TableView<ProduitFinancier> tableView;
    @FXML private TableColumn<ProduitFinancier, Integer> colId;
    @FXML private TableColumn<ProduitFinancier, String> colNom;
    @FXML private TableColumn<ProduitFinancier, String> colType;
    @FXML private TableColumn<ProduitFinancier, Double> colTaux;
    @FXML private TableColumn<ProduitFinancier, Double> colPrixFixe;

    @FXML private Label lblTotalProduits;
    @FXML private Label lblPrets;
    @FXML private Label lblCredits;
    @FXML private Label lblAutres;
    @FXML private Label lblCount;
    @FXML private Label lblStatus;
    @FXML private Label lblBanqueName;

    private ProduitFinancierService produitService;
    private BanqueService banqueService;
    private ObservableList<ProduitFinancier> produitsList;

    private Utilisateur currentUser;
    private Banque currentBanque;

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation BanqueProduitController...");

        try {
            produitService = new ProduitFinancierService();
            banqueService = new BanqueService();
            produitsList = FXCollections.observableArrayList();

            // Récupérer la banque connectée
            currentUser = SessionManager.getInstance().getUtilisateurConnecte();
            currentBanque = banqueService.rechercherParUtilisateurId(currentUser.getId());

            if (currentBanque == null) {
                showAlert("Erreur", "Impossible de récupérer les informations de la banque", Alert.AlertType.ERROR);
                return;
            }

            lblBanqueName.setText("Produits de: " + currentUser.getNom());

            // Configuration des colonnes
            colId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
            colNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
            colType.setCellValueFactory(new PropertyValueFactory<>("typeFinancement"));
            colTaux.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
            colPrixFixe.setCellValueFactory(new PropertyValueFactory<>("prixFixe"));

            chargerDonnees();

            System.out.println("✅ Initialisation terminée!");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            initializeEmptyState();
        }
    }

    private void initializeEmptyState() {
        lblTotalProduits.setText("0");
        lblPrets.setText("0");
        lblCredits.setText("0");
        lblAutres.setText("0");
        lblCount.setText("0 produits");
        lblStatus.setText("Non connecté");
        tableView.setItems(FXCollections.observableArrayList());
    }

    private void chargerDonnees() {
        try {
            // Charger uniquement les produits de cette banque
            produitsList = produitService.getProduitsByBanqueId(currentBanque.getBanqueId());
            tableView.setItems(produitsList);
            mettreAJourStats();
            lblCount.setText(produitsList.size() + " produits");
            lblStatus.setText("Connecté");

        } catch (Exception e) {
            showAlert("Erreur", "Erreur chargement: " + e.getMessage(), Alert.AlertType.ERROR);
            initializeEmptyState();
        }
    }

    private void mettreAJourStats() {
        int total = produitsList.size();
        int prets = 0;
        int credits = 0;
        int autres = 0;

        for (ProduitFinancier p : produitsList) {
            String type = p.getTypeFinancement();
            if (type != null) {
                if (type.toLowerCase().contains("pret")) {
                    prets++;
                } else if (type.toLowerCase().contains("credit")) {
                    credits++;
                } else {
                    autres++;
                }
            }
        }

        lblTotalProduits.setText(String.valueOf(total));
        lblPrets.setText(String.valueOf(prets));
        lblCredits.setText(String.valueOf(credits));
        lblAutres.setText(String.valueOf(autres));
    }

    @FXML
    private void onButtonPressed(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(0.95);
            st.setToY(0.95);
            st.play();
        }
    }

    @FXML
    private void onButtonReleased(MouseEvent event) {
        if (event.getSource() instanceof Button) {
            Button button = (Button) event.getSource();
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        }
    }

    @FXML
    private void handleNouveau() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/banque/BanqueFormulaireProduitView.fxml"));
            Parent root = loader.load();

            BanqueFormulaireProduitController controller = loader.getController();
            controller.setModeAjout(currentBanque.getBanqueId());
            controller.setOnSuccess(() -> chargerDonnees());

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 580, 720));
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
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un produit à modifier!", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/banque/BanqueFormulaireProduitView.fxml"));
            Parent root = loader.load();

            BanqueFormulaireProduitController controller = loader.getController();
            controller.setModeModification(selected, currentBanque.getBanqueId());
            controller.setOnSuccess(() -> chargerDonnees());

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 580, 720));
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
    private void handleDetails() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un produit", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/view/DetailsProduitView.fxml"));
            Parent root = loader.load();

            com.agrifund.controller.DetailsProduitController controller = loader.getController();
            controller.setProduit(selected);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 650, 800));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSupprimer() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un produit à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le produit?");
        confirm.setContentText("Voulez-vous vraiment supprimer: " + selected.getNomProduit() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = produitService.supprimerProduit(selected.getIdProduit());

            if (success) {
                chargerDonnees();
                lblStatus.setText("Produit supprimé");
                showAlert("Succès", "Produit supprimé avec succès!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Impossible de supprimer le produit.\nIl est peut-être lié à des offres.", Alert.AlertType.ERROR);
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

        ObservableList<ProduitFinancier> resultats = produitService.rechercherProduitsByBanque(keyword, currentBanque.getBanqueId());
        produitsList.clear();
        produitsList.addAll(resultats);
        tableView.setItems(produitsList);
        lblCount.setText(produitsList.size() + " résultats");
    }

    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerDonnees();
        lblStatus.setText("Actualisé");
    }

    @FXML
    private void handleDownloadPDF() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un produit", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = PDFGenerator.genererPDFProduit(selected);
            lblStatus.setText("PDF généré");
            showAlert("Succès", "PDF téléchargé avec succès!\n\nFichier: " + filePath, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRetour() {
        // Retour au dashboard banque
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
