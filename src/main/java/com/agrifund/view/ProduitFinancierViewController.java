package com.agrifund.view;

import com.agrifund.controller.ProduitFinancierController;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.util.DatabaseConnection;
import com.agrifund.util.PDFGenerator;
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
import javafx.util.Duration;

import java.sql.*;
import java.util.Optional;

public class ProduitFinancierViewController {

    @FXML private TextField txtRecherche;
    @FXML private TextField txtNouveauType;

    @FXML private TableView<ProduitFinancier> tableView;
    @FXML private TableColumn<ProduitFinancier, Integer> colId;
    @FXML private TableColumn<ProduitFinancier, String> colNom;
    @FXML private TableColumn<ProduitFinancier, String> colType;
    @FXML private TableColumn<ProduitFinancier, Double> colTaux;
    @FXML private TableColumn<ProduitFinancier, Double> colMontantMin;
    @FXML private TableColumn<ProduitFinancier, Double> colMontantMax;

    @FXML private Label lblTotalProduits;
    @FXML private Label lblPrets;
    @FXML private Label lblCredits;
    @FXML private Label lblAutres;
    @FXML private Label lblCount;
    @FXML private Label lblStatus;
    @FXML private Label lblTypesCount;

    @FXML private VBox panelTypes;
    @FXML private ListView<String> listTypes;
    @FXML private Button btnToggleTypes;
    @FXML private Button btnNouveau;
    @FXML private Button btnActualiser;

    private ProduitFinancierController produitController;
    private ObservableList<ProduitFinancier> produitsList;
    private ObservableList<String> typesList;

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation ProduitFinancierViewController...");

        try {
            produitController = new ProduitFinancierController();
            produitsList = FXCollections.observableArrayList();
            typesList = FXCollections.observableArrayList();

            // Configuration des colonnes
            colId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
            colNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
            colType.setCellValueFactory(new PropertyValueFactory<>("typeFinancement"));
            colTaux.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
            colMontantMin.setCellValueFactory(new PropertyValueFactory<>("montantMin"));
            colMontantMax.setCellValueFactory(new PropertyValueFactory<>("montantMax"));

            // Charger les donnees
            chargerDonnees();
            chargerTypes();

            System.out.println("✅ Initialisation terminee!");

        } catch (Exception e) {
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
        lblStatus.setText("Non connecte");
        lblTypesCount.setText("Types: 0");
        tableView.setItems(FXCollections.observableArrayList());
    }

    // ==================== ANIMATIONS ====================

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

    // ==================== CHARGEMENT DONNEES ====================

    private void chargerDonnees() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("⚠️ Connexion null - impossible de charger les donnees");
                initializeEmptyState();
                return;
            }

            String sql = "SELECT * FROM produit_financier ORDER BY id_produit DESC";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            produitsList.clear();
            while (rs.next()) {
                ProduitFinancier p = new ProduitFinancier();
                p.setIdProduit(rs.getInt("id_produit"));
                p.setNomProduit(rs.getString("nom_produit"));
                p.setTypeFinancement(rs.getString("type_financement"));
                p.setTauxInteret(rs.getDouble("taux_interet"));
                p.setMontantMin(rs.getDouble("montant_min"));
                p.setMontantMax(rs.getDouble("montant_max"));
                p.setReglesFinancieres(rs.getString("regles_financieres"));
                produitsList.add(p);
            }

            tableView.setItems(produitsList);
            mettreAJourStats();
            lblCount.setText(produitsList.size() + " produits");
            lblStatus.setText("Connecte");

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur chargement: " + e.getMessage(), Alert.AlertType.ERROR);
            initializeEmptyState();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void chargerTypes() {
        typesList.clear();
        typesList.addAll("Pret", "Credit", "Subvention", "Leasing", "Autre");

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return;

            String sql = "SELECT DISTINCT type_financement FROM produit_financier";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String type = rs.getString("type_financement");
                if (type != null && !typesList.contains(type)) {
                    typesList.add(type);
                }
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        listTypes.setItems(typesList);
        lblTypesCount.setText("Types: " + typesList.size());
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

    // ==================== GESTION DES TYPES ====================

    @FXML
    private void handleToggleTypes() {
        boolean isVisible = panelTypes.isVisible();
        panelTypes.setVisible(!isVisible);
        panelTypes.setManaged(!isVisible);
        btnToggleTypes.setText(isVisible ? "Gerer Types" : "Masquer Types");
    }

    @FXML
    private void handleAjouterType() {
        String nouveauType = txtNouveauType.getText().trim();
        if (nouveauType.isEmpty()) {
            showAlert("Attention", "Veuillez entrer un nom de type", Alert.AlertType.WARNING);
            return;
        }

        if (!typesList.contains(nouveauType)) {
            typesList.add(nouveauType);
            txtNouveauType.clear();
            lblTypesCount.setText("Types: " + typesList.size());
            showAlert("Succes", "Type ajoute: " + nouveauType, Alert.AlertType.INFORMATION);
        } else {
            showAlert("Attention", "Ce type existe deja", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleModifierType() {
        String selectedType = listTypes.getSelectionModel().getSelectedItem();
        String nouveauNom = txtNouveauType.getText().trim();

        if (selectedType == null) {
            showAlert("Attention", "Veuillez selectionner un type a renommer", Alert.AlertType.WARNING);
            return;
        }

        if (nouveauNom.isEmpty()) {
            showAlert("Attention", "Veuillez entrer le nouveau nom", Alert.AlertType.WARNING);
            return;
        }

        int index = typesList.indexOf(selectedType);
        if (index >= 0) {
            typesList.set(index, nouveauNom);
            txtNouveauType.clear();
            showAlert("Succes", "Type renomme: " + selectedType + " -> " + nouveauNom, Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleSupprimerType() {
        String selectedType = listTypes.getSelectionModel().getSelectedItem();
        if (selectedType == null) {
            showAlert("Attention", "Veuillez selectionner un type a supprimer", Alert.AlertType.WARNING);
            return;
        }

        typesList.remove(selectedType);
        lblTypesCount.setText("Types: " + typesList.size());
        showAlert("Succes", "Type supprime: " + selectedType, Alert.AlertType.INFORMATION);
    }

    // ==================== ACTIONS CRUD AVEC POPUP ====================

    /**
     * NOUVEAU PRODUIT - Ouvre le formulaire popup avec barre de titre personnalisee
     */
    @FXML
    private void handleNouveau() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/view/FormulaireProduitView.fxml"));
            Parent root = loader.load();

            FormulaireProduitController controller = loader.getController();
            controller.setModeAjout();
            controller.setOnSuccess(() -> {
                chargerDonnees();
                chargerTypes();
            });

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);  // Custom title bar
            stage.setScene(new Scene(root, 580, 720));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            System.out.println("✅ Formulaire Nouveau Produit ouvert");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * MODIFIER PRODUIT - Ouvre le formulaire popup avec barre de titre personnalisee
     */
    @FXML
    private void handleModifier() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner un produit a modifier!", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/view/FormulaireProduitView.fxml"));
            Parent root = loader.load();

            FormulaireProduitController controller = loader.getController();
            controller.setModeModification(selected);
            controller.setOnSuccess(() -> {
                chargerDonnees();
                chargerTypes();
            });

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);  // Custom title bar
            stage.setScene(new Scene(root, 580, 720));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            System.out.println("✅ Formulaire Modifier Produit ouvert pour: " + selected.getNomProduit());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSupprimer() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner un produit a supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le produit?");
        confirm.setContentText("Voulez-vous vraiment supprimer: " + selected.getNomProduit() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = produitController.supprimerProduit(selected.getIdProduit());

            if (success) {
                chargerDonnees();
                chargerTypes();
                lblStatus.setText("Produit supprime");
                showAlert("Succes", "Produit supprime avec succes!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Impossible de supprimer le produit.\nIl est peut-etre lie a des offres.", Alert.AlertType.ERROR);
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

        ObservableList<ProduitFinancier> resultats = produitController.rechercherProduits(keyword);
        produitsList.clear();
        produitsList.addAll(resultats);
        tableView.setItems(produitsList);
        lblCount.setText(produitsList.size() + " resultats");
    }

    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerDonnees();
        chargerTypes();
        lblStatus.setText("Actualise");
    }


    @FXML
    private void handleDetails() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner un produit", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/view/DetailsProduitView.fxml"));
            Parent root = loader.load();

            DetailsProduitController controller = loader.getController();
            controller.setProduit(selected);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);  // Custom title bar
            stage.setScene(new Scene(root, 650, 800));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDownloadPDF() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner un produit", Alert.AlertType.WARNING);
            return;
        }

        try {
            String filePath = PDFGenerator.genererPDFProduit(selected);
            lblStatus.setText("PDF genere");
            showAlert("Succes", "PDF telecharge avec succes!\n\nFichier: " + filePath, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de generer le PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ==================== UTILITAIRES ====================

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}