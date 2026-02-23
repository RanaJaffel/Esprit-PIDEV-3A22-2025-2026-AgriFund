package com.agrifund.controller;

import java.util.Arrays;
import java.util.List;

import com.agrifund.model.OffreFinanciere;
import com.agrifund.model.ProduitFinancier;
import com.agrifund.services.OffreFinanciereService;
import com.agrifund.services.ProduitFinancierService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AccueilUtilisateurController {

    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> cmbTypeFinancement;
    @FXML private VBox containerProduits;
    @FXML private VBox containerOffres;
    @FXML private Label lblNombreProduits;
    @FXML private Label lblNombreOffres;
    @FXML private Button btnNouveauFinancement;

    private ProduitFinancierService produitService;
    private OffreFinanciereService offreService;
    
    private ObservableList<ProduitFinancier> tousLesProduits;
    private ObservableList<OffreFinanciere> toutesLesOffres;

    @FXML
    public void initialize() {
        produitService = new ProduitFinancierService();
        offreService = new OffreFinanciereService();

        // Initialiser le ComboBox des types
        List<String> types = Arrays.asList(
            "Tous les types",
            "Crédit",
            "Prêt",
            "Leasing",
            "Subvention",
            "Microfinance"
        );
        cmbTypeFinancement.setItems(FXCollections.observableArrayList(types));
        cmbTypeFinancement.setValue("Tous les types");

        // Charger les données
        chargerDonnees();
    }

    /**
     * Charge tous les produits et offres depuis la base de données
     */
    private void chargerDonnees() {
        // Charger en arrière-plan pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                tousLesProduits = produitService.getAllProduits();
                toutesLesOffres = offreService.getAllOffresAvecProduit();

                Platform.runLater(() -> {
                    afficherProduits(tousLesProduits);
                    afficherOffres(toutesLesOffres);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Erreur de chargement", 
                             "Impossible de charger les données: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Affiche les produits financiers dans l'interface
     */
    private void afficherProduits(ObservableList<ProduitFinancier> produits) {
        containerProduits.getChildren().clear();

        if (produits.isEmpty()) {
            Label lblVide = new Label("Aucun produit disponible pour le moment.");
            lblVide.setStyle("-fx-text-fill: #999; -fx-font-size: 14;");
            containerProduits.getChildren().add(lblVide);
            lblNombreProduits.setText("(0 produits)");
            return;
        }

        lblNombreProduits.setText("(" + produits.size() + " produits)");

        for (ProduitFinancier produit : produits) {
            VBox card = creerCarteProduit(produit);
            containerProduits.getChildren().add(card);
        }
    }

    /**
     * Crée une carte visuelle pour un produit
     */
    private VBox creerCarteProduit(ProduitFinancier produit) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-padding: 20; " +
                     "-fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        // Header avec nom et type
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label lblNom = new Label(produit.getNomProduit());
        lblNom.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #076A39;");
        
        Label lblType = new Label(produit.getTypeFinancement());
        lblType.setStyle("-fx-background-color: #B2D944; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 5 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-font-size: 12; " +
                        "-fx-font-weight: bold;");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label lblTaux = new Label(String.format("%.2f%% /an", produit.getTauxInteret()));
        lblTaux.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #089647;");
        
        header.getChildren().addAll(lblNom, lblType, spacer, lblTaux);

        // Détails
        VBox details = new VBox(8);
        
        Label lblMontant = new Label(String.format("💰 Montant: %.0f - %.0f MAD", 
                                                   produit.getMontantMin(), 
                                                   produit.getMontantMax()));
        lblMontant.setStyle("-fx-font-size: 14; -fx-text-fill: #555;");
        
        Label lblRegles = new Label("📋 " + produit.getReglesFinancieres());
        lblRegles.setStyle("-fx-font-size: 13; -fx-text-fill: #777;");
        lblRegles.setWrapText(true);
        
        details.getChildren().addAll(lblMontant, lblRegles);

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnDetails = new Button("Détails");
        btnDetails.setStyle("-fx-background-color: #e0e0e0; " +
                           "-fx-text-fill: #333; " +
                           "-fx-padding: 8 20; " +
                           "-fx-background-radius: 5; " +
                           "-fx-cursor: hand;");
        btnDetails.setOnAction(e -> afficherDetailsProduit(produit));
        
        Button btnDemander = new Button("Demander ce produit");
        btnDemander.setStyle("-fx-background-color: #089647; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8 20; " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand;");
        btnDemander.setOnAction(e -> ouvrirFormulaireAvecProduit(produit));
        
        actions.getChildren().addAll(btnDetails, btnDemander);

        card.getChildren().addAll(header, new Separator(), details, actions);
        
        return card;
    }

    /**
     * Affiche les offres spéciales
     */
    private void afficherOffres(ObservableList<OffreFinanciere> offres) {
        containerOffres.getChildren().clear();

        if (offres.isEmpty()) {
            Label lblVide = new Label("Aucune offre spéciale pour le moment.");
            lblVide.setStyle("-fx-text-fill: #999; -fx-font-size: 14;");
            containerOffres.getChildren().add(lblVide);
            lblNombreOffres.setText("(0 offres)");
            return;
        }

        lblNombreOffres.setText("(" + offres.size() + " offres)");

        for (OffreFinanciere offre : offres) {
            VBox card = creerCarteOffre(offre);
            containerOffres.getChildren().add(card);
        }
    }

    /**
     * Crée une carte visuelle pour une offre
     */
    private VBox creerCarteOffre(OffreFinanciere offre) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: linear-gradient(to right, #FFF9C4, #FFFDE7); " +
                     "-fx-padding: 20; " +
                     "-fx-background-radius: 10; " +
                     "-fx-border-color: #F9A825; " +
                     "-fx-border-width: 2; " +
                     "-fx-border-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(249,168,37,0.3), 10, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label lblNom = new Label("🎁 " + offre.getNomOffre());
        lblNom.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #F57F17;");
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label lblStatut = new Label(offre.getStatut());
        lblStatut.setStyle("-fx-background-color: #4CAF50; " +
                          "-fx-text-fill: white; " +
                          "-fx-padding: 5 15; " +
                          "-fx-background-radius: 15; " +
                          "-fx-font-size: 12; " +
                          "-fx-font-weight: bold;");
        
        header.getChildren().addAll(lblNom, spacer, lblStatut);

        // Détails
        VBox details = new VBox(8);
        
        if (offre.getProduitFinancier() != null) {
            Label lblProduit = new Label("Produit: " + offre.getProduitFinancier().getNomProduit());
            lblProduit.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");
            details.getChildren().add(lblProduit);
        }
        
        Label lblConditions = new Label(offre.getConditions());
        lblConditions.setStyle("-fx-font-size: 13; -fx-text-fill: #555;");
        lblConditions.setWrapText(true);
        
        details.getChildren().add(lblConditions);

        // Bouton d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnProfiter = new Button("Profiter de cette offre");
        btnProfiter.setStyle("-fx-background-color: #F57F17; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8 20; " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand;");
        btnProfiter.setOnAction(e -> ouvrirFormulaireAvecOffre(offre));
        
        actions.getChildren().add(btnProfiter);

        card.getChildren().addAll(header, new Separator(), details, actions);
        
        return card;
    }

    @FXML
    private void handleRechercher() {
        String recherche = txtRecherche.getText().trim().toLowerCase();
        String typeSelectionne = cmbTypeFinancement.getValue();

        ObservableList<ProduitFinancier> produitsFiltres = FXCollections.observableArrayList();
        
        for (ProduitFinancier p : tousLesProduits) {
            boolean matchRecherche = recherche.isEmpty() || 
                                    p.getNomProduit().toLowerCase().contains(recherche) ||
                                    p.getTypeFinancement().toLowerCase().contains(recherche) ||
                                    p.getReglesFinancieres().toLowerCase().contains(recherche);
            
            boolean matchType = typeSelectionne.equals("Tous les types") || 
                               p.getTypeFinancement().equalsIgnoreCase(typeSelectionne);
            
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

    private void afficherDetailsProduit(ProduitFinancier produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DetailsProduitView.fxml"));
            Parent root = loader.load();
            
            DetailsProduitController controller = loader.getController();
            controller.setProduit(produit);
            
            Stage stage = new Stage();
            stage.setTitle("Détails - " + produit.getNomProduit());
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
            
            Stage stage = (Stage) btnNouveauFinancement.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
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
            
            Stage stage = (Stage) containerProduits.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void ouvrirFormulaireAvecOffre(OffreFinanciere offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/view/DemandeFinancementView.fxml"));
            Parent root = loader.load();
            
            DemandeFinancementController controller = loader.getController();
            // Pass the product from the offer
            if (offre != null && offre.getProduitFinancier() != null) {
                controller.setProduitSelectionne(offre.getProduitFinancier());
            }
            
            Stage stage = (Stage) containerOffres.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
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
