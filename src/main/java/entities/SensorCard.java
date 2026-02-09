package entities;

import entities.capteur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Composant personnalisé pour afficher une carte de capteur
 * Style moderne et interactif
 */
public class SensorCard extends VBox {

    private capteur capteur;
    private Label lblType;
    private Label lblLocalisation;
    private Label lblStatut;
    private Label lblLastValue;
    private Button btnDetails;
    private Button btnEdit;
    private Button btnDelete;

    public SensorCard(capteur capteur) {
        this.capteur = capteur;
        initializeUI();
        updateData();
    }

    private void initializeUI() {
        // Appliquer le style de carte
        getStyleClass().add("sensor-card");
        setSpacing(12);
        setPadding(new Insets(15));
        setPrefWidth(280);
        setMinHeight(200);

        // HEADER - Type et icône
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getIconForType(capteur.getTypeCapteur()));
        icon.setStyle("-fx-font-size: 36px;");

        VBox headerText = new VBox(3);
        lblType = new Label(formatTypeName(capteur.getTypeCapteur()));
        lblType.getStyleClass().add("sensor-type");

        lblLocalisation = new Label();
        lblLocalisation.getStyleClass().add("sensor-location");

        headerText.getChildren().addAll(lblType, lblLocalisation);
        header.getChildren().addAll(icon, headerText);

        // SÉPARATEUR
        Region separator = new Region();
        separator.setStyle("-fx-background-color: #E8E8E8; -fx-pref-height: 1;");
        separator.setPadding(new Insets(8, 0, 8, 0));

        // INFO - ID et Statut
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        VBox idBox = new VBox(3);
        Label lblIdLabel = new Label("ID Capteur");
        lblIdLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");
        Label lblIdValue = new Label("#" + capteur.getIdCapteur());
        lblIdValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #076A39;");
        idBox.getChildren().addAll(lblIdLabel, lblIdValue);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblStatut = new Label();
        lblStatut.getStyleClass().add("badge");

        infoBox.getChildren().addAll(idBox, spacer, lblStatut);

        // DERNIÈRE VALEUR
        VBox valueBox = new VBox(5);
        Label lblValueLabel = new Label("Dernière mesure:");
        lblValueLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");

        lblLastValue = new Label("--");
        lblLastValue.getStyleClass().add("sensor-value");
        lblLastValue.setStyle("-fx-font-size: 24px;");

        valueBox.getChildren().addAll(lblValueLabel, lblLastValue);

        // BOUTONS D'ACTION
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(10, 0, 0, 0));

        btnDetails = new Button("📊 Détails");
        btnDetails.getStyleClass().addAll("btn", "btn-success");
        btnDetails.setPrefWidth(85);
        btnDetails.setStyle("-fx-font-size: 11px; -fx-padding: 8 10;");

        btnEdit = new Button("✏️");
        btnEdit.getStyleClass().add("btn");
        btnEdit.setStyle("-fx-font-size: 14px; -fx-padding: 8 12; -fx-background-color: #E1B323;");

        btnDelete = new Button("🗑️");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");
        btnDelete.setStyle("-fx-font-size: 14px; -fx-padding: 8 12;");

        actionBox.getChildren().addAll(btnDetails, btnEdit, btnDelete);

        // AJOUTER TOUS LES ÉLÉMENTS
        getChildren().addAll(header, separator, infoBox, valueBox, actionBox);
    }

    /**
     * Met à jour les données affichées
     */
    public void updateData() {
        lblLocalisation.setText("📍 " + capteur.getLocalisation());

        // Mise à jour du statut avec badge coloré
        String statut = capteur.getStatut();
        lblStatut.setText(statut);
        lblStatut.getStyleClass().removeAll("badge-actif", "badge-inactif", "badge-maintenance", "badge-alerte");

        switch (statut.toUpperCase()) {
            case "ACTIF":
                lblStatut.getStyleClass().add("badge-actif");
                break;
            case "INACTIF":
                lblStatut.getStyleClass().add("badge-inactif");
                break;
            case "MAINTENANCE":
                lblStatut.getStyleClass().add("badge-maintenance");
                break;
            default:
                lblStatut.getStyleClass().add("badge-alerte");
        }
    }

    /**
     * Met à jour la dernière valeur mesurée
     */
    public void setLastValue(double value, String unit) {
        lblLastValue.setText(String.format("%.2f %s", value, unit));
    }

    /**
     * Retourne l'icône appropriée selon le type de capteur
     */
    private String getIconForType(String type) {
        switch (type.toUpperCase()) {
            case "TEMPERATURE":
                return "🌡️";
            case "HUMIDITE_SOL":
                return "💧";
            case "PH_SOL":
                return "🧪";
            case "PLUVIOMETRIE":
                return "🌧️";
            case "LUMINOSITE":
                return "☀️";
            case "VENT":
                return "💨";
            default:
                return "📡";
        }
    }

    /**
     * Formate le nom du type de capteur
     */
    private String formatTypeName(String type) {
        return type.replace("_", " ")
                .toLowerCase()
                .replaceFirst("^.", String.valueOf(type.charAt(0)));
    }

    // GETTERS pour les boutons (pour attacher des événements)
    public Button getBtnDetails() {
        return btnDetails;
    }

    public Button getBtnEdit() {
        return btnEdit;
    }

    public Button getBtnDelete() {
        return btnDelete;
    }

    public capteur getCapteur() {
        return capteur;
    }
}