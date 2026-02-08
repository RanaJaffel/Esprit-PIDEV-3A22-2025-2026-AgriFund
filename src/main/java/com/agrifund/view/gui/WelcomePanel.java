package view.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WelcomePanel extends JPanel {

    public WelcomePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Panel central
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Logo/Image
        JLabel logoLabel = new JLabel("🌾");
        logoLabel.setFont(new Font("Arial", Font.PLAIN, 100));
        centerPanel.add(logoLabel, gbc);

        // Titre
        JLabel titleLabel = new JLabel("Bienvenue dans AgriFund");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(44, 62, 80));
        centerPanel.add(titleLabel, gbc);

        // Sous-titre
        JLabel subtitleLabel = new JLabel("Système de Gestion de Financement Agricole");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        centerPanel.add(subtitleLabel, gbc);

        // Date et heure
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy - HH:mm");
        JLabel dateLabel = new JLabel(LocalDateTime.now().format(formatter));
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        centerPanel.add(dateLabel, gbc);

        // Stats Panel
        JPanel statsPanel = createStatsPanel();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(30, 10, 10, 10);
        centerPanel.add(statsPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Timer pour mettre à jour l'heure
        Timer timer = new Timer(60000, e -> {
            dateLabel.setText(LocalDateTime.now().format(formatter));
        });
        timer.start();
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(600, 100));

        // Carte 1
        JPanel card1 = createStatCard("Produits Financiers", "12", new Color(52, 152, 219));
        // Carte 2
        JPanel card2 = createStatCard("Offres Actives", "8", new Color(46, 204, 113));
        // Carte 3
        JPanel card3 = createStatCard("Clients", "156", new Color(155, 89, 182));

        panel.add(card1);
        panel.add(card2);
        panel.add(card3);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(valueLabel);

        return card;
    }
}