package com.agrifund.controller.agriculteur;

import com.agrifund.Main;
import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.OffreFinanciereService;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

public class AgriculteurOffreController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cmbStatut;
    @FXML private FlowPane containerOffres;
    @FXML private Label lblNombreOffres;
    @FXML private Label lblStatus;

    // Stats
    @FXML private Label lblTotalOffres;
    @FXML private Label lblActives;
    @FXML private Label lblEnPause;
    @FXML private Label lblExpirees;

    private OffreFinanciereService offreService;
    private ObservableList<OffreFinanciere> toutesLesOffres;

    @FXML
    public void initialize() {
        offreService = new OffreFinanciereService();

        // Statuts
        List<String> statuts = Arrays.asList("Tous les statuts", "Active", "En pause", "Expiree");
        cmbStatut.setItems(FXCollections.observableArrayList(statuts));
        cmbStatut.setValue("Tous les statuts");

        chargerDonnees();
    }

    private void chargerDonnees() {
        lblStatus.setText("Chargement...");

        new Thread(() -> {
            try {
                toutesLesOffres = offreService.getAllOffresAvecProduit();

                Platform.runLater(() -> {
                    mettreAJourStats();
                    afficherOffres(toutesLesOffres);
                    lblStatus.setText("✓ " + toutesLesOffres.size() + " offres disponibles");
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
        int total = toutesLesOffres.size();
        int actives = 0, enPause = 0, expirees = 0;

        for (OffreFinanciere o : toutesLesOffres) {
            String statut = o.getStatut();
            if (statut != null) {
                switch (statut) {
                    case "Active" -> actives++;
                    case "En pause" -> enPause++;
                    case "Expiree" -> expirees++;
                }
            }
        }

        lblTotalOffres.setText(String.valueOf(total));
        lblActives.setText(String.valueOf(actives));
        lblEnPause.setText(String.valueOf(enPause));
        lblExpirees.setText(String.valueOf(expirees));
    }

    private void afficherOffres(ObservableList<OffreFinanciere> offres) {
        containerOffres.getChildren().clear();

        if (offres.isEmpty()) {
            Label lblVide = new Label("🔍 Aucune offre trouvée");
            lblVide.setStyle("-fx-text-fill: #666; -fx-font-size: 16; -fx-padding: 40;");
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
        card.setPrefWidth(320);
        card.setMinWidth(290);
        card.setMaxWidth(350);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #2a1a05, #1e1608);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: rgba(255,183,77,0.15);" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 1;" +
                        "-fx-cursor: hand;"
        );
        card.setEffect(new DropShadow(16, Color.rgb(255, 152, 0, 0.15)));

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 20 22 12 22;");

        StackPane giftCircle = new StackPane();
        giftCircle.setPrefSize(48, 48);
        giftCircle.setMinSize(48, 48);
        giftCircle.setStyle("-fx-background-color: rgba(255,183,77,0.15); -fx-background-radius: 50;");
        Label giftIcon = new Label("🎁");
        giftIcon.setStyle("-fx-font-size: 22;");
        giftCircle.getChildren().add(giftIcon);

        VBox nameBox = new VBox(3);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label lblNom = new Label(offre.getNomOffre());
        lblNom.setStyle("-fx-text-fill: #ffb74d; -fx-font-size: 15; -fx-font-weight: bold;");
        lblNom.setWrapText(true);

        // Statut badge
        String statut = offre.getStatut();
        String statutColor = switch (statut) {
            case "Active" -> "#66bb6a";
            case "En pause" -> "#ffa726";
            default -> "#ef5350";
        };
        Label lblStatut = new Label("● " + statut);
        lblStatut.setStyle("-fx-text-fill: " + statutColor + "; -fx-font-size: 11; -fx-font-weight: bold;");

        nameBox.getChildren().addAll(lblNom, lblStatut);
        header.getChildren().addAll(giftCircle, nameBox);

        // Body
        VBox body = new VBox(10);
        body.setStyle("-fx-padding: 0 22 20 22;");

        // Produit associé
        if (offre.getProduitFinancier() != null || offre.getNomProduit() != null) {
            HBox prodRef = new HBox(8);
            prodRef.setAlignment(Pos.CENTER_LEFT);
            prodRef.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 10 14; -fx-background-radius: 10;");
            Label prodIcon = new Label("📦");
            prodIcon.setStyle("-fx-font-size: 14;");

            VBox prodInfo = new VBox(2);
            String produitNom = offre.getNomProduit() != null ? offre.getNomProduit() :
                    (offre.getProduitFinancier() != null ? offre.getProduitFinancier().getNomProduit() : "N/A");
            Label lblProd = new Label(produitNom);
            lblProd.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12; -fx-font-weight: bold;");

            if (offre.getProduitFinancier() != null) {
                Label lblTaux = new Label("Taux: " + String.format("%.2f%%", offre.getProduitFinancier().getTauxInteret()));
                lblTaux.setStyle("-fx-text-fill: #B2D944; -fx-font-size: 10;");
                prodInfo.getChildren().addAll(lblProd, lblTaux);
            } else {
                prodInfo.getChildren().add(lblProd);
            }

            prodRef.getChildren().addAll(prodIcon, prodInfo);
            body.getChildren().add(prodRef);
        }

        // Banque
        Label lblBanque = new Label("🏦 " + (offre.getNomBanque() != null ? offre.getNomBanque() : "Banque partenaire"));
        lblBanque.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 10;");

        // Conditions
        String conditions = offre.getConditions();
        Label lblConditions = new Label(conditions != null && !conditions.isEmpty() ?
                (conditions.length() > 80 ? conditions.substring(0, 80) + "..." : conditions) :
                "Conditions disponibles sur demande");
        lblConditions.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 11;");
        lblConditions.setWrapText(true);
        lblConditions.setMaxHeight(40);

        // Bouton
        Button btnProfiter = new Button("Profiter de cette offre →");
        btnProfiter.setMaxWidth(Double.MAX_VALUE);
        btnProfiter.setStyle(
                "-fx-background-color: linear-gradient(to right, #e65100, #ff8f00);" +
                        "-fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-padding: 12 20; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-size: 12;"
        );
        btnProfiter.setOnAction(e -> ouvrirFormulaireDemande(offre));

        body.getChildren().addAll(lblBanque, lblConditions, btnProfiter);

        card.getChildren().addAll(header, body);

        // Hover effects
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
            card.setEffect(new DropShadow(24, Color.rgb(255, 152, 0, 0.3)));
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            card.setEffect(new DropShadow(16, Color.rgb(255, 152, 0, 0.15)));
        });

        return card;
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
        String statutSelectionne = cmbStatut.getValue();

        ObservableList<OffreFinanciere> offresFiltrees = FXCollections.observableArrayList();

        for (OffreFinanciere o : toutesLesOffres) {
            boolean matchRecherche = recherche.isEmpty()
                    || o.getNomOffre().toLowerCase().contains(recherche)
                    || (o.getConditions() != null && o.getConditions().toLowerCase().contains(recherche))
                    || (o.getNomProduit() != null && o.getNomProduit().toLowerCase().contains(recherche))
                    || (o.getNomBanque() != null && o.getNomBanque().toLowerCase().contains(recherche));

            boolean matchStatut = statutSelectionne == null
                    || statutSelectionne.equals("Tous les statuts")
                    || o.getStatut().equalsIgnoreCase(statutSelectionne);

            if (matchRecherche && matchStatut) {
                offresFiltrees.add(o);
            }
        }

        afficherOffres(offresFiltrees);
        lblStatus.setText("🔍 " + offresFiltrees.size() + " résultat(s)");
    }

    @FXML
    private void handleRafraichir() {
        txtRecherche.clear();
        cmbStatut.setValue("Tous les statuts");
        chargerDonnees();
    }

    @FXML
    private void handleFiltrerActives() {
        cmbStatut.setValue("Active");
        handleRechercher();
    }

    private void ouvrirFormulaireDemande(OffreFinanciere offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DemandeFinancementView.fxml"));
            Parent root = loader.load();

            com.agrifund.controller.DemandeFinancementController controller = loader.getController();
            if (offre != null && offre.getProduitFinancier() != null) {
                controller.setProduitSelectionne(offre.getProduitFinancier());
            }

            Stage popup = new Stage();
            popup.setTitle("AgriFund — Offre: " + offre.getNomOffre());
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