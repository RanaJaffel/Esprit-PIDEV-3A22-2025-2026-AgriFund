package view.gui;

import model.OffreFinanciere;
import model.ProduitFinancier;
import service.OffreFinanciereService;
import service.ProduitFinancierService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OffreFinancierePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private OffreFinanciereService offreService;
    private ProduitFinancierService produitService;

    // Champs de formulaire
    private JTextField txtNomOffre;
    private JTextArea txtConditions;
    private JComboBox<String> comboStatut;
    private JComboBox<ProduitItem> comboProduit;

    public OffreFinancierePanel() {
        offreService = new OffreFinanciereService();
        produitService = new ProduitFinancierService();
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
        loadProduits();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(236, 240, 241));

        JLabel titleLabel = new JLabel("Gestion des Offres Financières");
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
        JButton btnStatus = new JButton("📋 Changer Statut");

        btnRefresh.addActionListener(e -> loadData());
        btnDelete.addActionListener(e -> deleteSelected());
        btnEdit.addActionListener(e -> loadSelectedToForm());
        btnStatus.addActionListener(e -> changeStatus());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnStatus);
        buttonPanel.add(btnDelete);

        // Tableau
        String[] columns = {"ID", "Nom Offre", "Produit", "Statut", "Conditions"};
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
        panel.setBorder(BorderFactory.createTitledBorder("Formulaire Offre"));

        // Nom de l'offre
        panel.add(createFormField("Nom de l'offre:", txtNomOffre = new JTextField()));

        // Produit financier (ComboBox)
        JPanel produitPanel = new JPanel(new BorderLayout());
        produitPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        produitPanel.add(new JLabel("Produit financier:"), BorderLayout.NORTH);
        comboProduit = new JComboBox<>();
        produitPanel.add(comboProduit, BorderLayout.CENTER);
        produitPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel.add(produitPanel);

        // Statut (ComboBox)
        JPanel statutPanel = new JPanel(new BorderLayout());
        statutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statutPanel.add(new JLabel("Statut:"), BorderLayout.NORTH);
        comboStatut = new JComboBox<>(new String[]{"En attente", "Approuvée", "Rejetée", "En cours", "Terminée"});
        statutPanel.add(comboStatut, BorderLayout.CENTER);
        statutPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel.add(statutPanel);

        // Conditions
        JPanel conditionsPanel = new JPanel(new BorderLayout());
        conditionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        conditionsPanel.add(new JLabel("Conditions:"), BorderLayout.NORTH);
        txtConditions = new JTextArea(5, 20);
        txtConditions.setLineWrap(true);
        conditionsPanel.add(new JScrollPane(txtConditions), BorderLayout.CENTER);
        panel.add(conditionsPanel);

        // Boutons du formulaire
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnSave = new JButton("💾 Enregistrer");
        JButton btnClear = new JButton("🔄 Effacer");
        JButton btnViewDetails = new JButton("📊 Voir Détails Produit");

        btnSave.addActionListener(e -> saveOffre());
        btnClear.addActionListener(e -> clearForm());
        btnViewDetails.addActionListener(e -> showProduitDetails());

        btnPanel.add(btnSave);
        btnPanel.add(btnClear);
        btnPanel.add(btnViewDetails);

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
        List<OffreFinanciere> offres = offreService.obtenirToutesOffres();

        for (OffreFinanciere o : offres) {
            String nomProduit = (o.getProduitFinancier() != null) ?
                    o.getProduitFinancier().getNomProduit() : "N/A";

            Object[] row = {
                    o.getIdOffre(),
                    o.getNomOffre(),
                    nomProduit,
                    o.getStatut(),
                    o.getConditions()
            };
            tableModel.addRow(row);
        }
    }

    private void loadProduits() {
        comboProduit.removeAllItems();
        List<ProduitFinancier> produits = produitService.obtenirTousProduits();

        for (ProduitFinancier p : produits) {
            comboProduit.addItem(new ProduitItem(p.getIdProduit(), p.getNomProduit()));
        }
    }

    private void saveOffre() {
        try {
            ProduitItem selectedProduit = (ProduitItem) comboProduit.getSelectedItem();
            if (selectedProduit == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un produit");
                return;
            }

            OffreFinanciere offre = new OffreFinanciere(
                    txtNomOffre.getText(),
                    txtConditions.getText(),
                    (String) comboStatut.getSelectedItem(),
                    selectedProduit.id
            );

            if (offreService.ajouterOffre(offre)) {
                JOptionPane.showMessageDialog(this, "Offre enregistrée avec succès!");
                clearForm();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir supprimer cette offre?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (offreService.supprimerOffre(id)) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "Offre supprimée avec succès!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une offre");
        }
    }

    private void loadSelectedToForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            OffreFinanciere offre = offreService.obtenirOffre(id);

            if (offre != null) {
                txtNomOffre.setText(offre.getNomOffre());
                txtConditions.setText(offre.getConditions());
                comboStatut.setSelectedItem(offre.getStatut());

                // Sélectionner le bon produit dans la combo
                for (int i = 0; i < comboProduit.getItemCount(); i++) {
                    ProduitItem item = comboProduit.getItemAt(i);
                    if (item.id == offre.getIdProduit()) {
                        comboProduit.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void clearForm() {
        txtNomOffre.setText("");
        txtConditions.setText("");
        comboStatut.setSelectedIndex(0);
        if (comboProduit.getItemCount() > 0) {
            comboProduit.setSelectedIndex(0);
        }
    }

    private void changeStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 3);

            String[] statuts = {"En attente", "Approuvée", "Rejetée", "En cours", "Terminée"};
            String newStatus = (String) JOptionPane.showInputDialog(
                    this,
                    "Changer le statut de l'offre:",
                    "Changement de statut",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    statuts,
                    currentStatus
            );

            if (newStatus != null && !newStatus.equals(currentStatus)) {
                if (offreService.changerStatutOffre(id, newStatus)) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "Statut modifié avec succès!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une offre");
        }
    }

    private void showProduitDetails() {
        ProduitItem selectedProduit = (ProduitItem) comboProduit.getSelectedItem();
        if (selectedProduit != null) {
            ProduitFinancier produit = produitService.obtenirProduit(selectedProduit.id);
            if (produit != null) {
                String details = String.format(
                        "Produit: %s\nType: %s\nTaux: %.2f%%\nMontant: %.2f - %.2f\nRègles: %s",
                        produit.getNomProduit(),
                        produit.getTypeFinancement(),
                        produit.getTauxInteret(),
                        produit.getMontantMin(),
                        produit.getMontantMax(),
                        produit.getReglesFinancieres()
                );
                JOptionPane.showMessageDialog(this, details, "Détails du Produit",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // Classe interne pour gérer les items du ComboBox
    private static class ProduitItem {
        int id;
        String nom;

        ProduitItem(int id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        @Override
        public String toString() {
            return nom;
        }
    }
}