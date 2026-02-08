package view.gui;

import model.ProduitFinancier;
import service.ProduitFinancierService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProduitFinancierPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private ProduitFinancierService service;

    // Champs de formulaire
    private JTextField txtNom, txtType, txtTaux, txtMin, txtMax;
    private JTextArea txtRegles;

    public ProduitFinancierPanel() {
        service = new ProduitFinancierService();
        setLayout(new BorderLayout());

        // Panel supérieur avec le titre
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Panel central avec le tableau et le formulaire
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createTablePanel());
        splitPane.setRightComponent(createFormPanel());
        splitPane.setDividerLocation(700);

        add(splitPane, BorderLayout.CENTER);

        // Charger les données
        loadData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(236, 240, 241));

        JLabel titleLabel = new JLabel("Gestion des Produits Financiers");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("🔄 Actualiser");
        JButton btnDelete = new JButton("🗑️ Supprimer");
        JButton btnEdit = new JButton("✏️ Modifier");

        btnRefresh.addActionListener(e -> loadData());
        btnDelete.addActionListener(e -> deleteSelected());
        btnEdit.addActionListener(e -> loadSelectedToForm());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);

        // Tableau
        String[] columns = {"ID", "Nom", "Type", "Taux (%)", "Min", "Max"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Formulaire Produit"));

        // Créer les champs
        panel.add(createFormField("Nom du produit:", txtNom = new JTextField()));
        panel.add(createFormField("Type financement:", txtType = new JTextField()));
        panel.add(createFormField("Taux d'intérêt (%):", txtTaux = new JTextField()));
        panel.add(createFormField("Montant minimum:", txtMin = new JTextField()));
        panel.add(createFormField("Montant maximum:", txtMax = new JTextField()));

        // Zone de texte pour les règles
        JPanel reglesPanel = new JPanel(new BorderLayout());
        reglesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        reglesPanel.add(new JLabel("Règles financières:"), BorderLayout.NORTH);
        txtRegles = new JTextArea(5, 20);
        txtRegles.setLineWrap(true);
        reglesPanel.add(new JScrollPane(txtRegles), BorderLayout.CENTER);
        panel.add(reglesPanel);

        // Boutons du formulaire
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnSave = new JButton("💾 Enregistrer");
        JButton btnClear = new JButton("🔄 Effacer");
        JButton btnCalculate = new JButton("🧮 Calculer Intérêts");

        btnSave.addActionListener(e -> saveProduit());
        btnClear.addActionListener(e -> clearForm());
        btnCalculate.addActionListener(e -> showCalculatorDialog());

        btnPanel.add(btnSave);
        btnPanel.add(btnClear);
        btnPanel.add(btnCalculate);

        panel.add(btnPanel);

        return panel;
    }

    private JPanel createFormField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        return panel;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<ProduitFinancier> produits = service.obtenirTousProduits();

        for (ProduitFinancier p : produits) {
            Object[] row = {
                    p.getIdProduit(),
                    p.getNomProduit(),
                    p.getTypeFinancement(),
                    p.getTauxInteret(),
                    p.getMontantMin(),
                    p.getMontantMax()
            };
            tableModel.addRow(row);
        }
    }

    private void saveProduit() {
        try {
            ProduitFinancier produit = new ProduitFinancier(
                    txtNom.getText(),
                    txtType.getText(),
                    Double.parseDouble(txtTaux.getText()),
                    Double.parseDouble(txtMin.getText()),
                    Double.parseDouble(txtMax.getText()),
                    txtRegles.getText()
            );

            if (service.ajouterProduit(produit)) {
                JOptionPane.showMessageDialog(this, "Produit enregistré avec succès!");
                clearForm();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des valeurs numériques valides",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer ce produit?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (service.supprimerProduit(id)) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "Produit supprimé avec succès!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un produit");
        }
    }

    private void loadSelectedToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            ProduitFinancier produit = service.obtenirProduit(id);

            if (produit != null) {
                txtNom.setText(produit.getNomProduit());
                txtType.setText(produit.getTypeFinancement());
                txtTaux.setText(String.valueOf(produit.getTauxInteret()));
                txtMin.setText(String.valueOf(produit.getMontantMin()));
                txtMax.setText(String.valueOf(produit.getMontantMax()));
                txtRegles.setText(produit.getReglesFinancieres());
            }
        }
    }

    private void clearForm() {
        txtNom.setText("");
        txtType.setText("");
        txtTaux.setText("");
        txtMin.setText("");
        txtMax.setText("");
        txtRegles.setText("");
    }

    private void showCalculatorDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Calculateur d'intérêts", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtMontant = new JTextField();
        JTextField txtDuree = new JTextField();
        JTextField txtTauxCalc = new JTextField(txtTaux.getText());
        JLabel lblResultat = new JLabel("0.0");

        panel.add(new JLabel("Montant:"));
        panel.add(txtMontant);
        panel.add(new JLabel("Durée (mois):"));
        panel.add(txtDuree);
        panel.add(new JLabel("Taux (%):"));
        panel.add(txtTauxCalc);
        panel.add(new JLabel("Intérêts:"));
        panel.add(lblResultat);

        JButton btnCalculer = new JButton("Calculer");
        btnCalculer.addActionListener(e -> {
            try {
                double montant = Double.parseDouble(txtMontant.getText());
                int duree = Integer.parseInt(txtDuree.getText());
                double taux = Double.parseDouble(txtTauxCalc.getText());

                double interets = montant * (taux / 100) * (duree / 12.0);
                lblResultat.setText(String.format("%.2f", interets));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Valeurs invalides");
            }
        });

        panel.add(btnCalculer);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}