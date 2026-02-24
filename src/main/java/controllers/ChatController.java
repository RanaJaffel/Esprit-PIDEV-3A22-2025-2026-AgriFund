package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ChatController {

    @FXML public ScrollPane scrollPane;
    @FXML public VBox messagesContainer;
    @FXML public TextField tfChatMessage;
    @FXML public Button btnChatSend;
    @FXML public Label lblStatusChat;
    @FXML public Button btnCloseChat;
}