package Controllers.admin;

import Controllers.MainApp;
import Services.AgriculteurService;
import Services.DocumentService;
import Utils.SessionManager;
import entities.Agriculteur;
import entities.Document;

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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAgricultureController {

    @FXML private VBox agriculteursList;
    @FXML private TextField searchField;
    @FXML private Label statsLabel;
    @FXML private Button btnAll;
    @FXML private Button btnPending;
    @FXML private Button btnVerified;
    @FXML private Button btnRejected;

    private AgriculteurService agriculteurService;
    private DocumentService documentService;
    private List<Agriculteur> allAgriculteurs;
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        try {
            agriculteurService = new AgriculteurService();
            documentService = new DocumentService();
            loadAgriculteurs();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAgriculteurs() throws SQLException {
        allAgriculteurs = agriculteurService.afficherTous();
        applyFilter();
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
            Label empty = new Label("Aucun agriculteur trouvé");
            empty.setStyle("-fx-text-fill: #848A86; -fx-font-size: 16px; -fx-padding: 50;");
            agriculteursList.getChildren().add(empty);
        }
    }

    private VBox createAgriculteurCard(Agriculteur agri) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 20;");

        // Header avec photo et infos principales
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(70);
        avatar.setFitHeight(70);

        Image image = loadImage(agri.getPhoto());
        avatar.setImage(image);
        Circle clip = new Circle(35, 35, 35);
        avatar.setClip(clip);

        // Infos
        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(agri.getPrenom() + " " + agri.getNom());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label emailLabel = new Label("📧 " + agri.getEmail());
        emailLabel.setStyle("-fx-text-fill: #848A86;");

        Label telLabel = new Label("📱 " + (agri.getTel() != null ? agri.getTel() : "Non renseigné"));
        telLabel.setStyle("-fx-text-fill: #848A86;");

        info.getChildren().addAll(nameLabel, emailLabel, telLabel);

        // Badge de statut
        Label statusBadge = new Label();
        if (agri.isCompteVerifie()) {
            statusBadge.setText("✓ Vérifié");
            statusBadge.getStyleClass().addAll("badge", "badge-success");
        } else if ("refuse".equals(agri.getStatusCompte())) {
            statusBadge.setText("✗ Refusé");
            statusBadge.getStyleClass().addAll("badge", "badge-danger");
        } else {
            statusBadge.setText("⏳ En attente");
            statusBadge.getStyleClass().addAll("badge", "badge-warning");
        }

        header.getChildren().addAll(avatar, info, statusBadge);

        // Détails de la ferme
        HBox details = new HBox(30);
        details.setStyle("-fx-padding: 15 0; -fx-border-color: #e0e0e0 transparent; -fx-border-width: 1 0;");

        VBox fermeInfo = new VBox(5);
        fermeInfo.getChildren().addAll(
                new Label("🏠 Adresse: " + agri.getAdresseFerme()),
                new Label("📏 Superficie: " + agri.getSuperficieFerme() + " hectares"),
                new Label("🌱 Culture: " + agri.getTypeCulture())
        );
        fermeInfo.getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #060806;");
            }
        });

        VBox dateInfo = new VBox(5);
        String dateStr = agri.getDateInscrit() != null ?
                agri.getDateInscrit().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
        dateInfo.getChildren().add(new Label("📅 Inscrit le: " + dateStr));
        dateInfo.getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #848A86;");
            }
        });

        HBox.setHgrow(fermeInfo, Priority.ALWAYS);
        details.getChildren().addAll(fermeInfo, dateInfo);

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnView = new Button("👁️ Voir détails");
        btnView.getStyleClass().addAll("btn", "btn-secondary");
        btnView.setOnAction(e -> viewAgriculteur(agri));

        Button btnDocs = new Button("📄 Documents");
        btnDocs.getStyleClass().addAll("btn");
        btnDocs.setStyle("-fx-background-color: #476C1A; -fx-text-fill: white;");
        btnDocs.setOnAction(e -> viewDocuments(agri));

        if (!agri.isCompteVerifie() && !"refuse".equals(agri.getStatusCompte())) {
            Button btnVerify = new Button("✓ Vérifier");
            btnVerify.getStyleClass().addAll("btn", "btn-success");
            btnVerify.setOnAction(e -> verifyAccount(agri, true));

            Button btnReject = new Button("✗ Refuser");
            btnReject.getStyleClass().addAll("btn", "btn-danger");
            btnReject.setOnAction(e -> verifyAccount(agri, false));

            actions.getChildren().addAll(btnView, btnDocs, btnVerify, btnReject);
        } else {
            Button btnMessage = new Button("💬 Message");
            btnMessage.getStyleClass().addAll("btn", "btn-primary");
            btnMessage.setOnAction(e -> sendMessage(agri));

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
                image = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
            } catch (Exception e) {}
        }

        return image;
    }

    private void viewAgriculteur(Agriculteur agri) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'agriculteur");
        alert.setHeaderText(agri.getPrenom() + " " + agri.getNom());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Photo
        ImageView avatar = new ImageView(loadImage(agri.getPhoto()));
        avatar.setFitWidth(100);
        avatar.setFitHeight(100);
        Circle clip = new Circle(50, 50, 50);
        avatar.setClip(clip);

        content.getChildren().addAll(
                avatar,
                new Separator(),
                new Label("📧 Email: " + agri.getEmail()),
                new Label("📱 Téléphone: " + (agri.getTel() != null ? agri.getTel() : "Non renseigné")),
                new Separator(),
                new Label("🏠 Adresse ferme: " + agri.getAdresseFerme()),
                new Label("📏 Superficie: " + agri.getSuperficieFerme() + " hectares"),
                new Label("🌱 Type de culture: " + agri.getTypeCulture()),
                new Separator(),
                new Label("📋 Statut: " + agri.getStatusCompte()),
                new Label("✓ Vérifié: " + (agri.isCompteVerifie() ? "Oui" : "Non"))
        );

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(400);
        alert.showAndWait();
    }

    private void viewDocuments(Agriculteur agri) {
        try {
            List<Document> docs = documentService.afficherParUtilisateur(agri.getId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Documents de l'agriculteur");
            alert.setHeaderText("Documents de " + agri.getPrenom() + " " + agri.getNom());

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

    private void verifyAccount(Agriculteur agri, boolean verify) {
        String action = verify ? "vérifier" : "refuser";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Voulez-vous " + action + " ce compte ?");
        confirm.setContentText(agri.getPrenom() + " " + agri.getNom());

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                agriculteurService.verifierCompte(agri.getAgriculteurId(), verify);
                loadAgriculteurs();

                String message = verify ? "Compte vérifié avec succès!" : "Compte refusé.";
                showAlert(message, Alert.AlertType.INFORMATION);

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void sendMessage(Agriculteur agri) {
        // Rediriger vers la messagerie avec cet utilisateur
        // TODO: Implémenter avec passage de paramètre
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
                "-fx-background-color: #089647; -fx-text-fill: white;" : "");
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
            loadAgriculteurs();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadPage(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile + ".fxml"));
            Stage stage = MainApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}