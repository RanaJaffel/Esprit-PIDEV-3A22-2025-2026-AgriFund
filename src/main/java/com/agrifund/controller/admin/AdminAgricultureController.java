package com.agrifund.controller.admin;

import com.agrifund.Main;
import com.agrifund.services.AgriculteurService;
import com.agrifund.services.DocumentService;
import com.agrifund.util.SessionManager;
import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Document;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAgricultureController {

    @FXML private VBox agriculteursList;
    @FXML private TextField searchField;
    @FXML private Label statsLabel;
    @FXML private Label dateLabel;
    @FXML private Label totalCountLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label verifiedCountLabel;
    @FXML private Label rejectedCountLabel;
    @FXML private Button btnAll;
    @FXML private Button btnPending;
    @FXML private Button btnVerified;
    @FXML private Button btnRejected;
    @FXML private HBox statsCardsContainer;

    private AgriculteurService agriculteurService;
    private DocumentService documentService;
    private List<Agriculteur> allAgriculteurs;
    private String currentFilter = "all";


    @FXML private HBox searchContainer;

    // Dans la méthode initialize(), ajoutez ce code après les autres initialisations :
    @FXML
    public void initialize() {
        try {
            agriculteurService = new AgriculteurService();
            documentService = new DocumentService();

            // Set current date
            if (dateLabel != null) {
                dateLabel.setText(LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new java.util.Locale("fr", "FR"))
                ));
            }

            // Gestion du focus pour la barre de recherche
            setupSearchFocusListener();

            loadAgriculteurs();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSearchFocusListener() {
        if (searchField != null && searchContainer != null) {
            searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    // Focus gained
                    searchContainer.setStyle(
                            "-fx-background-color: #0a0f0a; " +
                                    "-fx-background-radius: 12; " +
                                    "-fx-padding: 0 15; " +
                                    "-fx-border-color: #089647; " +
                                    "-fx-border-radius: 12; " +
                                    "-fx-border-width: 1; " +
                                    "-fx-pref-width: 300;"
                    );
                } else {
                    // Focus lost
                    searchContainer.setStyle(
                            "-fx-background-color: #0a0f0a; " +
                                    "-fx-background-radius: 12; " +
                                    "-fx-padding: 0 15; " +
                                    "-fx-border-color: #2a352a; " +
                                    "-fx-border-radius: 12; " +
                                    "-fx-border-width: 1; " +
                                    "-fx-pref-width: 300;"
                    );
                }
            });
        }
    }

    private void loadAgriculteurs() throws SQLException {
        allAgriculteurs = agriculteurService.afficherTous();
        updateStatsCards();
        applyFilter();
    }

    private void updateStatsCards() {
        int total = allAgriculteurs.size();
        int pending = (int) allAgriculteurs.stream()
                .filter(a -> !a.isCompteVerifie() && !"refuse".equals(a.getStatusCompte())).count();
        int verified = (int) allAgriculteurs.stream()
                .filter(Agriculteur::isCompteVerifie).count();
        int rejected = (int) allAgriculteurs.stream()
                .filter(a -> "refuse".equals(a.getStatusCompte())).count();

        if (totalCountLabel != null) totalCountLabel.setText(String.valueOf(total));
        if (pendingCountLabel != null) pendingCountLabel.setText(String.valueOf(pending));
        if (verifiedCountLabel != null) verifiedCountLabel.setText(String.valueOf(verified));
        if (rejectedCountLabel != null) rejectedCountLabel.setText(String.valueOf(rejected));
    }

    private void applyFilter() {
        agriculteursList.getChildren().clear();

        List<Agriculteur> filtered = allAgriculteurs.stream()
                .filter(a -> {
                    switch (currentFilter) {
                        case "pending":
                            return !a.isCompteVerifie() && !"refuse".equals(a.getStatusCompte());
                        case "verified":
                            return a.isCompteVerifie();
                        case "rejected":
                            return "refuse".equals(a.getStatusCompte());
                        default:
                            return true;
                    }
                })
                .filter(a -> {
                    String search = searchField.getText().toLowerCase().trim();
                    if (search.isEmpty()) return true;

                    String fullName = (a.getPrenom() + " " + a.getNom()).toLowerCase();
                    String email = a.getEmail().toLowerCase();
                    return fullName.contains(search) || email.contains(search);
                })
                .collect(Collectors.toList());

        statsLabel.setText(filtered.size() + " agriculteur(s)");

        for (Agriculteur agri : filtered) {
            VBox card = createAgriculteurCard(agri);
            agriculteursList.getChildren().add(card);
        }

        if (filtered.isEmpty()) {
            VBox emptyState = createEmptyState();
            agriculteursList.getChildren().add(emptyState);
        }
    }

    private VBox createEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));

        Label emptyIcon = new Label("🔍");
        emptyIcon.getStyleClass().add("empty-icon");
        emptyIcon.setStyle("-fx-font-size: 60px; -fx-opacity: 0.5;");

        Label emptyTitle = new Label("Aucun agriculteur trouvé");
        emptyTitle.getStyleClass().add("empty-title");
        emptyTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #5a605a;");

        Label emptySubtitle = new Label("Essayez de modifier vos critères de recherche");
        emptySubtitle.getStyleClass().add("empty-subtitle");
        emptySubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #3a403a;");

        emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptySubtitle);
        return emptyState;
    }

    private VBox createAgriculteurCard(Agriculteur agri) {
        VBox card = new VBox(0);
        card.getStyleClass().add("agriculteur-card");

        // ========== HEADER SECTION ==========
        HBox header = new HBox(20);
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar with border
        StackPane avatarContainer = new StackPane();
        avatarContainer.getStyleClass().add("avatar-container");

        ImageView avatar = new ImageView();
        avatar.setFitWidth(70);
        avatar.setFitHeight(70);
        avatar.setPreserveRatio(false);
        avatar.setSmooth(true);

        Image image = loadImage(agri.getPhoto());
        avatar.setImage(image);

        // Clip circulaire
        Circle clip = new Circle(35, 35, 35);
        avatar.setClip(clip);

        // Bordure circulaire
        Circle border = new Circle(38);
        border.getStyleClass().add("avatar-border");
        border.setStroke(Color.web("#089647"));
        border.setStrokeWidth(3);
        border.setFill(Color.TRANSPARENT);

        avatarContainer.getChildren().addAll(border, avatar);

        // Info Section
        VBox infoSection = new VBox(6);
        infoSection.getStyleClass().add("info-section");
        HBox.setHgrow(infoSection, Priority.ALWAYS);

        Label nameLabel = new Label(agri.getPrenom() + " " + agri.getNom());
        nameLabel.getStyleClass().add("name-label");
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        HBox emailRow = new HBox(8);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        Label emailIcon = new Label("📧");
        emailIcon.setStyle("-fx-font-size: 12px;");
        Label emailLabel = new Label(agri.getEmail());
        emailLabel.getStyleClass().add("email-label");
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #848A86;");
        emailRow.getChildren().addAll(emailIcon, emailLabel);

        HBox telRow = new HBox(8);
        telRow.setAlignment(Pos.CENTER_LEFT);
        Label telIcon = new Label("📱");
        telIcon.setStyle("-fx-font-size: 12px;");
        Label telLabel = new Label(agri.getTel() != null ? agri.getTel() : "Non renseigné");
        telLabel.getStyleClass().add("tel-label");
        telLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #848A86;");
        telRow.getChildren().addAll(telIcon, telLabel);

        infoSection.getChildren().addAll(nameLabel, emailRow, telRow);

        // Status Badge
        Label statusBadge = createStatusBadge(agri);

        header.getChildren().addAll(avatarContainer, infoSection, statusBadge);

        // ========== DETAILS SECTION ==========
        HBox details = new HBox(40);
        details.getStyleClass().add("card-details");
        details.setPadding(new Insets(20, 0, 20, 0));
        details.setStyle("-fx-border-color: transparent transparent #2a352a transparent; -fx-border-width: 0 0 1 0;");

        // Colonne Ferme
        VBox fermeColumn = new VBox(12);
        fermeColumn.getStyleClass().add("details-column");
        HBox.setHgrow(fermeColumn, Priority.ALWAYS);

        fermeColumn.getChildren().addAll(
                createDetailItem("🏠", "Adresse", agri.getAdresseFerme()),
                createDetailItem("📏", "Superficie", agri.getSuperficieFerme() + " hectares"),
                createDetailItem("🌱", "Culture", agri.getTypeCulture())
        );

        // Colonne Date
        VBox dateColumn = new VBox(12);
        dateColumn.getStyleClass().add("details-column");
        dateColumn.setAlignment(Pos.TOP_RIGHT);

        String dateStr = agri.getDateInscrit() != null ?
                agri.getDateInscrit().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "N/A";
        String timeStr = agri.getDateInscrit() != null ?
                agri.getDateInscrit().format(DateTimeFormatter.ofPattern("HH:mm")) : "";

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);

        Label dateTitle = new Label("📅 Date d'inscription");
        dateTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #5a605a;");

        Label dateValue = new Label(dateStr);
        dateValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label timeValue = new Label(timeStr);
        timeValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #848A86;");

        dateBox.getChildren().addAll(dateTitle, dateValue, timeValue);
        dateColumn.getChildren().add(dateBox);

        details.getChildren().addAll(fermeColumn, dateColumn);

        // ========== ACTIONS SECTION ==========
        HBox actions = new HBox(12);
        actions.getStyleClass().add("card-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(15, 0, 0, 0));

        Button btnView = createActionButton("👁️ Détails", "btn-view");
        btnView.setOnAction(e -> viewAgriculteur(agri));

        Button btnDocs = createActionButton("📄 Documents", "btn-docs");
        btnDocs.setOnAction(e -> viewDocuments(agri));

        actions.getChildren().addAll(btnView, btnDocs);

        if (!agri.isCompteVerifie() && !"refuse".equals(agri.getStatusCompte())) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnVerify = createActionButton("✓ Vérifier", "btn-verify");
            btnVerify.setOnAction(e -> verifyAccount(agri, true));

            Button btnReject = createActionButton("✗ Refuser", "btn-reject");
            btnReject.setOnAction(e -> verifyAccount(agri, false));

            actions.getChildren().addAll(spacer, btnVerify, btnReject);
        } else {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnMessage = createActionButton("💬 Message", "btn-message");
            btnMessage.setOnAction(e -> sendMessage(agri));

            actions.getChildren().addAll(spacer, btnMessage);
        }

        card.getChildren().addAll(header, details, actions);
        return card;
    }

    private Label createStatusBadge(Agriculteur agri) {
        Label badge = new Label();
        badge.getStyleClass().add("status-badge");
        badge.setPadding(new Insets(8, 16, 8, 16));

        if (agri.isCompteVerifie()) {
            badge.setText("✓ Vérifié");
            badge.getStyleClass().add("badge-verified");
            badge.setStyle("-fx-background-color: linear-gradient(to right, rgba(8, 150, 71, 0.2), rgba(7, 106, 57, 0.2)); " +
                    "-fx-text-fill: #B2D944; -fx-background-radius: 20; -fx-border-color: #089647; -fx-border-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else if ("refuse".equals(agri.getStatusCompte())) {
            badge.setText("✗ Refusé");
            badge.getStyleClass().add("badge-rejected");
            badge.setStyle("-fx-background-color: linear-gradient(to right, rgba(220, 53, 69, 0.2), rgba(200, 35, 51, 0.2)); " +
                    "-fx-text-fill: #ff6b7a; -fx-background-radius: 20; -fx-border-color: #dc3545; -fx-border-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            badge.setText("⏳ En attente");
            badge.getStyleClass().add("badge-pending");
            badge.setStyle("-fx-background-color: linear-gradient(to right, rgba(225, 179, 35, 0.2), rgba(201, 157, 30, 0.2)); " +
                    "-fx-text-fill: #E1B323; -fx-background-radius: 20; -fx-border-color: #E1B323; -fx-border-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;");
        }

        return badge;
    }

    private HBox createDetailItem(String icon, String label, String value) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("detail-item");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px; -fx-min-width: 25;");

        VBox textBox = new VBox(2);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #5a605a; -fx-font-size: 11px;");

        Label valueText = new Label(value != null && !value.isEmpty() ? value : "Non renseigné");
        valueText.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: 500;");

        textBox.getChildren().addAll(labelText, valueText);
        item.getChildren().addAll(iconLabel, textBox);

        return item;
    }

    private Button createActionButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("action-btn", styleClass);
        btn.setPadding(new Insets(10, 20, 10, 20));

        switch (styleClass) {
            case "btn-view":
                btn.setStyle("-fx-background-color: #1a201a; -fx-text-fill: #a8b0a8; " +
                        "-fx-background-radius: 10; -fx-border-color: #2a352a; -fx-border-radius: 10; " +
                        "-fx-font-size: 12px; -fx-font-weight: 600; -fx-cursor: hand;");
                break;
            case "btn-docs":
                btn.setStyle("-fx-background-color: linear-gradient(to right, #476C1A, #3a5a15); " +
                        "-fx-text-fill: #ffffff; -fx-background-radius: 10; " +
                        "-fx-font-size: 12px; -fx-font-weight: 600; -fx-cursor: hand;");
                break;
            case "btn-verify":
                btn.setStyle("-fx-background-color: linear-gradient(to right, #089647, #076A39); " +
                        "-fx-text-fill: #ffffff; -fx-background-radius: 10; " +
                        "-fx-font-size: 12px; -fx-font-weight: 600; -fx-cursor: hand;");
                break;
            case "btn-reject":
                btn.setStyle("-fx-background-color: linear-gradient(to right, #dc3545, #c82333); " +
                        "-fx-text-fill: #ffffff; -fx-background-radius: 10; " +
                        "-fx-font-size: 12px; -fx-font-weight: 600; -fx-cursor: hand;");
                break;
            case "btn-message":
                btn.setStyle("-fx-background-color: linear-gradient(to right, #E1B323, #c99d1e); " +
                        "-fx-text-fill: #0a0f0a; -fx-background-radius: 10; " +
                        "-fx-font-size: 12px; -fx-font-weight: 600; -fx-cursor: hand;");
                break;
        }

        return btn;
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

    private void viewAgriculteur(Agriculteur agri) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'agriculteur");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #111611; -fx-border-color: #2a352a; -fx-border-radius: 16; -fx-background-radius: 16;");
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #111611;");

        // Header with photo
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView avatar = new ImageView(loadImage(agri.getPhoto()));
        avatar.setFitWidth(100);
        avatar.setFitHeight(100);
        Circle clip = new Circle(50, 50, 50);
        avatar.setClip(clip);

        VBox headerInfo = new VBox(8);
        Label nameLabel = new Label(agri.getPrenom() + " " + agri.getNom());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label statusLabel = createStatusBadge(agri);

        headerInfo.getChildren().addAll(nameLabel, statusLabel);
        header.getChildren().addAll(avatar, headerInfo);

        // Separator
        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #2a352a;");

        // Contact Info
        VBox contactSection = createInfoSection("📞 Informations de contact",
                "📧 Email: " + agri.getEmail(),
                "📱 Téléphone: " + (agri.getTel() != null ? agri.getTel() : "Non renseigné")
        );

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #2a352a;");

        // Farm Info
        VBox farmSection = createInfoSection("🌾 Informations de la ferme",
                "🏠 Adresse: " + agri.getAdresseFerme(),
                "📏 Superficie: " + agri.getSuperficieFerme() + " hectares",
                "🌱 Type de culture: " + agri.getTypeCulture()
        );

        Separator sep3 = new Separator();
        sep3.setStyle("-fx-background-color: #2a352a;");

        // Account Info
        VBox accountSection = createInfoSection("⚙️ Informations du compte",
                "📋 Statut: " + agri.getStatusCompte(),
                "✓ Vérifié: " + (agri.isCompteVerifie() ? "Oui" : "Non")
        );

        content.getChildren().addAll(header, sep1, contactSection, sep2, farmSection, sep3, accountSection);

        dialogPane.setContent(content);
        dialogPane.setPrefWidth(500);

        dialog.showAndWait();
    }

    private VBox createInfoSection(String title, String... items) {
        VBox section = new VBox(12);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #B2D944;");
        section.getChildren().add(titleLabel);

        for (String item : items) {
            Label itemLabel = new Label(item);
            itemLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #a8b0a8;");
            section.getChildren().add(itemLabel);
        }

        return section;
    }

    private void viewDocuments(Agriculteur agri) {
        try {
            List<Document> docs = documentService.afficherParUtilisateur(agri.getId());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Documents de l'agriculteur");
            dialog.setHeaderText(null);

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #111611; -fx-border-color: #2a352a; -fx-border-radius: 16; -fx-background-radius: 16;");
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);

            VBox content = new VBox(15);
            content.setPadding(new Insets(25));
            content.setStyle("-fx-background-color: #111611;");

            // Header
            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);

            Label headerIcon = new Label("📄");
            headerIcon.setStyle("-fx-font-size: 28px;");

            VBox headerText = new VBox(4);
            Label headerTitle = new Label("Documents de " + agri.getPrenom() + " " + agri.getNom());
            headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Label headerSubtitle = new Label(docs.size() + " document(s) trouvé(s)");
            headerSubtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #848A86;");
            headerText.getChildren().addAll(headerTitle, headerSubtitle);

            header.getChildren().addAll(headerIcon, headerText);
            content.getChildren().add(header);

            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #2a352a;");
            content.getChildren().add(sep);

            if (docs.isEmpty()) {
                VBox emptyState = new VBox(10);
                emptyState.setAlignment(Pos.CENTER);
                emptyState.setPadding(new Insets(40));

                Label emptyIcon = new Label("📭");
                emptyIcon.setStyle("-fx-font-size: 40px; -fx-opacity: 0.5;");

                Label emptyText = new Label("Aucun document uploadé");
                emptyText.setStyle("-fx-font-size: 14px; -fx-text-fill: #5a605a;");

                emptyState.getChildren().addAll(emptyIcon, emptyText);
                content.getChildren().add(emptyState);
            } else {
                VBox docsList = new VBox(10);

                for (Document doc : docs) {
                    HBox docRow = new HBox(15);
                    docRow.setAlignment(Pos.CENTER_LEFT);
                    docRow.setPadding(new Insets(15));
                    docRow.setStyle("-fx-background-color: #1a201a; -fx-background-radius: 12; -fx-border-color: #2a352a; -fx-border-radius: 12;");

                    Label icon = new Label("📄");
                    icon.setStyle("-fx-font-size: 24px;");

                    VBox docInfo = new VBox(4);
                    HBox.setHgrow(docInfo, Priority.ALWAYS);

                    Label docName = new Label(doc.getNom());
                    docName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

                    HBox docMeta = new HBox(15);
                    Label docType = new Label("Type: " + doc.getTypeDocument());
                    docType.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");

                    Label docStatus = new Label("Statut: " + doc.getStatut());
                    docStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: " +
                            (doc.getStatut().equals("validé") ? "#B2D944" : "#E1B323") + ";");

                    docMeta.getChildren().addAll(docType, docStatus);
                    docInfo.getChildren().addAll(docName, docMeta);

                    docRow.getChildren().addAll(icon, docInfo);
                    docsList.getChildren().add(docRow);
                }

                content.getChildren().add(docsList);
            }

            dialogPane.setContent(content);
            dialogPane.setPrefWidth(550);

            dialog.showAndWait();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void verifyAccount(Agriculteur agri, boolean verify) {
        String action = verify ? "vérifier" : "refuser";
        String actionColor = verify ? "#089647" : "#dc3545";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Voulez-vous " + action + " ce compte ?");
        confirm.setContentText(agri.getPrenom() + " " + agri.getNom());

        // Style the dialog
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #111611;");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                agriculteurService.verifierCompte(agri.getAgriculteurId(), verify);
                loadAgriculteurs();

                String message = verify ? "✓ Compte vérifié avec succès!" : "✗ Compte refusé.";
                showStyledAlert(message, verify ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void sendMessage(Agriculteur agri) {
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
        // Reset all buttons
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #848A86; " +
                "-fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10 20; " +
                "-fx-background-radius: 10; -fx-cursor: hand;";

        btnAll.setStyle(defaultStyle);
        btnPending.setStyle(defaultStyle);
        btnVerified.setStyle(defaultStyle);
        btnRejected.setStyle(defaultStyle);

        // Apply active style
        String activeBase = "-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: 600; " +
                "-fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;";

        switch (currentFilter) {
            case "all":
                btnAll.setStyle("-fx-background-color: linear-gradient(to right, #089647, #076A39); " + activeBase);
                break;
            case "pending":
                btnPending.setStyle("-fx-background-color: linear-gradient(to right, #E1B323, #c99d1e); " +
                        "-fx-text-fill: #0a0f0a; -fx-font-size: 13px; -fx-font-weight: 600; " +
                        "-fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand;");
                break;
            case "verified":
                btnVerified.setStyle("-fx-background-color: linear-gradient(to right, #089647, #076A39); " + activeBase);
                break;
            case "rejected":
                btnRejected.setStyle("-fx-background-color: linear-gradient(to right, #dc3545, #c82333); " + activeBase);
                break;
        }
    }

    @FXML
    public void handleSearch() {
        applyFilter();
    }

    @FXML
    public void refreshList() {
        try {
            searchField.clear();
            loadAgriculteurs();
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

    private void showStyledAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #111611; -fx-border-color: #2a352a; -fx-border-radius: 12;");

        alert.showAndWait();
    }
}