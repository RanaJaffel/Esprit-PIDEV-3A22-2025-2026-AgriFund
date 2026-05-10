package com.agrifund.services;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.SnapshotParameters;

import java.security.SecureRandom;
import java.util.Random;

public class CaptchaService {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CAPTCHA_LENGTH = 6;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 80;

    private String currentCaptchaText;
    private final SecureRandom random = new SecureRandom();

    /**
     * Génère un nouveau CAPTCHA et retourne l'image
     */
    public Image generateCaptcha() {
        // Générer le texte aléatoire
        currentCaptchaText = generateRandomText();

        // Créer le canvas
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fond avec gradient
        drawBackground(gc);

        // Ajouter du bruit (lignes et points)
        drawNoise(gc);

        // Dessiner le texte déformé
        drawCaptchaText(gc, currentCaptchaText);

        // Ajouter des lignes de brouillage sur le texte
        drawOverlayLines(gc);

        // Convertir en image
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = canvas.snapshot(params, null);

        return image;
    }

    /**
     * Vérifie si le texte entré correspond au CAPTCHA
     */
    public boolean verifyCaptcha(String userInput) {
        if (userInput == null || currentCaptchaText == null) {
            return false;
        }
        return currentCaptchaText.equalsIgnoreCase(userInput.trim());
    }

    /**
     * Retourne le texte actuel du CAPTCHA (pour debug uniquement)
     */
    public String getCurrentCaptchaText() {
        return currentCaptchaText;
    }

    private String generateRandomText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    private void drawBackground(GraphicsContext gc) {
        // Gradient de fond
        gc.setFill(Color.rgb(245, 251, 240));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Ajouter des rectangles colorés aléatoires
        for (int i = 0; i < 5; i++) {
            gc.setFill(Color.rgb(
                    200 + random.nextInt(55),
                    220 + random.nextInt(35),
                    200 + random.nextInt(55),
                    0.3
            ));
            gc.fillRect(
                    random.nextInt(WIDTH),
                    random.nextInt(HEIGHT),
                    30 + random.nextInt(50),
                    20 + random.nextInt(30)
            );
        }
    }

    private void drawNoise(GraphicsContext gc) {
        // Points aléatoires
        for (int i = 0; i < 100; i++) {
            gc.setFill(Color.rgb(
                    random.nextInt(200),
                    random.nextInt(200),
                    random.nextInt(200),
                    0.5
            ));
            gc.fillOval(
                    random.nextInt(WIDTH),
                    random.nextInt(HEIGHT),
                    2 + random.nextInt(3),
                    2 + random.nextInt(3)
            );
        }

        // Lignes courbes
        gc.setStroke(Color.rgb(150, 150, 150, 0.5));
        gc.setLineWidth(1);
        for (int i = 0; i < 3; i++) {
            gc.beginPath();
            gc.moveTo(random.nextInt(WIDTH), random.nextInt(HEIGHT));
            gc.bezierCurveTo(
                    random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT)
            );
            gc.stroke();
        }
    }

    private void drawCaptchaText(GraphicsContext gc, String text) {
        int charWidth = WIDTH / (CAPTCHA_LENGTH + 1);

        for (int i = 0; i < text.length(); i++) {
            // Couleur aléatoire pour chaque caractère
            gc.setFill(Color.rgb(
                    random.nextInt(100),
                    80 + random.nextInt(70),
                    random.nextInt(100)
            ));

            // Police avec taille variable
            int fontSize = 28 + random.nextInt(12);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, fontSize));

            // Position avec légère variation
            double x = charWidth * (i + 0.5) + random.nextInt(10) - 5;
            double y = HEIGHT / 2 + fontSize / 3 + random.nextInt(10) - 5;

            // Rotation aléatoire
            gc.save();
            gc.translate(x, y);
            gc.rotate(-15 + random.nextInt(30));
            gc.fillText(String.valueOf(text.charAt(i)), 0, 0);
            gc.restore();
        }
    }

    private void drawOverlayLines(GraphicsContext gc) {
        // Lignes qui passent sur le texte
        for (int i = 0; i < 4; i++) {
            gc.setStroke(Color.rgb(
                    100 + random.nextInt(100),
                    100 + random.nextInt(100),
                    100 + random.nextInt(100),
                    0.7
            ));
            gc.setLineWidth(1 + random.nextDouble());
            gc.strokeLine(
                    0, random.nextInt(HEIGHT),
                    WIDTH, random.nextInt(HEIGHT)
            );
        }
    }
}
