package view.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public MainFrame() {
        setTitle("AgriFund - Système de Gestion Agricole");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Initialiser le look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initComponents();
    }

    private void initComponents() {
        // Menu Bar
        setJMenuBar(createMenuBar());

        // Panel principal avec CardLayout pour changer entre les vues
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Ajouter les différents panels
        mainPanel.add(new WelcomePanel(), "WELCOME");
        mainPanel.add(new ProduitFinancierPanel(), "PRODUITS");
        mainPanel.add(new OffreFinancierePanel(), "OFFRES");

        // Panel de navigation
        JPanel navigationPanel = createNavigationPanel();

        // Layout principal
        setLayout(new BorderLayout());
        add(navigationPanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Afficher le panel de bienvenue par défaut
        cardLayout.show(mainPanel, "WELCOME");
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Fichier
        JMenu fileMenu = new JMenu("Fichier");
        JMenuItem exitItem = new JMenuItem("Quitter");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Menu Aide
        JMenu helpMenu = new JMenu("Aide");
        JMenuItem aboutItem = new JMenuItem("À propos");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(new Color(52, 73, 94));
        navPanel.setPreferredSize(new Dimension(200, getHeight()));

        // Logo ou titre
        JLabel titleLabel = new JLabel("AGRIFUND", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        navPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        navPanel.add(titleLabel);
        navPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Boutons de navigation
        JButton btnAccueil = createNavButton("🏠 Accueil", "WELCOME");
        JButton btnProduits = createNavButton("📊 Produits Financiers", "PRODUITS");
        JButton btnOffres = createNavButton("💼 Offres Financières", "OFFRES");

        navPanel.add(btnAccueil);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(btnProduits);
        navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        navPanel.add(btnOffres);

        return navPanel;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));

        button.addActionListener(e -> cardLayout.show(mainPanel, cardName));

        // Effet hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }
        });

        return button;
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "AgriFund v1.0\nSystème de Gestion de Financement Agricole\n© 2024",
                "À propos",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}