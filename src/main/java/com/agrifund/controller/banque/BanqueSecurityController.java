package com.agrifund.controller.banque;

import com.agrifund.services.TwoFactorAuthService;
import com.agrifund.util.SessionManager;
import com.agrifund.util.DatabaseConnection;
import com.agrifund.entities.Parametres2FA;
import com.agrifund.entities.Code2FA;
import com.agrifund.entities.Utilisateur;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BanqueSecurityController {

    @FXML private HBox statusBadge;
    @FXML private VBox twoFAStatus;
    @FXML private Button toggle2FAButton;
    @FXML private Button test2FAButton;

    @FXML private TableView<LoginHistory> historyTable;
    @FXML private TableColumn<LoginHistory, String> colDate;
    @FXML private TableColumn<LoginHistory, String> colStatus;
    @FXML private TableColumn<LoginHistory, String> colMethod;

    private TwoFactorAuthService twoFAService;
    private int currentUserId;
    private boolean is2FAActive = false;

    @FXML
    public void initialize() {
        try {
            twoFAService = new TwoFactorAuthService();
            currentUserId = SessionManager.getInstance().getUtilisateurConnecte().getId();

            setupTable();
            loadSecurityStatus();
            loadHistory();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupTable() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colMethod.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMethod()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.contains("Réussie")) {
                        badge.getStyleClass().addAll("badge", "badge-success");
                    } else {
                        badge.getStyleClass().addAll("badge", "badge-danger");
                    }
                    setGraphic(badge);
                }
            }
        });
    }

    private void loadSecurityStatus() throws SQLException {
        Parametres2FA params = twoFAService.getParametres(currentUserId);
        is2FAActive = params != null && params.isEstActive();

        statusBadge.getChildren().clear();
        Label badge = new Label(is2FAActive ? "✓ Activée" : "✗ Désactivée");
        badge.getStyleClass().addAll("badge", is2FAActive ? "badge-success" : "badge-danger");
        statusBadge.getChildren().add(badge);

        twoFAStatus.getChildren().clear();

        if (is2FAActive) {
            Label info = new Label("🔐 La 2FA est activée - Votre compte est sécurisé");
            info.setStyle("-fx-text-fill: #089647; -fx-font-size: 14px;");

            Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
            Label emailLabel = new Label("📧 Les codes sont envoyés à: " + user.getEmail());
            emailLabel.setStyle("-fx-text-fill: #848A86;");

            twoFAStatus.getChildren().addAll(info, emailLabel);

            toggle2FAButton.setText("🔓 Désactiver la 2FA");
            toggle2FAButton.getStyleClass().removeAll("btn-accent");
            toggle2FAButton.getStyleClass().add("btn-danger");

            test2FAButton.setVisible(true);

        } else {
            Label info = new Label("⚠️ La 2FA n'est pas activée");
            info.setStyle("-fx-text-fill: #E1B323; -fx-font-size: 14px;");

            Label warning = new Label("Pour un établissement bancaire, l'activation de la 2FA est fortement recommandée.");
            warning.setStyle("-fx-text-fill: #848A86;");
            warning.setWrapText(true);

            twoFAStatus.getChildren().addAll(info, warning);

            toggle2FAButton.setText("🔒 Activer la 2FA");
            toggle2FAButton.getStyleClass().removeAll("btn-danger");
            toggle2FAButton.getStyleClass().add("btn-accent");

            test2FAButton.setVisible(false);
        }
    }

    @FXML
    public void toggle2FA() {
        try {
            if (is2FAActive) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation");
                confirm.setHeaderText("Désactiver l'authentification à deux facteurs ?");
                confirm.setContentText("Cette action est déconseillée pour un établissement bancaire.");

                if (confirm.showAndWait().get() == ButtonType.OK) {
                    Parametres2FA params = twoFAService.getParametres(currentUserId);
                    if (params != null) {
                        params.setEstActive(false);
                        twoFAService.sauvegarderParametres(params);
                    }
                    loadSecurityStatus();
                    showAlert("2FA désactivée", Alert.AlertType.INFORMATION);
                }
            } else {
                Parametres2FA params = twoFAService.getParametres(currentUserId);
                if (params == null) {
                    params = new Parametres2FA(currentUserId);
                }
                params.setEstActive(true);
                params.setMethodePreferee("email");
                twoFAService.sauvegarderParametres(params);

                loadSecurityStatus();
                showAlert("2FA activée! Votre compte est maintenant sécurisé.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void test2FA() {
        try {
            Code2FA code = twoFAService.envoyerCode2FA(currentUserId);

            if (code != null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Test 2FA");
                dialog.setHeaderText("Code envoyé à votre email");
                dialog.setContentText("Entrez le code:");

                Optional<String> result = dialog.showAndWait();

                result.ifPresent(codeEntre -> {
                    try {
                        if (twoFAService.verifierCode(currentUserId, codeEntre)) {
                            showAlert("✓ Code vérifié! La 2FA fonctionne.", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("✗ Code incorrect", Alert.AlertType.ERROR);
                        }
                    } catch (SQLException e) {
                        showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            }
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadHistory() {
        ObservableList<LoginHistory> history = FXCollections.observableArrayList();

        String sql = "SELECT date_connexion, connexion_reussie, methode_auth " +
                "FROM HistoriqueConnexion WHERE utilisateur_id = ? " +
                "ORDER BY date_connexion DESC LIMIT 15";

        try (PreparedStatement pst = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pst.setInt(1, currentUserId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String date = rs.getTimestamp("date_connexion")
                        .toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                boolean success = rs.getBoolean("connexion_reussie");
                String status = success ? "✓ Réussie" : "✗ Échouée";

                String method = rs.getString("methode_auth");
                String methodDisplay = "";
                switch (method) {
                    case "2fa": methodDisplay = "🔐 2FA"; break;
                    case "password": methodDisplay = "🔑 Mot de passe"; break;
                    default: methodDisplay = method;
                }

                history.add(new LoginHistory(date, status, methodDisplay));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        historyTable.setItems(history);
    }

    @FXML
    public void refreshHistory() {
        loadHistory();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class LoginHistory {
        private final String date;
        private final String status;
        private final String method;

        public LoginHistory(String date, String status, String method) {
            this.date = date;
            this.status = status;
            this.method = method;
        }

        public String getDate() { return date; }
        public String getStatus() { return status; }
        public String getMethod() { return method; }
    }
}
