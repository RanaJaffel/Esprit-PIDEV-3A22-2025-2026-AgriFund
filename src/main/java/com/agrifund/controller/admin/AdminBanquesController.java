package com.agrifund.controller.admin;

import com.agrifund.Main;
import com.agrifund.services.BanqueService;
import com.agrifund.services.DocumentService;
import com.agrifund.entities.Banque;
import com.agrifund.entities.Document;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminBanquesController {

    @FXML private VBox banquesList;
    @FXML private TextField searchField;
    @FXML private Label statsLabel;
    @FXML private Button btnAll;
    @FXML private Button btnPending;
    @FXML private Button btnVerified;
    @FXML private Button btnRejected;

    private BanqueService banqueService;
    private DocumentService documentService;
    private List<Banque> allBanques;
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        try {
            banqueService = new BanqueService();
            documentService = new DocumentService();
            loadBanques();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadBanques() throws SQLException {
        allBanques = banqueService.afficherTous();
        applyFilter();
    }

    private void applyFilter() {
        banquesList.getChildren().clear();

        List<Banque> filtered = allBanques.stream()
                .filter(b -> {
                    switch (currentFilter) {
                        case "pending":
                            return !b.isCompteVerifie() && !"refuse".equals(b.getStatusCompte());
                        case "verified":
                            return b.isCompteVerifie();
                        case "rejected":
                            return "refuse".equals(b.getStatusCompte());
                        default:
                            return true;
                    }
                })
                .filter(b -> {
                    String search = searchField.getText().toLowerCase().trim();
                    if (search.isEmpty()) return true;

                    String name = b.getNom().toLowerCase();
                    String email = b.getEmail().toLowerCase();
                    String code = b.getCodeBanque().toLowerCase();
                    return name.contains(search) || email.contains(search) || code.contains(search);
                })
                .collect(Collectors.toList());

        statsLabel.setText(filtered.size() + " banque(s)");

        for (Banque banque : filtered) {
            VBox card = createBanqueCard(banque);
            banquesList.getChildren().add(card);
        }

        if (filtered.isEmpty()) {
            Label empty = new Label("Aucune banque trouvée");
            empty.setStyle("-fx-text-fill: #848A86; -fx-font-size: 16px; -fx-padding: 50;");
            banquesList.getChildren().add(empty);
        }
    }

    private VBox createBanqueCard(Banque banque) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 20; -fx-border-left: 4px solid #E1B323;");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo/Avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(70);
        avatar.setFitHeight(70);

        Image image = loadImage(banque.getPhoto() != null ? banque.getPhoto() : banque.getLogo());
        avatar.setImage(image);
        avatar.setStyle("-fx-background-radius: 10;");

        // Infos
        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(banque.getNom());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;-fx-text-fill: #E1B323");
        Label codeLabel = new Label("[" + banque.getCodeBanque() + "]");
        codeLabel.setStyle("-fx-text-fill: #E1B323; -fx-font-weight: bold;");
        nameRow.getChildren().addAll(nameLabel, codeLabel);

        Label emailLabel = new Label("📧 " + banque.getEmail());
        emailLabel.setStyle("-fx-text-fill: #848A86;");

        Label representantLabel = new Label("👔 Représentant: " + banque.getRepresentantLegal());
        representantLabel.setStyle("-fx-text-fill: #060806;");

        info.getChildren().addAll(nameRow, emailLabel, representantLabel);

        // Badge de statut
        Label statusBadge = new Label();
        if (banque.isCompteVerifie()) {
            statusBadge.setText("✓ Vérifiée");
            statusBadge.getStyleClass().addAll("badge", "badge-success");
        } else if ("refuse".equals(banque.getStatusCompte())) {
            statusBadge.setText("✗ Refusée");
            statusBadge.getStyleClass().addAll("badge", "badge-danger");
        } else {
            statusBadge.setText("⏳ En attente");
            statusBadge.getStyleClass().addAll("badge", "badge-warning");
        }

        header.getChildren().addAll(avatar, info, statusBadge);

        // Détails
        HBox details = new HBox(30);
        details.setStyle("-fx-padding: 15 0; -fx-border-color: #e0e0e0 transparent; -fx-border-width: 1 0;");

        VBox addressInfo = new VBox(5);
        addressInfo.getChildren().addAll(
                new Label("🏛️ Siège: " + banque.getAddresseSiege()),
                new Label("🏪 Agence: " + (banque.getAdresseAgence() != null ? banque.getAdresseAgence() : "Non renseignée")),
                new Label("🌐 Site web: " + (banque.getSiteWeb() != null ? banque.getSiteWeb() : "Non renseigné"))
        );
        addressInfo.getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #060806;");
            }
        });

        VBox dateInfo = new VBox(5);
        String dateStr = banque.getDateInscrit() != null ?
                banque.getDateInscrit().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
        dateInfo.getChildren().add(new Label("📅 Inscrite le: " + dateStr));
        dateInfo.getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #848A86;");
            }
        });

        HBox.setHgrow(addressInfo, Priority.ALWAYS);
        details.getChildren().addAll(addressInfo, dateInfo);

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnView = new Button("👁️ Voir détails");
        btnView.getStyleClass().addAll("btn", "btn-secondary");
        btnView.setOnAction(e -> viewBanque(banque));

        Button btnDocs = new Button("📄 Documents");
        btnDocs.getStyleClass().addAll("btn");
        btnDocs.setStyle("-fx-background-color: #476C1A; -fx-text-fill: white;");
        btnDocs.setOnAction(e -> viewDocuments(banque));

        if (!banque.isCompteVerifie() && !"refuse".equals(banque.getStatusCompte())) {
            Button btnVerify = new Button("✓ Vérifier");
            btnVerify.getStyleClass().addAll("btn", "btn-success");
            btnVerify.setOnAction(e -> verifyAccount(banque, true));

            Button btnReject = new Button("✗ Refuser");
            btnReject.getStyleClass().addAll("btn", "btn-danger");
            btnReject.setOnAction(e -> verifyAccount(banque, false));

            actions.getChildren().addAll(btnView, btnDocs, btnVerify, btnReject);
        } else {
            Button btnMessage = new Button("💬 Message");
            btnMessage.getStyleClass().addAll("btn", "btn-accent");
            btnMessage.setOnAction(e -> sendMessage(banque));

            actions.getChildren().addAll(btnView, btnDocs, btnMessage);
        }

        card.getChildren().addAll(header, details, actions);
        return card;
    }

    private Image loadImage(String photoPath) {
        Image image = null;

        if (photoPath != null && !photoPath.isEmpty()) {
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                image = new Image(photoFile.toURI().toString());
            }
        }

        if (image == null) {
            try {
                image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-avatar.png"));
            } catch (Exception e) {}
        }

        return image;
    }

    private void viewBanque(Banque banque) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la banque");
        alert.setHeaderText(banque.getNom() + " [" + banque.getCodeBanque() + "]");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        content.getChildren().addAll(
                new Label("📧 Email: " + banque.getEmail()),
                new Label("📱 Téléphone: " + (banque.getTel() != null ? banque.getTel() : "Non renseigné")),
                new Separator(),
                new Label("👔 Représentant légal: " + banque.getRepresentantLegal()),
                new Separator(),
                new Label("🏛️ Siège: " + banque.getAddresseSiege()),
                new Label("🏪 Agence: " + (banque.getAdresseAgence() != null ? banque.getAdresseAgence() : "Non renseignée")),
                new Label("🌐 Site web: " + (banque.getSiteWeb() != null ? banque.getSiteWeb() : "Non renseigné")),
                new Separator(),
                new Label("📋 Statut: " + banque.getStatusCompte()),
                new Label("✓ Vérifiée: " + (banque.isCompteVerifie() ? "Oui" : "Non"))
        );

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(450);
        alert.showAndWait();
    }

    private void viewDocuments(Banque banque) {
        try {
            List<Document> docs = documentService.afficherParUtilisateur(banque.getId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Documents de la banque");
            alert.setHeaderText("Documents de " + banque.getNom());

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            if (docs.isEmpty()) {
                content.getChildren().add(new Label("Aucun document uploadé"));
            } else {
                for (Document doc : docs) {
                    HBox docRow = new HBox(15);
                    docRow.setAlignment(Pos.CENTER_LEFT);
                    docRow.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

                    Label icon = new Label("📄");
                    icon.setStyle("-fx-font-size: 24px;");

                    VBox docInfo = new VBox(3);
                    docInfo.getChildren().addAll(
                            new Label(doc.getNom()),
                            new Label("Type: " + doc.getTypeDocument() + " | Statut: " + doc.getStatut())
                    );
                    docInfo.getChildren().get(1).setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px;");

                    HBox.setHgrow(docInfo, Priority.ALWAYS);
                    docRow.getChildren().addAll(icon, docInfo);
                    content.getChildren().add(docRow);
                }
            }

            alert.getDialogPane().setContent(content);
            alert.getDialogPane().setPrefWidth(500);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void verifyAccount(Banque banque, boolean verify) {
        String action = verify ? "vérifier" : "refuser";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Voulez-vous " + action + " ce compte ?");
        confirm.setContentText(banque.getNom());

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                banqueService.verifierCompte(banque.getBanqueId(), verify);
                loadBanques();

                String message = verify ? "Compte vérifié avec succès!" : "Compte refusé.";
                showAlert(message, Alert.AlertType.INFORMATION);

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void sendMessage(Banque banque) {
        loadPage("admin/admin-messagerie");
    }

    @FXML
    public void filterAll() {
        currentFilter = "all";
        updateFilterButtons();
        applyFilter();
    }

    @FXML
    public void filterPending() {
        currentFilter = "pending";
        updateFilterButtons();
        applyFilter();
    }

    @FXML
    public void filterVerified() {
        currentFilter = "verified";
        updateFilterButtons();
        applyFilter();
    }

    @FXML
    public void filterRejected() {
        currentFilter = "rejected";
        updateFilterButtons();
        applyFilter();
    }

    private void updateFilterButtons() {
        btnAll.setStyle(currentFilter.equals("all") ?
                "-fx-background-color: #E1B323; -fx-text-fill: #060806;" : "");
        btnPending.setStyle(currentFilter.equals("pending") ?
                "-fx-background-color: #E1B323; -fx-text-fill: #060806;" : "");
        btnVerified.setStyle(currentFilter.equals("verified") ?
                "-fx-background-color: #089647; -fx-text-fill: white;" : "");
        btnRejected.setStyle(currentFilter.equals("rejected") ?
                "-fx-background-color: #dc3545; -fx-text-fill: white;" : "");
    }

    @FXML
    public void handleSearch() {
        applyFilter();
    }

    @FXML
    public void refreshList() {
        try {
            searchField.clear();
            loadBanques();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadPage(String fxmlFile) {
        Main.navigateTo("/com/agrifund/fxml/" + fxmlFile + ".fxml");
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
