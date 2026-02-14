package com.agrifund.view;

import com.agrifund.controller.ProduitFinancierController;
import com.agrifund.model.ProduitFinancier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ProduitFinancierViewController implements Initializable {

    // ========== COMPOSANTS FXML - PRODUITS ==========
    @FXML private TableView<ProduitFinancier> tableView;
    @FXML private TableColumn<ProduitFinancier, Integer> colId;
    @FXML private TableColumn<ProduitFinancier, String> colNom;
    @FXML private TableColumn<ProduitFinancier, String> colType;
    @FXML private TableColumn<ProduitFinancier, Double> colTaux;
    @FXML private TableColumn<ProduitFinancier, Double> colMontantMin;
    @FXML private TableColumn<ProduitFinancier, Double> colMontantMax;

    @FXML private TextField txtNom;
    @FXML private TextField txtTaux;
    @FXML private TextField txtMontantMin;
    @FXML private TextField txtMontantMax;
    @FXML private TextField txtRecherche;
    @FXML private TextArea txtRegles;
    @FXML private ComboBox<String> cbType;

    // ========== COMPOSANTS FXML - TYPES ==========
    @FXML private VBox panelTypes;
    @FXML private TextField txtNouveauType;
    @FXML private ListView<String> listTypes;

    // ========== LABELS ==========
    @FXML private Label lblStatus;
    @FXML private Label lblCount;
    @FXML private Label lblTypesCount;
    @FXML private Label lblTotalProduits;
    @FXML private Label lblPrets;
    @FXML private Label lblCredits;
    @FXML private Label lblAutres;

    // ========== VARIABLES ==========
    private ProduitFinancierController controller = new ProduitFinancierController();
    private ProduitFinancier produitSelectionne;
    private ObservableList<String> typesDisponibles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 Initialisation du contrôleur...");

        configurerTableau();
        chargerTypesExistants();
        chargerProduits();
        configurerListeners();

        System.out.println("✅ Initialisation terminée");
    }

    // ========== CONFIGURATION ==========

    private void configurerTableau() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeFinancement"));
        colTaux.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
        colMontantMin.setCellValueFactory(new PropertyValueFactory<>("montantMin"));
        colMontantMax.setCellValueFactory(new PropertyValueFactory<>("montantMax"));

        // Formater les colonnes
        colMontantMin.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.2f DH", item));
            }
        });

        colMontantMax.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.2f DH", item));
            }
        });

        colTaux.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f%%", item));
            }
        });
    }

    private void configurerListeners() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null) afficherProduit(newVal);
                }
        );
    }

    // ========== GESTION DES TYPES ==========

    private void chargerTypesExistants() {
        ObservableList<ProduitFinancier> produits = controller.getAllProduits();
        Set<String> typesUniques = produits.stream()
                .map(ProduitFinancier::getTypeFinancement)
                .filter(type -> type != null && !type.trim().isEmpty())
                .collect(Collectors.toSet());

        if (typesUniques.isEmpty()) {
            typesUniques.addAll(Arrays.asList(
                    "Prêt Agricole",
                    "Crédit d'Équipement",
                    "Subvention",
                    "Leasing",
                    "Microcrédit",
                    "Crédit de Campagne"
            ));
        }

        typesDisponibles = FXCollections.observableArrayList(typesUniques);
        Collections.sort(typesDisponibles);

        cbType.setItems(typesDisponibles);
        listTypes.setItems(typesDisponibles);

        updateTypesCount();
    }

    @FXML
    private void handleToggleTypes() {
        if (panelTypes != null) {
            boolean visible = !panelTypes.isVisible();
            panelTypes.setVisible(visible);
            panelTypes.setManaged(visible);
        }
    }

    @FXML
    private void handleAjouterType() {
        String nouveauType = txtNouveauType.getText().trim();

        if (nouveauType.isEmpty()) {
            alert("⚠️ Attention", "Entrez le nom du type!", Alert.AlertType.WARNING);
            return;
        }

        if (typesDisponibles.contains(nouveauType)) {
            alert("⚠️ Attention", "Ce type existe déjà!", Alert.AlertType.WARNING);
            return;
        }

        typesDisponibles.add(nouveauType);
        Collections.sort(typesDisponibles);
        txtNouveauType.clear();
        updateTypesCount();

        alert("✅ Succès", "Type ajouté: " + nouveauType, Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleModifierType() {
        String typeSelectionne = listTypes.getSelectionModel().getSelectedItem();
        String nouveauNom = txtNouveauType.getText().trim();

        if (typeSelectionne == null) {
            alert("⚠️ Attention", "Sélectionnez un type dans la liste!", Alert.AlertType.WARNING);
            return;
        }

        if (nouveauNom.isEmpty()) {
            alert("⚠️ Attention", "Entrez le nouveau nom du type!", Alert.AlertType.WARNING);
            return;
        }

        if (typesDisponibles.contains(nouveauNom)) {
            alert("⚠️ Attention", "Ce nom de type existe déjà!", Alert.AlertType.WARNING);
            return;
        }

        // Vérifier si le type est utilisé
        List<ProduitFinancier> produitsAvecType = tableView.getItems().stream()
                .filter(p -> p.getTypeFinancement().equals(typeSelectionne))
                .collect(Collectors.toList());

        if (!produitsAvecType.isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ce type est utilisé par " + produitsAvecType.size() + " produit(s).\n" +
                            "Voulez-vous renommer le type pour tous ces produits ?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                for (ProduitFinancier produit : produitsAvecType) {
                    produit.setTypeFinancement(nouveauNom);
                    controller.modifierProduit(produit);
                }
            } else {
                return;
            }
        }

        int index = typesDisponibles.indexOf(typeSelectionne);
        typesDisponibles.set(index, nouveauNom);
        Collections.sort(typesDisponibles);

        txtNouveauType.clear();
        chargerProduits();

        alert("✅ Succès", "Type renommé: " + typeSelectionne + " → " + nouveauNom, Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSupprimerType() {
        String typeSelectionne = listTypes.getSelectionModel().getSelectedItem();

        if (typeSelectionne == null) {
            alert("⚠️ Attention", "Sélectionnez un type!", Alert.AlertType.WARNING);
            return;
        }

        long count = tableView.getItems().stream()
                .filter(p -> p.getTypeFinancement().equals(typeSelectionne))
                .count();

        if (count > 0) {
            alert("⚠️ Attention",
                    "Ce type est utilisé par " + count + " produit(s)!\nSupprimez d'abord les produits.",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le type '" + typeSelectionne + "' ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            typesDisponibles.remove(typeSelectionne);
            updateTypesCount();
            alert("✅ Succès", "Type supprimé!", Alert.AlertType.INFORMATION);
        }
    }

    private void updateTypesCount() {
        if (lblTypesCount != null) {
            lblTypesCount.setText("🏷️ Types: " + typesDisponibles.size());
        }
    }

    // ========== GESTION DES PRODUITS ==========

    private void chargerProduits() {
        ObservableList<ProduitFinancier> produits = controller.getAllProduits();
        tableView.setItems(produits);
        if (lblCount != null) lblCount.setText(produits.size() + " produits");
        updateStats(produits);
    }

    private void updateStats(ObservableList<ProduitFinancier> produits) {
        if (lblTotalProduits != null) lblTotalProduits.setText(String.valueOf(produits.size()));

        long prets = produits.stream()
                .filter(p -> p.getTypeFinancement().toLowerCase().contains("prêt"))
                .count();

        long credits = produits.stream()
                .filter(p -> p.getTypeFinancement().toLowerCase().contains("crédit"))
                .count();

        long autres = produits.size() - prets - credits;

        if (lblPrets != null) lblPrets.setText(String.valueOf(prets));
        if (lblCredits != null) lblCredits.setText(String.valueOf(credits));
        if (lblAutres != null) lblAutres.setText(String.valueOf(autres));
    }

    private void afficherProduit(ProduitFinancier p) {
        produitSelectionne = p;
        txtNom.setText(p.getNomProduit());
        cbType.setValue(p.getTypeFinancement());
        txtTaux.setText(String.valueOf(p.getTauxInteret()));
        txtMontantMin.setText(String.valueOf(p.getMontantMin()));
        txtMontantMax.setText(String.valueOf(p.getMontantMax()));
        txtRegles.setText(p.getReglesFinancieres());
    }

    @FXML
    private void handleNouveau() {
        txtNom.clear();
        txtTaux.clear();
        txtMontantMin.clear();
        txtMontantMax.clear();
        txtRegles.clear();
        cbType.setValue(null);
        produitSelectionne = null;
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAjouter() {
        try {
            if (txtNom.getText().isEmpty() || cbType.getValue() == null) {
                alert("⚠️ Attention", "Remplissez tous les champs obligatoires!", Alert.AlertType.WARNING);
                return;
            }

            String type = cbType.getValue();

            if (!typesDisponibles.contains(type)) {
                typesDisponibles.add(type);
                Collections.sort(typesDisponibles);
                updateTypesCount();
            }

            ProduitFinancier p = new ProduitFinancier(
                    txtNom.getText().trim(),
                    type,
                    Double.parseDouble(txtTaux.getText().trim()),
                    Double.parseDouble(txtMontantMin.getText().trim()),
                    Double.parseDouble(txtMontantMax.getText().trim()),
                    txtRegles.getText().trim()
            );

            if (controller.ajouterProduit(p)) {
                alert("✅ Succès", "Produit ajouté avec succès!", Alert.AlertType.INFORMATION);
                chargerProduits();
                handleNouveau();
            }
        } catch (NumberFormatException e) {
            alert("❌ Erreur", "Vérifiez les valeurs numériques!", Alert.AlertType.ERROR);
        } catch (Exception e) {
            alert("❌ Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleModifier() {
        if (produitSelectionne == null) {
            alert("⚠️", "Sélectionnez un produit!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String type = cbType.getValue();

            if (!typesDisponibles.contains(type)) {
                typesDisponibles.add(type);
                Collections.sort(typesDisponibles);
                updateTypesCount();
            }

            produitSelectionne.setNomProduit(txtNom.getText().trim());
            produitSelectionne.setTypeFinancement(type);
            produitSelectionne.setTauxInteret(Double.parseDouble(txtTaux.getText().trim()));
            produitSelectionne.setMontantMin(Double.parseDouble(txtMontantMin.getText().trim()));
            produitSelectionne.setMontantMax(Double.parseDouble(txtMontantMax.getText().trim()));
            produitSelectionne.setReglesFinancieres(txtRegles.getText().trim());

            if (controller.modifierProduit(produitSelectionne)) {
                alert("✅ Succès", "Produit modifié avec succès!", Alert.AlertType.INFORMATION);
                chargerProduits();
                handleNouveau();
            }
        } catch (Exception e) {
            alert("❌ Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (produitSelectionne == null) {
            alert("⚠️", "Sélectionnez un produit!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer '" + produitSelectionne.getNomProduit() + "' ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (controller.supprimerProduit(produitSelectionne.getIdProduit())) {
                alert("✅ Succès", "Produit supprimé avec succès!", Alert.AlertType.INFORMATION);
                chargerProduits();
                chargerTypesExistants();
                handleNouveau();
            }
        }
    }

    @FXML
    private void handleRechercher() {
        String critere = txtRecherche.getText().trim();
        tableView.setItems(critere.isEmpty() ?
                controller.getAllProduits() :
                controller.rechercherProduits(critere));
    }

    @FXML
    private void handleActualiser() {
        chargerProduits();
        chargerTypesExistants();
        txtRecherche.clear();
        if (lblStatus != null) lblStatus.setText("🔄 Données actualisées");
    }

    @FXML
    private void handleDetails() {
        if (produitSelectionne != null) {
            alert("📄 Détails du Produit", String.format(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📦 Nom: %s\n" +
                            "🏷️ Type: %s\n" +
                            "💹 Taux: %.2f%%\n" +
                            "💰 Montant Min: %,.2f DH\n" +
                            "💵 Montant Max: %,.2f DH\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📜 Règles Financières:\n%s",
                    produitSelectionne.getNomProduit(),
                    produitSelectionne.getTypeFinancement(),
                    produitSelectionne.getTauxInteret(),
                    produitSelectionne.getMontantMin(),
                    produitSelectionne.getMontantMax(),
                    produitSelectionne.getReglesFinancieres()
            ), Alert.AlertType.INFORMATION);
        } else {
            alert("⚠️ Attention", "Sélectionnez un produit!", Alert.AlertType.WARNING);
        }
    }

    private void alert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}