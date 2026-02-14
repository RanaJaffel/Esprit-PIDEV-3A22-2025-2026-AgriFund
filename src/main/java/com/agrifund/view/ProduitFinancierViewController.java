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
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.util.Optional;

public class ProduitFinancierViewController {

    // FXML Elements
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtTaux;
    @FXML private TextField txtMontantMin;
    @FXML private TextField txtMontantMax;
    @FXML private TextArea txtRegles;
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
        System.out.println("Initialisation ProduitFinancierViewController...");

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

        // Selection listener
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        remplirFormulaire(newVal);
                    }
                });

        // Charger les donnees
        chargerDonnees();
        chargerTypes();

        System.out.println("Initialisation terminee!");
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

    private void animateButton(Button button) {
        ScaleTransition pressTransition = new ScaleTransition(Duration.millis(80), button);
        pressTransition.setToX(0.9);
        pressTransition.setToY(0.9);

        ScaleTransition releaseTransition = new ScaleTransition(Duration.millis(80), button);
        releaseTransition.setToX(1.0);
        releaseTransition.setToY(1.0);

        pressTransition.setOnFinished(e -> releaseTransition.play());
        pressTransition.play();
    }

    // ==================== CHARGEMENT DONNEES ====================

    private void chargerDonnees() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
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

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur chargement: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void chargerTypes() {
        typesList.clear();
        typesList.addAll("Pret", "Credit", "Subvention", "Leasing", "Autre");

        // Ajouter les types de la base de donnees
        try {
            Connection conn = DatabaseConnection.getConnection();
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
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cbType.setItems(typesList);
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

    // ==================== FORMULAIRE ====================

    private void remplirFormulaire(ProduitFinancier p) {
        txtNom.setText(p.getNomProduit());
        cbType.setValue(p.getTypeFinancement());
        txtTaux.setText(String.valueOf(p.getTauxInteret()));
        txtMontantMin.setText(String.valueOf(p.getMontantMin()));
        txtMontantMax.setText(String.valueOf(p.getMontantMax()));
        txtRegles.setText(p.getReglesFinancieres());
    }

    private boolean validerFormulaire() {
        String erreurs = "";

        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            erreurs += "Le nom du produit est obligatoire\n";
        }
        if (cbType.getValue() == null || cbType.getValue().trim().isEmpty()) {
            erreurs += "Le type de financement est obligatoire\n";
        }
        try {
            Double.parseDouble(txtTaux.getText().trim());
        } catch (Exception e) {
            erreurs += "Le taux doit etre un nombre valide\n";
        }
        try {
            Double.parseDouble(txtMontantMin.getText().trim());
        } catch (Exception e) {
            erreurs += "Le montant minimum doit etre un nombre valide\n";
        }
        try {
            Double.parseDouble(txtMontantMax.getText().trim());
        } catch (Exception e) {
            erreurs += "Le montant maximum doit etre un nombre valide\n";
        }

        if (!erreurs.isEmpty()) {
            showAlert("Validation", erreurs, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    // ==================== ACTIONS CRUD ====================

    @FXML
    private void handleNouveau() {
        txtNom.clear();
        cbType.setValue(null);
        txtTaux.clear();
        txtMontantMin.clear();
        txtMontantMax.clear();
        txtRegles.clear();
        tableView.getSelectionModel().clearSelection();
        txtNom.requestFocus();
        lblStatus.setText("Nouveau produit");
        System.out.println("Formulaire vide pour nouveau produit");
    }

    @FXML
    private void handleAjouter() {
        if (!validerFormulaire()) return;

        try {
            ProduitFinancier p = new ProduitFinancier();
            p.setNomProduit(txtNom.getText().trim());
            p.setTypeFinancement(cbType.getValue().trim());
            p.setTauxInteret(Double.parseDouble(txtTaux.getText().trim()));
            p.setMontantMin(Double.parseDouble(txtMontantMin.getText().trim()));
            p.setMontantMax(Double.parseDouble(txtMontantMax.getText().trim()));
            p.setReglesFinancieres(txtRegles.getText().trim());

            boolean success = produitController.ajouterProduit(p);

            if (success) {
                chargerDonnees();
                chargerTypes();
                handleNouveau();
                lblStatus.setText("Produit ajoute");
                showAlert("Succes", "Produit ajoute avec succes!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Impossible d ajouter le produit", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleModifier() {
        ProduitFinancier selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner un produit a modifier", Alert.AlertType.WARNING);
            return;
        }

        if (!validerFormulaire()) return;

        try {
            selected.setNomProduit(txtNom.getText().trim());
            selected.setTypeFinancement(cbType.getValue().trim());
            selected.setTauxInteret(Double.parseDouble(txtTaux.getText().trim()));
            selected.setMontantMin(Double.parseDouble(txtMontantMin.getText().trim()));
            selected.setMontantMax(Double.parseDouble(txtMontantMax.getText().trim()));
            selected.setReglesFinancieres(txtRegles.getText().trim());

            boolean success = produitController.modifierProduit(selected);

            if (success) {
                chargerDonnees();
                chargerTypes();
                handleNouveau();
                lblStatus.setText("Produit modifie");
                showAlert("Succes", "Produit modifie avec succes!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Impossible de modifier le produit", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
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
                handleNouveau();
                lblStatus.setText("Produit supprime");
                showAlert("Succes", "Produit supprime avec succes!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Impossible de supprimer le produit", Alert.AlertType.ERROR);
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
        handleNouveau();
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
            stage.setTitle("Details - " + selected.getNomProduit());
            stage.setScene(new Scene(root, 700, 800));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d ouvrir les details: " + e.getMessage(), Alert.AlertType.ERROR);
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

    // ==================== UTILITAIRES ====================

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}