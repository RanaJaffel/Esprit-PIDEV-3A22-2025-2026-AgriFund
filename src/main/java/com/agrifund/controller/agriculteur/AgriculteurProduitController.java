package com.agrifund.controller.agriculteur;

import com.agrifund.Main;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.ProduitFinancierService;
import com.agrifund.util.PDFGenerator;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

public class AgriculteurProduitController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cmbTypeFinancement;
    @FXML private FlowPane containerProduits;
    @FXML private Label lblNombreProduits;
    @FXML private Label lblStatus;

    // Stats
    @FXML private Label lblTotalProduits;
    @FXML private Label lblPrets;
    @FXML private Label lblCredits;
    @FXML private Label lblAutres;

    private ProduitFinancierService produitService;
    private ObservableList<ProduitFinancier> tousLesProduits;

    // Palette de couleurs pour les cartes
    private static final String[][] CARD_THEMES = {
            {"#B2D944", "#1a2e10", "#243d15"},  // Lime green
            {"#4fc3f7", "#0a1e2e", "#10324a"},  // Sky blue
            {"#ffb74d", "#2a1a05", "#3d2a0a"},  // Amber
            {"#ef5350", "#2a0a0a", "#3d1010"},  // Red
            {"#ce93d8", "#1e0a2a", "#2d1040"},  // Purple
            {"#26c6da", "#0a2a2e", "#103d42"},  // Teal
    };

    @FXML
    public void initialize() {
        produitService = new ProduitFinancierService();

        // Types de financement
        List<String> types = Arrays.asList(
                "Tous les types", "Crédit", "Prêt", "Leasing", "Subvention", "Microfinance"
        );
        cmbTypeFinancement.setItems(FXCollections.observableArrayList(types));
        cmbTypeFinancement.setValue("Tous les types");

        chargerDonnees();
    }

    private void chargerDonnees() {
        lblStatus.setText("Chargement...");

        new Thread(() -> {
            try {
                tousLesProduits = produitService.getAllProduits();

                Platform.runLater(() -> {
                    mettreAJourStats();
                    afficherProduits(tousLesProduits);
                    lblStatus.setText("✓ " + tousLesProduits.size() + " produits disponibles");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblStatus.setText("Erreur de chargement");
                    showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private void mettreAJourStats() {
        int total = tousLesProduits.size();
        int prets = 0, credits = 0, autres = 0;

        for (ProduitFinancier p : tousLesProduits) {
            String type = p.getTypeFinancement();
            if (type != null) {
                if (type.toLowerCase().contains("pret") || type.toLowerCase().contains("prêt")) {
                    prets++;
                } else if (type.toLowerCase().contains("credit") || type.toLowerCase().contains("crédit")) {
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

    private void afficherProduits(ObservableList<ProduitFinancier> produits) {
        containerProduits.getChildren().clear();

        if (produits.isEmpty()) {
            Label lblVide = new Label("🔍 Aucun produit trouvé pour votre recherche");
            lblVide.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 40;");
            containerProduits.getChildren().add(lblVide);
            lblNombreProduits.setText("(0)");
            return;
        }

        lblNombreProduits.setText("(" + produits.size() + ")");

        int idx = 0;
        for (ProduitFinancier produit : produits) {
            VBox card = creerCarteProduit(produit, idx);
            animateCardEntry(card, idx);
            containerProduits.getChildren().add(card);
            idx++;
        }
    }

    private VBox creerCarteProduit(ProduitFinancier produit, int index) {
        String[] theme = CARD_THEMES[index % CARD_THEMES.length];
        String accent = theme[0];
        String bgStart = theme[1];
        String bgEnd = theme[2];

        VBox card = new VBox(0);
        card.setPrefWidth(320);
        card.setMinWidth(290);
        card.setMaxWidth(350);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + bgStart + ", " + bgEnd + ");" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + accent + "15;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 1;" +
                        "-fx-cursor: hand;"
        );
        card.setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.4)));

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 20 22 14 22;");

        StackPane iconBubble = new StackPane();
        iconBubble.setPrefSize(44, 44);
        iconBubble.setStyle("-fx-background-color: " + accent + "20; -fx-background-radius: 12;");
        Label iconTxt = new Label(getTypeIcon(produit.getTypeFinancement()));
        iconTxt.setStyle("-fx-font-size: 20;");
        iconBubble.getChildren().add(iconTxt);

        VBox nameBox = new VBox(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label lblNom = new Label(produit.getNomProduit());
        lblNom.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;");
        lblNom.setWrapText(true);

        Label lblType = new Label(produit.getTypeFinancement());
        lblType.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 11; -fx-font-weight: bold;");
        nameBox.getChildren().addAll(lblNom, lblType);

        // Rate badge
        StackPane rateBubble = new StackPane();
        rateBubble.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 10;");
        Label lblRate = new Label(String.format("%.1f%%", produit.getTauxInteret()));
        lblRate.setStyle("-fx-text-fill: #0c1b13; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 4 12;");
        rateBubble.getChildren().add(lblRate);

        header.getChildren().addAll(iconBubble, nameBox, rateBubble);

        // Body
        VBox body = new VBox(10);
        body.setStyle("-fx-padding: 0 22 18 22;");

        // Montant range
        HBox montantRow = new HBox(8);
        montantRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMin = new Label(String.format("%.0f DT", produit.getMontantMin()));
        lblMin.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11;");
        Region rangeSpacer = new Region();
        HBox.setHgrow(rangeSpacer, Priority.ALWAYS);
        Label lblMax = new Label(String.format("%.0f DT", produit.getMontantMax()));
        lblMax.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 12; -fx-font-weight: bold;");
        montantRow.getChildren().addAll(lblMin, rangeSpacer, lblMax);

        // Progress bar
        StackPane barBg = new StackPane();
        barBg.setPrefHeight(5);
        barBg.setMaxWidth(Double.MAX_VALUE);
        barBg.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 3;");

        Region barFill = new Region();
        barFill.setPrefHeight(5);
        barFill.setStyle("-fx-background-color: linear-gradient(to right, " + accent + "88, " + accent + "); -fx-background-radius: 3;");
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
        double maxGlobal = tousLesProduits.stream().mapToDouble(ProduitFinancier::getMontantMax).max().orElse(1);
        double ratio = Math.min(produit.getMontantMax() / maxGlobal, 1.0);
        barFill.maxWidthProperty().bind(barBg.widthProperty().multiply(ratio));
        barBg.getChildren().add(barFill);

        // Banque info
        Label lblBanque = new Label("🏦 " + (produit.getNomBanque() != null ? produit.getNomBanque() : "Banque partenaire"));
        lblBanque.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 10;");

        // Rules
        String regles = produit.getReglesFinancieres();
        Label lblRegles = new Label("📋 " + (regles != null && !regles.isEmpty() ?
                (regles.length() > 60 ? regles.substring(0, 60) + "..." : regles) : "Conditions disponibles"));
        lblRegles.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 10;");
        lblRegles.setWrapText(true);
        lblRegles.setMaxHeight(30);

        // Buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        Button btnDetails = new Button("Détails");
        btnDetails.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: rgba(255,255,255,0.7);" +
                        "-fx-padding: 8 18; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 11;"
        );
        btnDetails.setOnAction(e -> afficherDetailsProduit(produit));

        Button btnDemander = new Button("Demander →");
        btnDemander.setStyle(
                "-fx-background-color: " + accent + "; -fx-text-fill: #0c1b13; -fx-font-weight: bold;" +
                        "-fx-padding: 8 18; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 11;"
        );
        btnDemander.setOnAction(e -> ouvrirFormulaireDemande(produit));

        actions.getChildren().addAll(btnDetails, btnDemander);

        body.getChildren().addAll(montantRow, barBg, lblBanque, lblRegles, actions);

        card.getChildren().addAll(header, body);

        // Hover effects
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
            card.setEffect(new DropShadow(28, Color.web(accent + "44")));
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            card.setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.4)));
        });

        return card;
    }

    private String getTypeIcon(String type) {
        if (type == null) return "📦";
        return switch (type.toLowerCase()) {
            case "crédit", "credit" -> "💳";
            case "prêt", "pret" -> "🏦";
            case "leasing" -> "🚜";
            case "subvention" -> "🎯";
            case "microfinance" -> "🌾";
            default -> "📦";
        };
    }

    private void animateCardEntry(Node card, int index) {
        card.setOpacity(0);
        card.setTranslateY(30);

        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(index * 60));

        TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
        tt.setFromY(30);
        tt.setToY(0);
        tt.setDelay(Duration.millis(index * 60));

        ft.play();
        tt.play();
    }

    // ==================== ACTIONS ====================

    @FXML
    private void handleRechercher() {
        String recherche = txtRecherche.getText().trim().toLowerCase();
        String typeSelectionne = cmbTypeFinancement.getValue();

        ObservableList<ProduitFinancier> produitsFiltres = FXCollections.observableArrayList();

        for (ProduitFinancier p : tousLesProduits) {
            boolean matchRecherche = recherche.isEmpty()
                    || p.getNomProduit().toLowerCase().contains(recherche)
                    || p.getTypeFinancement().toLowerCase().contains(recherche)
                    || (p.getReglesFinancieres() != null && p.getReglesFinancieres().toLowerCase().contains(recherche))
                    || (p.getNomBanque() != null && p.getNomBanque().toLowerCase().contains(recherche));

            boolean matchType = typeSelectionne == null
                    || typeSelectionne.equals("Tous les types")
                    || p.getTypeFinancement().equalsIgnoreCase(typeSelectionne);

            if (matchRecherche && matchType) {
                produitsFiltres.add(p);
            }
        }

        afficherProduits(produitsFiltres);
        lblStatus.setText("🔍 " + produitsFiltres.size() + " résultat(s)");
    }

    @FXML
    private void handleRafraichir() {
        txtRecherche.clear();
        cmbTypeFinancement.setValue("Tous les types");
        chargerDonnees();
    }

    private void afficherDetailsProduit(ProduitFinancier produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DetailsProduitView.fxml"));
            Parent root = loader.load();

            com.agrifund.controller.DetailsProduitController controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Détails — " + produit.getNomProduit());
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 650, 800));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void ouvrirFormulaireDemande(ProduitFinancier produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DemandeFinancementView.fxml"));
            Parent root = loader.load();

            com.agrifund.controller.DemandeFinancementController controller = loader.getController();
            controller.setProduitSelectionne(produit);

            Stage popup = new Stage();
            popup.setTitle("AgriFund — Demande: " + produit.getNomProduit());
            popup.setScene(new Scene(root, 1000, 700));
            popup.show();

        } catch (Exception e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRetour() {
        Main.navigateTo("/com/agrifund/fxml/agriculteur/agriculteur-dashboard.fxml");
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}