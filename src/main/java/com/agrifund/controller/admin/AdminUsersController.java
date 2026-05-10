package com.agrifund.controller.admin;

import com.agrifund.Main;
import com.agrifund.services.*;
import com.agrifund.util.SessionManager;
import com.agrifund.entities.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminUsersController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterStatus;
    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, Void> colPhoto;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colTel;
    @FXML private TableColumn<Utilisateur, String> colType;
    @FXML private TableColumn<Utilisateur, String> colDate;
    @FXML private TableColumn<Utilisateur, Void> colActions;
    @FXML private Pagination pagination;

    private UtilisateurService utilisateurService;
    private AuthService authService;
    private ObservableList<Utilisateur> allUsers;
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            authService = new AuthService();

            setupTableColumns();
            loadUsers();
            setupFilters();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupTableColumns() {
        // Photo column
        colPhoto.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Utilisateur, Void> call(TableColumn<Utilisateur, Void> param) {
                return new TableCell<>() {
                    private final ImageView imageView = new ImageView();

                    {
                        imageView.setFitWidth(40);
                        imageView.setFitHeight(40);
                        Circle clip = new Circle(20, 20, 20);
                        imageView.setClip(clip);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            Utilisateur user = getTableView().getItems().get(getIndex());
                            Image image = loadUserImage(user.getPhoto());
                            imageView.setImage(image);
                            setGraphic(imageView);
                        }
                    }
                };
            }
        });

        // ID column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Nom column
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Prenom column
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        // Email column
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Tel column
        colTel.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTel() != null ?
                        cellData.getValue().getTel() : "-"));

        // Type column
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    try {
                        String type = authService.getTypeUtilisateur(user.getId());
                        Label badge = new Label();

                        switch (type) {
                            case "ADMIN":
                                badge.setText("👨‍💼 Admin");
                                badge.getStyleClass().addAll("badge", "badge-info");
                                break;
                            case "AGRICULTEUR":
                                badge.setText("🌾 Agriculteur");
                                badge.getStyleClass().addAll("badge", "badge-success");
                                break;
                            case "BANQUE":
                                badge.setText("🏦 Banque");
                                badge.getStyleClass().addAll("badge", "badge-warning");
                                break;
                            default:
                                badge.setText("Inconnu");
                                badge.getStyleClass().addAll("badge", "badge-pending");
                        }

                        setGraphic(badge);

                    } catch (SQLException e) {
                        setText("?");
                    }
                }
            }
        });

        // Date column
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateInscrit() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getDateInscrit()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new SimpleStringProperty("-");
        });

        // Actions column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("modifier");
            private final Button btnDelete = new Button("supprimer");
            private final Button btnView = new Button("voir");
            private final HBox buttons = new HBox(5, btnView, btnEdit, btnDelete);

            {
                btnView.getStyleClass().addAll("btn", "btn-small");
                btnView.setStyle("-fx-background-color: #476C1A; -fx-text-fill: white;");

                btnEdit.getStyleClass().addAll("btn", "btn-small");
                btnEdit.setStyle("-fx-background-color: #E1B323; -fx-text-fill: #060806;");

                btnDelete.getStyleClass().addAll("btn", "btn-small", "btn-danger");

                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Utilisateur user = getTableView().getItems().get(getIndex());

                    btnView.setOnAction(e -> viewUser(user));
                    btnEdit.setOnAction(e -> editUser(user));
                    btnDelete.setOnAction(e -> deleteUser(user));

                    setGraphic(buttons);
                }
            }
        });
    }

    private Image loadUserImage(String photoPath) {
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
            } catch (Exception e) {
                // Fallback
            }
        }

        return image;
    }

    private void loadUsers() throws SQLException {
        List<Utilisateur> users = utilisateurService.afficherTous();
        allUsers = FXCollections.observableArrayList(users);

        setupPagination();
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) allUsers.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<Utilisateur> createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allUsers.size());

        usersTable.setItems(FXCollections.observableArrayList(
                allUsers.subList(fromIndex, toIndex)
        ));

        return usersTable;
    }

    private void setupFilters() {
        filterType.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> applyFilters()
        );

        filterStatus.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> applyFilters()
        );
    }

    @FXML
    public void handleSearch() {
        applyFilters();
    }

    private void applyFilters() {
        try {
            List<Utilisateur> users = utilisateurService.afficherTous();
            String searchText = searchField.getText().toLowerCase().trim();
            String typeFilter = filterType.getValue();

            List<Utilisateur> filtered = users.stream()
                    .filter(u -> {
                        // Filtre de recherche
                        if (!searchText.isEmpty()) {
                            String fullName = (u.getPrenom() + " " + u.getNom()).toLowerCase();
                            String email = u.getEmail().toLowerCase();

                            if (!fullName.contains(searchText) && !email.contains(searchText)) {
                                return false;
                            }
                        }

                        // Filtre de type
                        if (typeFilter != null && !typeFilter.equals("Tous")) {
                            try {
                                String userType = authService.getTypeUtilisateur(u.getId());
                                switch (typeFilter) {
                                    case "Admin":
                                        if (!userType.equals("ADMIN")) return false;
                                        break;
                                    case "Agriculteur":
                                        if (!userType.equals("AGRICULTEUR")) return false;
                                        break;
                                    case "Banque":
                                        if (!userType.equals("BANQUE")) return false;
                                        break;
                                }
                            } catch (SQLException e) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            allUsers = FXCollections.observableArrayList(filtered);
            setupPagination();

        } catch (SQLException e) {
            showAlert("Erreur lors du filtrage: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void refreshList() {
        try {
            searchField.clear();
            filterType.getSelectionModel().clearSelection();
            filterStatus.getSelectionModel().clearSelection();
            loadUsers();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void viewUser(Utilisateur user) {
        try {
            String type = authService.getTypeUtilisateur(user.getId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails de l'utilisateur");
            alert.setHeaderText(user.getPrenom() + " " + user.getNom());

            VBox content = new VBox(10);
            content.getChildren().addAll(
                    new Label("📧 Email: " + user.getEmail()),
                    new Label("📱 Téléphone: " + (user.getTel() != null ? user.getTel() : "-")),
                    new Label("👤 Type: " + type),
                    new Label("📅 Inscription: " +
                            (user.getDateInscrit() != null ?
                                    user.getDateInscrit().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) :
                                    "-"))
            );

            // Ajouter la photo
            if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
                content.getChildren().add(new Label("📷 Photo: " + user.getPhoto()));
            }

            alert.getDialogPane().setContent(content);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void editUser(Utilisateur user) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText("Modifier " + user.getPrenom() + " " + user.getNom());

        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");

        TextField nomField = new TextField(user.getNom());
        nomField.setPromptText("Nom");

        TextField prenomField = new TextField(user.getPrenom());
        prenomField.setPromptText("Prénom");

        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Email");

        TextField telField = new TextField(user.getTel());
        telField.setPromptText("Téléphone");

        content.getChildren().addAll(
                new Label("Nom:"), nomField,
                new Label("Prénom:"), prenomField,
                new Label("Email:"), emailField,
                new Label("Téléphone:"), telField
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setNom(nomField.getText());
                user.setPrenom(prenomField.getText());
                user.setEmail(emailField.getText());
                user.setTel(telField.getText());
                return user;
            }
            return null;
        });

        Optional<Utilisateur> result = dialog.showAndWait();

        result.ifPresent(updatedUser -> {
            try {
                utilisateurService.modifier(updatedUser);
                loadUsers();
                showAlert("Utilisateur modifié avec succès!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void deleteUser(Utilisateur user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " +
                user.getPrenom() + " " + user.getNom() + " ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                utilisateurService.supprimer(user.getId());
                loadUsers();
                showAlert("Utilisateur supprimé!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void showAddAdminDialog() {
        Dialog<Admin> dialog = new Dialog<>();
        dialog.setTitle("Nouvel Administrateur");
        dialog.setHeaderText("Créer un nouveau compte administrateur");

        ButtonType createButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        TextField telField = new TextField();
        telField.setPromptText("Téléphone");

        content.getChildren().addAll(
                new Label("Nom:"), nomField,
                new Label("Prénom:"), prenomField,
                new Label("Email:"), emailField,
                new Label("Mot de passe:"), passwordField,
                new Label("Téléphone:"), telField
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Admin admin = new Admin(
                        nomField.getText(),
                        prenomField.getText(),
                        emailField.getText(),
                        passwordField.getText()
                );
                admin.setTel(telField.getText());
                return admin;
            }
            return null;
        });

        Optional<Admin> result = dialog.showAndWait();

        result.ifPresent(admin -> {
            try {
                authService.inscrireAdmin(
                        admin.getNom(),
                        admin.getPrenom(),
                        admin.getEmail(),
                        admin.getPassword(),
                        admin.getTel()
                );
                loadUsers();
                showAlert("Administrateur créé avec succès!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
