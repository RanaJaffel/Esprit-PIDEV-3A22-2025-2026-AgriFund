package com.agrifund.controller;

import com.agrifund.entities.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.agrifund.services.ApiMeteoService;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MeteoController {

    @FXML private Label lblVille;
    @FXML private Label lblTemp;
    @FXML private Label lblRessenti;
    @FXML private Label lblHumidite;
    @FXML private Label lblVent;
    @FXML private Label lblDescription;
    @FXML private ImageView imgIcon;
    @FXML private Label lblDate;

    private final ApiMeteoService api = new ApiMeteoService();

    @FXML
    public void initialize() {

        String ville = "Tunis";
        lblVille.setText("Météo - " + ville);

        MeteoData data = api.getWeather(ville);

        if (data != null) {

            lblTemp.setText(data.getTemperature() + " °C");
            lblRessenti.setText("Ressenti : " + data.getRessentie() + " °C");
            lblHumidite.setText("Humidité : " + data.getHumidite() + " %");
            lblVent.setText("Vent : " + data.getVent() + " m/s");
            lblDescription.setText(data.getDescription());

            String iconUrl =
                    "https://openweathermap.org/img/wn/"
                            + data.getIcon()
                            + "@2x.png";

            imgIcon.setImage(new Image(iconUrl));
        }
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");

        lblDate.setText(LocalDate.now().format(formatter));
    }
    @FXML
    public void allerReleve(javafx.event.ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("com/agrifund/fxml/dashboard-rana.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}