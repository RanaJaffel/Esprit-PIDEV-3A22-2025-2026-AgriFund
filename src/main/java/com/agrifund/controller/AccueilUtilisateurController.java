package com.agrifund.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.OffreFinanciereService;
import com.agrifund.services.ProduitFinancierService;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AccueilUtilisateurController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cmbTypeFinancement;
    @FXML private FlowPane containerProduits;
    @FXML private FlowPane containerOffres;
    @FXML private Label lblNombreProduits;
    @FXML private Label lblNombreOffres;
    @FXML private Label lblWelcomeName;

    // KPI Labels
    @FXML private Label lblStatProduits;
    @FXML private Label lblStatOffres;
    @FXML private Label lblStatTaux;
    @FXML private Label lblStatMontant;

    // Chart
    @FXML private PieChart pieChartTypes;

    // Featured product spotlight
    @FXML private VBox containerFeatured;

    // Activity timeline
    @FXML private VBox containerActivites;

    // Scroll targets
    @FXML private VBox sectionProduits;
    @FXML private VBox sectionOffres;

    private ProduitFinancierService produitService;
    private OffreFinanciereService offreService;

    private ObservableList<ProduitFinancier> tousLesProduits;
    private ObservableList<OffreFinanciere> toutesLesOffres;

    // Creative color palette — vibrant neon-on-dark
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
        offreService = new OffreFinanciereService();

        List<String> types = Arrays.asList(
            "Tous les types", "Crédit", "Prêt", "Leasing", "Subvention", "Microfinance"
        );
        cmbTypeFinancement.setItems(FXCollections.observableArrayList(types));
        cmbTypeFinancement.setValue("Tous les types");

        populateTimeline();
        chargerDonnees();
    }

    // ═══════════════════════════ DATA LOADING ═══════════════════════════

    private void chargerDonnees() {
        new Thread(() -> {
            try {
                tousLesProduits = produitService.getAllProduits();
                toutesLesOffres = offreService.getAllOffresAvecProduit();

                Platform.runLater(() -> {
                    animateKPIs();
                    updateChart();
                    buildFeaturedSpotlight();
                    afficherProduits(tousLesProduits);
                    afficherOffres(toutesLesOffres);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Erreur de chargement",
                        "Impossible de charger les données: " + e.getMessage()));
            }
        }).start();
    }

    // ═══════════════════════════ ANIMATED KPI COUNTERS ═══════════════════════════

    private void animateKPIs() {
        if (tousLesProduits == null) return;

        int totalProduits = tousLesProduits.size();
        int totalOffres = toutesLesOffres != null ? toutesLesOffres.size() : 0;
        double avgTaux = tousLesProduits.stream()
                .mapToDouble(ProduitFinancier::getTauxInteret).average().orElse(0);
        double maxMontant = tousLesProduits.stream()
                .mapToDouble(ProduitFinancier::getMontantMax).max().orElse(0);

        animateCounter(lblStatProduits, totalProduits, "");
        animateCounter(lblStatOffres, totalOffres, "");
        animateCounterDecimal(lblStatTaux, avgTaux);

        if (maxMontant >= 1_000_000)
            lblStatMontant.setText(String.format("%.1fM", maxMontant / 1_000_000));
        else if (maxMontant >= 1_000)
            lblStatMontant.setText(String.format("%.0fK", maxMontant / 1_000));
        else
            lblStatMontant.setText(String.format("%.0f", maxMontant));
    }

    private void animateCounter(Label label, int target, String suffix) {
        IntegerProperty counter = new SimpleIntegerProperty(0);
        counter.addListener((obs, ov, nv) -> label.setText(nv.intValue() + suffix));
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(counter, 0)),
            new KeyFrame(Duration.millis(900), new KeyValue(counter, target))
        );
        tl.play();
    }

    private void animateCounterDecimal(Label label, double target) {
        IntegerProperty counter = new SimpleIntegerProperty(0);
        int targetInt = (int) (target * 10);
        counter.addListener((obs, ov, nv) ->
            label.setText(String.format("%.1f%%", nv.intValue() / 10.0))
        );
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(counter, 0)),
            new KeyFrame(Duration.millis(900), new KeyValue(counter, targetInt))
        );
        tl.play();
    }

    // ═══════════════════════════ FEATURED SPOTLIGHT ═══════════════════════════

    private void buildFeaturedSpotlight() {
        containerFeatured.getChildren().clear();
        if (tousLesProduits == null || tousLesProduits.isEmpty()) return;

        // Pick the product with the best (lowest) rate
        ProduitFinancier best = tousLesProduits.stream()
                .min((a, b) -> Double.compare(a.getTauxInteret(), b.getTauxInteret()))
                .orElse(tousLesProduits.get(0));

        HBox content = new HBox(30);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setStyle("-fx-padding: 32 40;");

        // Left side — info
        VBox leftInfo = new VBox(10);
        leftInfo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(leftInfo, Priority.ALWAYS);

        Label lblBadge = new Label("⭐  PRODUIT EN VEDETTE");
        lblBadge.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);" +
            "-fx-text-fill: #B2D944; -fx-font-size: 11; -fx-font-weight: bold;" +
            "-fx-padding: 5 14; -fx-background-radius: 20;"
        );

        Label lblName = new Label(best.getNomProduit());
        lblName.setStyle("-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold;");
        lblName.setWrapText(true);

        Label lblDesc = new Label("📋 " + best.getReglesFinancieres());
        lblDesc.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 12;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(420);

        HBox statsRow = new HBox(24);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.setPadding(new Insets(6, 0, 0, 0));

        VBox statTaux = new VBox(1);
        statTaux.getChildren().addAll(
            styledLabel(String.format("%.2f%%", best.getTauxInteret()), "#B2D944", 22, true),
            styledLabel("Taux d'intérêt", "rgba(255,255,255,0.5)", 10, false)
        );

        VBox statMin = new VBox(1);
        statMin.getChildren().addAll(
            styledLabel(String.format("%.0f MAD", best.getMontantMin()), "white", 16, true),
            styledLabel("Montant Min", "rgba(255,255,255,0.5)", 10, false)
        );

        VBox statMax = new VBox(1);
        statMax.getChildren().addAll(
            styledLabel(String.format("%.0f MAD", best.getMontantMax()), "white", 16, true),
            styledLabel("Montant Max", "rgba(255,255,255,0.5)", 10, false)
        );

        statsRow.getChildren().addAll(statTaux, statMin, statMax);

        HBox btnRow = new HBox(10);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        Button btnDetails = new Button("Voir les détails");
        btnDetails.setStyle(
            "-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: white;" +
            "-fx-padding: 10 24; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 12;"
        );
        btnDetails.setOnAction(e -> afficherDetailsProduit(best));

        Button btnDemander = new Button("Demander maintenant →");
        btnDemander.setStyle(
            "-fx-background-color: #B2D944; -fx-text-fill: #0c1b13; -fx-font-weight: bold;" +
            "-fx-padding: 10 24; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 12;"
        );
        btnDemander.setOnAction(e -> ouvrirFormulaireAvecProduit(best));

        btnRow.getChildren().addAll(btnDetails, btnDemander);

        leftInfo.getChildren().addAll(lblBadge, lblName, lblDesc, statsRow, btnRow);

        // Right side — circular rate display
        StackPane rateCircle = new StackPane();
        rateCircle.setPrefSize(140, 140);
        rateCircle.setMinSize(140, 140);

        Circle outerCircle = new Circle(70);
        outerCircle.setFill(Color.web("rgba(255,255,255,0.06)"));
        outerCircle.setStroke(Color.web("#B2D944"));
        outerCircle.setStrokeWidth(4);

        Circle innerCircle = new Circle(55);
        innerCircle.setFill(Color.web("rgba(0,0,0,0.3)"));

        VBox rateInfo = new VBox(2);
        rateInfo.setAlignment(Pos.CENTER);
        rateInfo.getChildren().addAll(
            styledLabel(String.format("%.1f%%", best.getTauxInteret()), "#B2D944", 24, true),
            styledLabel("Taux", "rgba(255,255,255,0.5)", 10, false)
        );

        rateCircle.getChildren().addAll(outerCircle, innerCircle, rateInfo);

        content.getChildren().addAll(leftInfo, rateCircle);
        containerFeatured.getChildren().add(content);

        // Animate spotlight entry
        containerFeatured.setOpacity(0);
        containerFeatured.setTranslateY(20);
        FadeTransition ft = new FadeTransition(Duration.millis(600), containerFeatured);
        ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(300));
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), containerFeatured);
        tt.setFromY(20); tt.setToY(0); tt.setDelay(Duration.millis(300));
        ft.play(); tt.play();
    }

    private Label styledLabel(String text, String color, int size, boolean bold) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + ";" +
                     (bold ? " -fx-font-weight: bold;" : ""));
        return lbl;
    }

    // ═══════════════════════════ ACTIVITY TIMELINE ═══════════════════════════

    private void populateTimeline() {
        containerActivites.getChildren().clear();

        String[][] activities = {
            {"#B2D944", "Consultation", "Crédit Équipement Agricole consulté", "Il y a 2h"},
            {"#4fc3f7", "Recherche",    "Produits de type Leasing explorés",   "Il y a 5h"},
            {"#ffb74d", "Offre vue",    "Offre promotionnelle printemps vue",  "Hier"},
            {"#ce93d8", "Inscription",  "Bienvenue sur AgriFund !",            "25 Fév"},
        };

        for (int i = 0; i < activities.length; i++) {
            String[] act = activities[i];
            boolean isLast = (i == activities.length - 1);

            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                "-fx-padding: 12 16; -fx-background-color: rgba(255,255,255,0.03);" +
                "-fx-background-radius: 12; -fx-cursor: hand;"
            );

            // Timeline dot + vertical line
            VBox timelineTrack = new VBox(0);
            timelineTrack.setAlignment(Pos.TOP_CENTER);
            timelineTrack.setPrefWidth(20);

            Circle dot = new Circle(5);
            dot.setFill(Color.web(act[0]));
            dot.setEffect(new DropShadow(8, Color.web(act[0] + "66")));
            timelineTrack.getChildren().add(dot);

            if (!isLast) {
                Line line = new Line(0, 0, 0, 18);
                line.setStroke(Color.web("rgba(255,255,255,0.08)"));
                line.setStrokeWidth(1.5);
                timelineTrack.getChildren().add(line);
            }

            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label lblTitle = new Label(act[1]);
            lblTitle.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + act[0] + ";");
            Label lblDesc = new Label(act[2]);
            lblDesc.setStyle("-fx-font-size: 11; -fx-text-fill: rgba(255,255,255,0.45);");
            info.getChildren().addAll(lblTitle, lblDesc);

            Label lblDate = new Label(act[3]);
            lblDate.setStyle("-fx-font-size: 10; -fx-text-fill: rgba(255,255,255,0.25);");

            row.getChildren().addAll(timelineTrack, info, lblDate);

            // Hover glow
            row.setOnMouseEntered(e -> row.setStyle(
                "-fx-padding: 12 16; -fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-background-radius: 12; -fx-cursor: hand;"
            ));
            row.setOnMouseExited(e -> row.setStyle(
                "-fx-padding: 12 16; -fx-background-color: rgba(255,255,255,0.03);" +
                "-fx-background-radius: 12; -fx-cursor: hand;"
            ));

            containerActivites.getChildren().add(row);
        }
    }

    // ═══════════════════════════ PIE CHART ═══════════════════════════

    private void updateChart() {
        if (tousLesProduits == null || tousLesProduits.isEmpty()) return;

        Map<String, Integer> typeCount = new HashMap<>();
        for (ProduitFinancier p : tousLesProduits) {
            typeCount.merge(p.getTypeFinancement(), 1, Integer::sum);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        typeCount.forEach((type, count) ->
            pieData.add(new PieChart.Data(type + " (" + count + ")", count))
        );
        pieChartTypes.setData(pieData);

        String[] chartColors = {"#B2D944", "#4fc3f7", "#ffb74d", "#ef5350", "#ce93d8", "#26c6da"};
        int idx = 0;
        for (PieChart.Data d : pieChartTypes.getData()) {
            String col = chartColors[idx % chartColors.length];
            d.getNode().setStyle("-fx-pie-color: " + col + ";");
            idx++;
        }
    }

    // ═══════════════════════════ PRODUCT CARDS ═══════════════════════════

    private void afficherProduits(ObservableList<ProduitFinancier> produits) {
        containerProduits.getChildren().clear();

        if (produits.isEmpty()) {
            Label lblVide = new Label("Aucun produit disponible pour le moment.");
            lblVide.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 14;");
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
        card.setPrefWidth(310);
        card.setMinWidth(280);
        card.setMaxWidth(340);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + bgStart + ", " + bgEnd + ");" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + accent + "15;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        card.setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.4)));

        // ── Gradient top header ──
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 20 22 14 22;");

        StackPane iconBubble = new StackPane();
        iconBubble.setPrefSize(40, 40);
        iconBubble.setStyle("-fx-background-color: " + accent + "20; -fx-background-radius: 12;");
        Label iconTxt = new Label(getTypeIcon(produit.getTypeFinancement()));
        iconTxt.setStyle("-fx-font-size: 18;");
        iconBubble.getChildren().add(iconTxt);

        VBox nameBox = new VBox(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label lblNom = new Label(produit.getNomProduit());
        lblNom.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold;");
        lblNom.setWrapText(true);

        Label lblType = new Label(produit.getTypeFinancement());
        lblType.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 10; -fx-font-weight: bold;");
        nameBox.getChildren().addAll(lblNom, lblType);

        // Rate badge
        StackPane rateBubble = new StackPane();
        rateBubble.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 10;");
        Label lblRate = new Label(String.format("%.1f%%", produit.getTauxInteret()));
        lblRate.setStyle("-fx-text-fill: #0c1b13; -fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 4 12;");
        rateBubble.getChildren().add(lblRate);

        header.getChildren().addAll(iconBubble, nameBox, rateBubble);

        // ── Body ──
        VBox body = new VBox(10);
        body.setStyle("-fx-padding: 0 22 16 22;");

        // Montant range with glow bar
        HBox montantRow = new HBox(8);
        montantRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMin = new Label(String.format("%.0f", produit.getMontantMin()));
        lblMin.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11;");
        Region rangeSpacer = new Region();
        HBox.setHgrow(rangeSpacer, Priority.ALWAYS);
        Label lblMax = new Label(String.format("%.0f MAD", produit.getMontantMax()));
        lblMax.setStyle("-fx-text-fill: " + accent + "; -fx-font-size: 12; -fx-font-weight: bold;");
        montantRow.getChildren().addAll(lblMin, rangeSpacer, lblMax);

        // Progress bar with glow
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

        // Rules
        Label lblRegles = new Label("📋 " + produit.getReglesFinancieres());
        lblRegles.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 10;");
        lblRegles.setWrapText(true);
        lblRegles.setMaxHeight(30);

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(6, 0, 0, 0));

        Button btnDetails = new Button("Détails");
        btnDetails.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: rgba(255,255,255,0.6);" +
            "-fx-padding: 8 18; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 11;"
        );
        btnDetails.setOnAction(e -> afficherDetailsProduit(produit));

        Button btnDemander = new Button("Demander →");
        btnDemander.setStyle(
            "-fx-background-color: " + accent + "; -fx-text-fill: #0c1b13; -fx-font-weight: bold;" +
            "-fx-padding: 8 18; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 11;"
        );
        btnDemander.setOnAction(e -> ouvrirFormulaireAvecProduit(produit));

        actions.getChildren().addAll(btnDetails, btnDemander);

        body.getChildren().addAll(montantRow, barBg, lblRegles, actions);

        card.getChildren().addAll(header, body);

        // Hover effects
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.03); st.setToY(1.03); st.play();
            card.setEffect(new DropShadow(28, Color.web(accent + "44")));
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + bgEnd + ", " + bgStart + ");" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: " + accent + "44;" +
                "-fx-border-radius: 20;" +
                "-fx-border-width: 1.5;" +
                "-fx-cursor: hand;"
            );
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
            card.setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.4)));
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + bgStart + ", " + bgEnd + ");" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: " + accent + "15;" +
                "-fx-border-radius: 20;" +
                "-fx-border-width: 1;" +
                "-fx-cursor: hand;"
            );
        });

        return card;
    }

    private String getTypeIcon(String type) {
        return switch (type.toLowerCase()) {
            case "crédit" -> "💳";
            case "prêt" -> "🏦";
            case "leasing" -> "🚜";
            case "subvention" -> "🎯";
            case "microfinance" -> "🌾";
            default -> "📦";
        };
    }

    // ═══════════════════════════ OFFER CARDS ═══════════════════════════

    private void afficherOffres(ObservableList<OffreFinanciere> offres) {
        containerOffres.getChildren().clear();

        if (offres == null || offres.isEmpty()) {
            Label lblVide = new Label("Aucune offre spéciale pour le moment.");
            lblVide.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 14;");
            containerOffres.getChildren().add(lblVide);
            lblNombreOffres.setText("(0)");
            return;
        }

        lblNombreOffres.setText("(" + offres.size() + ")");

        int idx = 0;
        for (OffreFinanciere offre : offres) {
            VBox card = creerCarteOffre(offre, idx);
            animateCardEntry(card, idx);
            containerOffres.getChildren().add(card);
            idx++;
        }
    }

    private VBox creerCarteOffre(OffreFinanciere offre, int index) {
        VBox card = new VBox(0);
        card.setPrefWidth(310);
        card.setMinWidth(280);
        card.setMaxWidth(340);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #2a1a05, #1e1608);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(255,183,77,0.12);" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        card.setEffect(new DropShadow(16, Color.rgb(255, 152, 0, 0.12)));

        // ── Header with ribbon badge ──
        StackPane headerStack = new StackPane();
        headerStack.setStyle("-fx-padding: 20 22 10 22;");

        HBox headerContent = new HBox(12);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setMaxWidth(Double.MAX_VALUE);

        StackPane giftCircle = new StackPane();
        giftCircle.setPrefSize(44, 44);
        giftCircle.setMinSize(44, 44);
        giftCircle.setStyle("-fx-background-color: rgba(255,183,77,0.15); -fx-background-radius: 50;");
        Label giftIcon = new Label("🎁");
        giftIcon.setStyle("-fx-font-size: 20;");
        giftCircle.getChildren().add(giftIcon);

        VBox offreNameBox = new VBox(2);
        HBox.setHgrow(offreNameBox, Priority.ALWAYS);
        Label lblNom = new Label(offre.getNomOffre());
        lblNom.setStyle("-fx-text-fill: #ffb74d; -fx-font-size: 14; -fx-font-weight: bold;");
        lblNom.setWrapText(true);

        Label lblStatut = new Label("● " + offre.getStatut());
        String statutColor = offre.getStatut().equalsIgnoreCase("Active") ? "#66bb6a" : "#ffb74d";
        lblStatut.setStyle("-fx-text-fill: " + statutColor + "; -fx-font-size: 10; -fx-font-weight: bold;");

        offreNameBox.getChildren().addAll(lblNom, lblStatut);
        headerContent.getChildren().addAll(giftCircle, offreNameBox);
        headerStack.getChildren().add(headerContent);

        // ── Body ──
        VBox body = new VBox(8);
        body.setStyle("-fx-padding: 4 22 18 22;");

        if (offre.getProduitFinancier() != null) {
            HBox prodRef = new HBox(8);
            prodRef.setAlignment(Pos.CENTER_LEFT);
            prodRef.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-padding: 8 12; -fx-background-radius: 8;");
            Label prodIcon = new Label("📦");
            prodIcon.setStyle("-fx-font-size: 12;");
            Label lblProd = new Label(offre.getProduitFinancier().getNomProduit());
            lblProd.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 11; -fx-font-weight: bold;");
            prodRef.getChildren().addAll(prodIcon, lblProd);
            body.getChildren().add(prodRef);
        }

        Label lblConditions = new Label(offre.getConditions());
        lblConditions.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 11;");
        lblConditions.setWrapText(true);
        lblConditions.setMaxHeight(34);

        Button btnProfiter = new Button("Profiter de cette offre →");
        btnProfiter.setMaxWidth(Double.MAX_VALUE);
        btnProfiter.setStyle(
            "-fx-background-color: linear-gradient(to right, #e65100, #ff8f00);" +
            "-fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-padding: 10 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-size: 12;"
        );
        btnProfiter.setOnAction(e -> ouvrirFormulaireAvecOffre(offre));

        body.getChildren().addAll(lblConditions, btnProfiter);

        card.getChildren().addAll(headerStack, body);

        // Hover
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.03); st.setToY(1.03); st.play();
            card.setEffect(new DropShadow(24, Color.rgb(255, 152, 0, 0.25)));
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
            card.setEffect(new DropShadow(16, Color.rgb(255, 152, 0, 0.12)));
        });

        return card;
    }

    // ═══════════════════════════ ANIMATIONS ═══════════════════════════

    private void animateCardEntry(Node card, int index) {
        card.setOpacity(0);
        card.setTranslateY(30);

        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0); ft.setToValue(1);
        ft.setDelay(Duration.millis(index * 70));

        TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
        tt.setFromY(30); tt.setToY(0);
        tt.setDelay(Duration.millis(index * 70));

        ft.play();
        tt.play();
    }

    // ═══════════════════════════ SCROLL HANDLERS ═══════════════════════════

    @FXML
    private void scrollToProduits() {
        if (sectionProduits != null) sectionProduits.requestFocus();
    }

    @FXML
    private void scrollToOffres() {
        if (sectionOffres != null) sectionOffres.requestFocus();
    }

    // ═══════════════════════════ SEARCH & FILTER ═══════════════════════════

    @FXML
    private void handleRechercher() {
        String recherche = txtRecherche.getText().trim().toLowerCase();
        String typeSelectionne = cmbTypeFinancement.getValue();

        ObservableList<ProduitFinancier> produitsFiltres = FXCollections.observableArrayList();

        for (ProduitFinancier p : tousLesProduits) {
            boolean matchRecherche = recherche.isEmpty()
                    || p.getNomProduit().toLowerCase().contains(recherche)
                    || p.getTypeFinancement().toLowerCase().contains(recherche)
                    || p.getReglesFinancieres().toLowerCase().contains(recherche);

            boolean matchType = typeSelectionne == null
                    || typeSelectionne.equals("Tous les types")
                    || p.getTypeFinancement().equalsIgnoreCase(typeSelectionne);

            if (matchRecherche && matchType) {
                produitsFiltres.add(p);
            }
        }

        afficherProduits(produitsFiltres);
    }

    @FXML
    private void handleRafraichir() {
        txtRecherche.clear();
        cmbTypeFinancement.setValue("Tous les types");
        chargerDonnees();
    }

    @FXML
    private void handleNouveauFinancement() {
        ouvrirFormulaireDemande();
    }

    // ═══════════════════════════ NAVIGATION ═══════════════════════════

    private void afficherDetailsProduit(ProduitFinancier produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DetailsProduitView.fxml"));
            Parent root = loader.load();

            DetailsProduitController controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Détails — " + produit.getNomProduit());
            stage.setScene(new Scene(root, 700, 500));
            stage.show();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'afficher les détails: " + e.getMessage());
        }
    }

    private void ouvrirFormulaireDemande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DemandeFinancementView.fxml"));
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.setTitle("AgriFund — Nouvelle Demande de Financement");
            popup.setScene(new Scene(root, 1000, 700));
            popup.show();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void ouvrirFormulaireAvecProduit(ProduitFinancier produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DemandeFinancementView.fxml"));
            Parent root = loader.load();

            DemandeFinancementController controller = loader.getController();
            controller.setProduitSelectionne(produit);

            Stage popup = new Stage();
            popup.setTitle("AgriFund — Demande: " + produit.getNomProduit());
            popup.setScene(new Scene(root, 1000, 700));
            popup.show();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void ouvrirFormulaireAvecOffre(OffreFinanciere offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DemandeFinancementView.fxml"));
            Parent root = loader.load();

            DemandeFinancementController controller = loader.getController();
            if (offre != null && offre.getProduitFinancier() != null) {
                controller.setProduitSelectionne(offre.getProduitFinancier());
            }

            Stage popup = new Stage();
            popup.setTitle("AgriFund — Offre: " + offre.getNomOffre());
            popup.setScene(new Scene(root, 1000, 700));
            popup.show();
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
