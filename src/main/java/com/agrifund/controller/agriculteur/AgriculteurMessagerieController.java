package com.agrifund.controller.agriculteur;

import com.agrifund.Main;
import com.agrifund.services.MessagerieService;
import com.agrifund.services.UtilisateurService;
import com.agrifund.services.AuthService;
import com.agrifund.util.SessionManager;
import com.agrifund.util.EmojiManager;
import com.agrifund.entities.Conversation;
import com.agrifund.entities.Message;
import com.agrifund.entities.Utilisateur;
import com.agrifund.entities.PieceJointe;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AgriculteurMessagerieController {

    @FXML private VBox conversationsList;
    @FXML private TextField searchConversation;

    @FXML private VBox noConversationPane;
    @FXML private VBox conversationPane;

    @FXML private ImageView chatAvatar;
    @FXML private Label chatUserName;
    @FXML private Label chatUserType;

    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;

    @FXML private TextField messageInput;

    private MessagerieService messagerieService;
    private UtilisateurService utilisateurService;
    private AuthService authService;
    private Conversation currentConversation;
    private int currentUserId;

    @FXML
    public void initialize() {
        try {
            messagerieService = new MessagerieService();
            utilisateurService = new UtilisateurService();
            authService = new AuthService();
            currentUserId = SessionManager.getInstance().getUtilisateurConnecte().getId();

            loadConversations();

            Circle clip = new Circle(22.5, 22.5, 22.5);
            chatAvatar.setClip(clip);

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadConversations() throws SQLException {
        conversationsList.getChildren().clear();

        List<Conversation> conversations = messagerieService.listerConversations(currentUserId);

        if (conversations.isEmpty()) {
            VBox empty = new VBox(15);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(30));

            Label text = new Label("Aucune conversation");
            text.setStyle("-fx-text-fill: #848A86;");

            Label hint = new Label("Contactez une banque pour commencer");
            hint.setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px;");

            empty.getChildren().addAll(text, hint);
            conversationsList.getChildren().add(empty);
            return;
        }

        for (Conversation conv : conversations) {
            HBox convItem = createConversationItem(conv);
            conversationsList.getChildren().add(convItem);
        }
    }

    private HBox createConversationItem(Conversation conv) throws SQLException {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        item.getStyleClass().add("conversation-item");

        int correspondantId = conv.getCorrespondantId(currentUserId);
        Utilisateur correspondant = utilisateurService.rechercherParId(correspondantId);

        // Avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(50);
        avatar.setFitHeight(50);
        Image image = loadImage(correspondant != null ? correspondant.getPhoto() : null);
        avatar.setImage(image);
        Circle clip = new Circle(25, 25, 25);
        avatar.setClip(clip);

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        String name = correspondant != null ?
                correspondant.getPrenom() + " " + correspondant.getNom() : "Utilisateur";
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Type d'utilisateur
        String userType = "";
        try {
            String type = authService.getTypeUtilisateur(correspondantId);
            if ("BANQUE".equals(type)) {
                userType = "🏦 Banque";
            } else if ("ADMIN".equals(type)) {
                userType = "👨‍💼 Admin";
            }
        } catch (Exception e) {}

        Label typeLabel = new Label(userType);
        typeLabel.setStyle("-fx-text-fill: #E1B323; -fx-font-size: 11px;");

        Label previewLabel = new Label(conv.getDernierMessage() != null ?
                conv.getDernierMessage() : "Aucun message");
        previewLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px;");
        previewLabel.setMaxWidth(180);

        info.getChildren().addAll(nameLabel, typeLabel, previewLabel);

        // Time & Badge
        VBox rightInfo = new VBox(5);
        rightInfo.setAlignment(Pos.TOP_RIGHT);

        String timeStr = conv.getDerniereActivite() != null ?
                conv.getDerniereActivite().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 11px;");

        rightInfo.getChildren().add(timeLabel);

        if (conv.getNbMessagesNonLus() > 0) {
            Label badge = new Label(String.valueOf(conv.getNbMessagesNonLus()));
            badge.getStyleClass().add("conversation-unread-badge");
            rightInfo.getChildren().add(badge);
        }

        item.getChildren().addAll(avatar, info, rightInfo);

        item.setOnMouseClicked(e -> {
            try {
                openConversation(conv);
                conversationsList.getChildren().forEach(node ->
                        node.getStyleClass().remove("conversation-item-active"));
                item.getStyleClass().add("conversation-item-active");
            } catch (SQLException ex) {
                showAlert("Erreur: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        return item;
    }

    private void openConversation(Conversation conv) throws SQLException {
        currentConversation = conv;

        noConversationPane.setVisible(false);
        noConversationPane.setManaged(false);
        conversationPane.setVisible(true);
        conversationPane.setManaged(true);

        int correspondantId = conv.getCorrespondantId(currentUserId);
        Utilisateur correspondant = utilisateurService.rechercherParId(correspondantId);

        if (correspondant != null) {
            chatUserName.setText(correspondant.getPrenom() + " " + correspondant.getNom());
            chatAvatar.setImage(loadImage(correspondant.getPhoto()));

            try {
                String type = authService.getTypeUtilisateur(correspondantId);
                if ("BANQUE".equals(type)) {
                    chatUserType.setText("🏦 Banque");
                } else if ("ADMIN".equals(type)) {
                    chatUserType.setText("👨‍💼 Administrateur");
                } else {
                    chatUserType.setText("");
                }
            } catch (Exception e) {
                chatUserType.setText("");
            }
        }

        loadMessages();
    }

    private void loadMessages() throws SQLException {
        messagesContainer.getChildren().clear();

        List<Message> messages = messagerieService.listerMessages(
                currentConversation.getId(), currentUserId);

        String lastDate = "";

        for (Message msg : messages) {
            String msgDate = msg.getDateEnvoi().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            if (!msgDate.equals(lastDate)) {
                Label dateLabel = new Label(msgDate);
                dateLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px; -fx-padding: 10 0;");
                HBox dateBox = new HBox(dateLabel);
                dateBox.setAlignment(Pos.CENTER);
                messagesContainer.getChildren().add(dateBox);
                lastDate = msgDate;
            }

            HBox msgBox = createMessageBubble(msg);
            messagesContainer.getChildren().add(msgBox);
        }

        Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
    }

    private HBox createMessageBubble(Message msg) throws SQLException {
        boolean isMine = msg.getExpediteurId() == currentUserId;

        HBox container = new HBox();
        container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        container.setPadding(new Insets(2, 10, 2, 10));

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(400);
        bubble.setStyle("-fx-padding: 10 15; -fx-background-radius: 15;");

        if (isMine) {
            bubble.setStyle(bubble.getStyle() + "-fx-background-color: #089647;");

            // Menu contextuel pour mes messages
            ContextMenu contextMenu = createMessageContextMenu(msg);
            bubble.setOnContextMenuRequested(e ->
                    contextMenu.show(bubble, e.getScreenX(), e.getScreenY())
            );

        } else {
            bubble.setStyle(bubble.getStyle() + "-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

            Utilisateur expediteur = utilisateurService.rechercherParId(msg.getExpediteurId());
            if (expediteur != null) {
                ImageView miniAvatar = new ImageView(loadImage(expediteur.getPhoto()));
                miniAvatar.setFitWidth(25);
                miniAvatar.setFitHeight(25);
                Circle clip = new Circle(12.5, 12.5, 12.5);
                miniAvatar.setClip(clip);

                HBox msgWithAvatar = new HBox(10);
                msgWithAvatar.setAlignment(Pos.BOTTOM_LEFT);
                msgWithAvatar.getChildren().addAll(miniAvatar, bubble);
                container.getChildren().add(msgWithAvatar);

                Label content = new Label(msg.getContenu());
                content.setWrapText(true);
                content.setStyle("-fx-text-fill: #060806;");

                String time = msg.getDateEnvoi().format(DateTimeFormatter.ofPattern("HH:mm"));
                String modifiedIndicator = msg.estModifie() ? " (modifié)" : "";
                Label timeLabel = new Label(time + modifiedIndicator);
                timeLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 10px;");

                bubble.getChildren().addAll(content, timeLabel);

                if (msg.isaPieceJointe()) {
                    addAttachments(bubble, msg, false);
                }

                return container;
            }
        }

        Label content = new Label(msg.getContenu());
        content.setWrapText(true);
        content.setStyle(isMine ? "-fx-text-fill: white;" : "-fx-text-fill: #060806;");

        String time = msg.getDateEnvoi().format(DateTimeFormatter.ofPattern("HH:mm"));
        String modifiedIndicator = msg.estModifie() ? " (modifié)" : "";
        Label timeLabel = new Label(time + modifiedIndicator);
        timeLabel.setStyle(isMine ?
                "-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 10px;" :
                "-fx-text-fill: #848A86; -fx-font-size: 10px;");

        bubble.getChildren().addAll(content, timeLabel);

        if (msg.isaPieceJointe()) {
            addAttachments(bubble, msg, isMine);
        }

        container.getChildren().add(bubble);
        return container;
    }

    private ContextMenu createMessageContextMenu(Message msg) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("✏️ Modifier");
        editItem.setOnAction(e -> showEditMessageDialog(msg));

        MenuItem deleteItem = new MenuItem("🗑️ Supprimer");
        deleteItem.setOnAction(e -> showDeleteMessageConfirmation(msg));

        MenuItem copyItem = new MenuItem("📋 Copier");
        copyItem.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent contentClip = new javafx.scene.input.ClipboardContent();
            contentClip.putString(msg.getContenu());
            clipboard.setContent(contentClip);
            showNotification("Message copié", "success");
        });

        contextMenu.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(), copyItem);

        return contextMenu;
    }

    private void showEditMessageDialog(Message message) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modifier le message");
        dialog.setHeaderText("Modifier votre message");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextArea textArea = new TextArea(message.getContenu());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(4);
        textArea.setPrefWidth(400);

        Label charCount = new Label(message.getContenu().length() + " caractères");
        charCount.setStyle("-fx-text-fill: #848A86; -fx-font-size: 11px;");

        textArea.textProperty().addListener((obs, oldVal, newVal) ->
                charCount.setText(newVal.length() + " caractères")
        );

        content.getChildren().addAll(new Label("Message:"), textArea, charCount);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            try {
                if (!newContent.trim().isEmpty() && !newContent.equals(message.getContenu())) {
                    String contentWithEmojis = EmojiManager.convertirEmojis(newContent.trim());
                    boolean success = messagerieService.modifierMessage(
                            message.getId(),
                            currentUserId,
                            contentWithEmojis
                    );

                    if (success) {
                        loadMessages();
                        loadConversations();
                        showNotification("✓ Message modifié", "success");
                    } else {
                        showAlert("Impossible de modifier ce message", Alert.AlertType.WARNING);
                    }
                }
            } catch (SQLException e) {
                showAlert("Erreur lors de la modification: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void showDeleteMessageConfirmation(Message message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer le message");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce message ?");
        alert.setContentText("\"" + message.getContenu().substring(0, Math.min(50, message.getContenu().length())) +
                (message.getContenu().length() > 50 ? "..." : "") + "\"");

        ButtonType deleteButton = new ButtonType("🗑️ Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButton, cancelButton);

        Button deleteBtn = (Button) alert.getDialogPane().lookupButton(deleteButton);
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            try {
                boolean success = messagerieService.supprimerMessage(message.getId(), currentUserId);

                if (success) {
                    loadMessages();
                    loadConversations();
                    showNotification("✓ Message supprimé", "info");
                } else {
                    showAlert("Impossible de supprimer ce message", Alert.AlertType.WARNING);
                }
            } catch (SQLException e) {
                showAlert("Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showNotification(String message, String type) {
        Label notification = new Label(message);
        notification.setPadding(new Insets(10, 20, 10, 20));

        String bgColor;
        switch (type) {
            case "success":
                bgColor = "#089647";
                break;
            case "info":
                bgColor = "#2196F3";
                break;
            case "warning":
                bgColor = "#FF9800";
                break;
            case "error":
                bgColor = "#e74c3c";
                break;
            default:
                bgColor = "#333";
        }

        notification.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-font-weight: bold;");

        HBox notificationBox = new HBox(notification);
        notificationBox.setAlignment(Pos.CENTER);
        notificationBox.setPadding(new Insets(10));

        if (conversationPane != null && conversationPane.getChildren().size() > 0) {
            conversationPane.getChildren().add(1, notificationBox);

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> conversationPane.getChildren().remove(notificationBox));
            pause.play();
        }
    }

    private void addAttachments(VBox bubble, Message msg, boolean isMine) {
        try {
            List<PieceJointe> pieces = messagerieService.listerPiecesJointes(msg.getId());

            for (PieceJointe pj : pieces) {
                HBox attachBox = new HBox(10);
                attachBox.setAlignment(Pos.CENTER_LEFT);
                attachBox.setStyle("-fx-padding: 8; -fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 8; -fx-cursor: hand;");

                Label icon = new Label(pj.getIcone());
                icon.setStyle("-fx-font-size: 20px;");

                VBox attachInfo = new VBox(2);
                Label fileName = new Label(pj.getNomOriginal());
                fileName.setStyle("-fx-font-size: 12px;" + (isMine ? "-fx-text-fill: white;" : ""));
                Label fileSize = new Label(pj.getTailleFormatee());
                fileSize.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (isMine ? "rgba(255,255,255,0.7);" : "#848A86;"));
                attachInfo.getChildren().addAll(fileName, fileSize);

                attachBox.getChildren().addAll(icon, attachInfo);

                attachBox.setOnMouseClicked(e -> {
                    try {
                        File file = new File(pj.getCheminFichier());
                        if (file.exists()) {
                            java.awt.Desktop.getDesktop().open(file);
                        }
                    } catch (Exception ex) {
                        showAlert("Impossible d'ouvrir le fichier", Alert.AlertType.ERROR);
                    }
                });

                bubble.getChildren().add(attachBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendMessage() {
        String content = messageInput.getText().trim();

        if (content.isEmpty() || currentConversation == null) {
            return;
        }

        try {
            String contentWithEmojis = EmojiManager.convertirEmojis(content);

            Message msg = messagerieService.envoyerMessage(
                    currentConversation.getId(),
                    currentUserId,
                    contentWithEmojis
            );

            if (msg != null) {
                messageInput.clear();
                loadMessages();
                loadConversations();
            }

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void attachFile() {
        if (currentConversation == null) {
            showAlert("Veuillez d'abord sélectionner une conversation", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File file = fileChooser.showOpenDialog(Main.getPrimaryStage());

        if (file != null) {
            try {
                String fileName = file.getName();
                Message msg = messagerieService.envoyerMessage(
                        currentConversation.getId(),
                        currentUserId,
                        "📎 " + fileName
                );

                if (msg != null) {
                    String type = com.agrifund.util.MessagerieFileManager.determinerTypeFichier(fileName);
                    String storedPath = null;

                    switch (type) {
                        case "image":
                            storedPath = com.agrifund.util.MessagerieFileManager.uploadImage(file.getAbsolutePath(), msg.getId());
                            break;
                        case "document":
                            storedPath = com.agrifund.util.MessagerieFileManager.uploadDocument(file.getAbsolutePath(), msg.getId());
                            break;
                        default:
                            storedPath = com.agrifund.util.MessagerieFileManager.uploadDocument(file.getAbsolutePath(), msg.getId());
                    }

                    if (storedPath != null) {
                        messagerieService.ajouterPieceJointe(msg.getId(), storedPath, fileName);
                    }

                    loadMessages();
                    loadConversations();
                }

            } catch (Exception e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void showEmojiPicker() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Emojis");
        alert.setHeaderText("Cliquez pour insérer");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        String[] emojis = {"😊", "😂", "❤️", "👍", "👎", "🎉", "🔥", "✅", "❌", "⭐",
                "🌾", "🚜", "🏦", "💰", "📄", "📧", "🌱", "☀️", "🌧️", "🙏"};

        HBox row1 = new HBox(10);
        HBox row2 = new HBox(10);

        for (int i = 0; i < emojis.length; i++) {
            String emoji = emojis[i];
            Button btn = new Button(emoji);
            btn.setStyle("-fx-font-size: 20px; -fx-background-color: transparent; -fx-cursor: hand;");
            btn.setOnAction(e -> {
                messageInput.setText(messageInput.getText() + emoji);
                alert.close();
            });

            if (i < 10) row1.getChildren().add(btn);
            else row2.getChildren().add(btn);
        }

        content.getChildren().addAll(row1, row2);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    @FXML
    public void showNewConversationDialog() {
        try {
            List<Utilisateur> users = utilisateurService.afficherTous();

            List<String> choices = users.stream()
                    .filter(u -> u.getId() != currentUserId)
                    .filter(u -> {
                        try {
                            String type = authService.getTypeUtilisateur(u.getId());
                            return "BANQUE".equals(type) || "ADMIN".equals(type);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .map(u -> {
                        String type = "";
                        try {
                            type = authService.getTypeUtilisateur(u.getId());
                            type = "BANQUE".equals(type) ? "🏦" : "👨‍💼";
                        } catch (Exception e) {}
                        return u.getId() + " - " + type + " " + u.getPrenom() + " " + u.getNom();
                    })
                    .collect(Collectors.toList());

            if (choices.isEmpty()) {
                showAlert("Aucun contact disponible pour le moment", Alert.AlertType.INFORMATION);
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
            dialog.setTitle("Nouvelle conversation");
            dialog.setHeaderText("Contacter une banque ou un administrateur");
            dialog.setContentText("Sélectionnez:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(selection -> {
                try {
                    int userId = Integer.parseInt(selection.split(" - ")[0]);
                    Conversation conv = messagerieService.creerOuRecupererConversation(currentUserId, userId);
                    loadConversations();
                    openConversation(conv);
                } catch (SQLException e) {
                    showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void searchConversations() {
        String query = searchConversation.getText().toLowerCase().trim();

        conversationsList.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox item = (HBox) node;
                if (item.getChildren().size() > 1) {
                    VBox info = (VBox) item.getChildren().get(1);
                    if (!info.getChildren().isEmpty()) {
                        Label nameLabel = (Label) info.getChildren().get(0);
                        boolean matches = nameLabel.getText().toLowerCase().contains(query);
                        item.setVisible(matches);
                        item.setManaged(matches);
                    }
                }
            }
        });
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

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}