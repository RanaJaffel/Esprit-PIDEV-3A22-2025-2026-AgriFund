package com.agrifund.view;

import com.agrifund.controller.OffreFinanciereController;
import com.agrifund.model.OffreFinanciere;
import com.agrifund.util.AgrifundDBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;  // Add this import at the top
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.scene.image.Image;  // Add this import

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class OffreFinanciereViewController {

    @FXML private TextField txtRecherche;

    @FXML private TableView<OffreFinanciere> tableOffres;
    @FXML private TableColumn<OffreFinanciere, Integer> colId;
    @FXML private TableColumn<OffreFinanciere, String> colNom;
    @FXML private TableColumn<OffreFinanciere, String> colProduit;
    @FXML private TableColumn<OffreFinanciere, String> colConditions;
    @FXML private TableColumn<OffreFinanciere, String> colStatut;

    @FXML private Label lblTotalOffres;
    @FXML private Label lblActives;
    @FXML private Label lblEnPause;
    @FXML private Label lblExpirees;

    private OffreFinanciereController offreController;
    private ObservableList<OffreFinanciere> offresList;

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation OffreFinanciereViewController...");

        try {
            offreController = new OffreFinanciereController();
            offresList = FXCollections.observableArrayList();

            // Configuration des colonnes
            colId.setCellValueFactory(new PropertyValueFactory<>("idOffre"));
            colNom.setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
            colProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
            colConditions.setCellValueFactory(new PropertyValueFactory<>("conditions"));
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

            // Style pour la colonne statut
            colStatut.setCellFactory(column -> new TableCell<OffreFinanciere, String>() {
                @Override
                protected void updateItem(String statut, boolean empty) {
                    super.updateItem(statut, empty);
                    if (empty || statut == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(statut);
                        if (statut.equals("Active")) {
                            setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        } else if (statut.equals("En pause")) {
                            setStyle("-fx-text-fill: #F9A825; -fx-font-weight: bold;");
                        } else if (statut.equals("Expiree")) {
                            setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-font-weight: bold;");
                        }
                    }
                }
            });

            // Charger les donnees
            chargerDonnees();

            System.out.println("✅ Initialisation terminee!");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            initializeEmptyState();
        }
    }

    private void initializeEmptyState() {
        lblTotalOffres.setText("0");
        lblActives.setText("0");
        lblEnPause.setText("0");
        lblExpirees.setText("0");
        tableOffres.setItems(FXCollections.observableArrayList());
    }

    private void chargerDonnees() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = AgrifundDBConnection.getConnection();
            if (conn == null) {
                System.err.println("⚠️ Connexion null - impossible de charger les donnees");
                initializeEmptyState();
                return;
            }

            String sql = "SELECT o.*, p.nom_produit FROM offre_financiere o " +
                    "JOIN produit_financier p ON o.id_produit = p.id_produit " +
                    "ORDER BY o.id_offre DESC";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            offresList.clear();
            while (rs.next()) {
                OffreFinanciere offre = new OffreFinanciere();
                offre.setIdOffre(rs.getInt("id_offre"));
                offre.setNomOffre(rs.getString("nom_offre"));
                offre.setConditions(rs.getString("conditions"));
                offre.setStatut(rs.getString("statut"));
                offre.setIdProduit(rs.getInt("id_produit"));
                offre.setNomProduit(rs.getString("nom_produit"));
                offresList.add(offre);
            }

            tableOffres.setItems(offresList);
            mettreAJourStats();

            System.out.println("✅ " + offresList.size() + " offres chargees");

        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
            initializeEmptyState();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void mettreAJourStats() {
        Connection conn = null;

        try {
            conn = AgrifundDBConnection.getConnection();
            if (conn == null) {
                initializeEmptyState();
                return;
            }

            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery("SELECT COUNT(*) FROM offre_financiere");
            if (rs1.next()) {
                lblTotalOffres.setText(String.valueOf(rs1.getInt(1)));
            }
            rs1.close();
            stmt1.close();

            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM offre_financiere WHERE statut=?");
            ps2.setString(1, "Active");
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                lblActives.setText(String.valueOf(rs2.getInt(1)));
            }
            rs2.close();
            ps2.close();

            PreparedStatement ps3 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM offre_financiere WHERE statut=?");
            ps3.setString(1, "En pause");
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                lblEnPause.setText(String.valueOf(rs3.getInt(1)));
            }
            rs3.close();
            ps3.close();

            PreparedStatement ps4 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM offre_financiere WHERE statut=?");
            ps4.setString(1, "Expiree");
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) {
                lblExpirees.setText(String.valueOf(rs4.getInt(1)));
            }
            rs4.close();
            ps4.close();

        } catch (SQLException e) {
            System.err.println("❌ Erreur stats: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/view/FormulaireOffreView.fxml"));
            Parent root = loader.load();

            FormulaireOffreController controller = loader.getController();
            controller.setModeAjout();
            controller.setOnSuccess(() -> chargerDonnees());

            Stage stage = new Stage();

            // ✅ Add logo icon
            try {
                Image icon = new Image(getClass().getResourceAsStream("/com/agrifund/images/logo.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("⚠️ Logo non trouve");
            }

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 580, 680));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleModifier() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner une offre a modifier!", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/view/FormulaireOffreView.fxml"));
            Parent root = loader.load();

            FormulaireOffreController controller = loader.getController();
            controller.setModeModification(selected);
            controller.setOnSuccess(() -> chargerDonnees());

            Stage stage = new Stage();

            // ✅ Add logo icon
            try {
                Image icon = new Image(getClass().getResourceAsStream("/com/agrifund/images/logo.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("⚠️ Logo non trouve");
            }

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 580, 680));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void handleDownloadPDF() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner une offre pour telecharger le PDF!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String home = System.getProperty("user.home");
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "Offre_" + selected.getIdOffre() + "_" + timestamp + ".pdf";
            String filePath = home + "/Downloads/" + fileName;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Couleurs
            BaseColor vertFonce = new BaseColor(46, 125, 50);
            BaseColor vertClair = new BaseColor(76, 175, 80);
            BaseColor lime = new BaseColor(129, 199, 132);

            // Fonts
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, vertFonce);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, vertClair);
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, vertFonce);
            Font fontValeur = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font fontStatut = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE);

            // Header
            Paragraph titre = new Paragraph("AGRIFUND", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);

            Paragraph sousTitre = new Paragraph("Fiche Offre Financiere", fontSousTitre);
            sousTitre.setAlignment(Element.ALIGN_CENTER);
            sousTitre.setSpacingAfter(20);
            document.add(sousTitre);

            LineSeparator line = new LineSeparator();
            line.setLineColor(lime);
            line.setLineWidth(3);
            document.add(line);
            document.add(new Paragraph(" "));

            // Tableau principal
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2});
            table.setSpacingBefore(20);

            addTableRow(table, "ID Offre", "#" + selected.getIdOffre(), fontLabel, fontValeur, lime);
            addTableRow(table, "Nom de l'Offre", selected.getNomOffre(), fontLabel, fontValeur, lime);
            addTableRow(table, "Produit Associe", selected.getNomProduit(), fontLabel, fontValeur, lime);
            addTableRow(table, "Statut", selected.getStatut(), fontLabel, fontValeur, lime);

            document.add(table);
            document.add(new Paragraph(" "));

            // Section Conditions
            Paragraph conditionsTitre = new Paragraph("CONDITIONS ET DETAILS", fontLabel);
            conditionsTitre.setSpacingBefore(20);
            conditionsTitre.setSpacingAfter(10);
            document.add(conditionsTitre);

            PdfPTable conditionsTable = new PdfPTable(1);
            conditionsTable.setWidthPercentage(100);

            PdfPCell conditionsCell = new PdfPCell(new Phrase(
                    selected.getConditions() != null ? selected.getConditions() : "Aucune condition specifiee",
                    fontValeur));
            conditionsCell.setPadding(15);
            conditionsCell.setBackgroundColor(new BaseColor(241, 248, 233));
            conditionsCell.setBorderColor(vertClair);
            conditionsCell.setBorderWidth(2);
            conditionsTable.addCell(conditionsCell);

            document.add(conditionsTable);
            document.add(new Paragraph(" "));

            // Statut avec couleur
            PdfPTable statutTable = new PdfPTable(1);
            statutTable.setWidthPercentage(50);
            statutTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            BaseColor statutColor;
            if ("Active".equals(selected.getStatut())) {
                statutColor = vertClair;
            } else if ("En pause".equals(selected.getStatut())) {
                statutColor = new BaseColor(251, 192, 45);
            } else {
                statutColor = new BaseColor(198, 40, 40);
            }

            PdfPCell statutCell = new PdfPCell(new Phrase("STATUT: " + selected.getStatut().toUpperCase(), fontStatut));
            statutCell.setBackgroundColor(statutColor);
            statutCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statutCell.setPadding(15);
            statutCell.setBorder(Rectangle.NO_BORDER);
            statutTable.addCell(statutCell);

            document.add(statutTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Footer
            LineSeparator footerLine = new LineSeparator();
            footerLine.setLineColor(lime);
            footerLine.setLineWidth(2);
            document.add(footerLine);

            Paragraph footer = new Paragraph(
                    "Document genere le " + new SimpleDateFormat("dd/MM/yyyy a HH:mm").format(new Date()),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            Paragraph copyright = new Paragraph(
                    "AgriFund - Plateforme de Financement Agricole",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, vertClair));
            copyright.setAlignment(Element.ALIGN_CENTER);
            document.add(copyright);

            document.close();

            showAlert("Succes", "PDF telecharge avec succes!\n\nFichier: " + filePath, Alert.AlertType.INFORMATION);
            System.out.println("✅ PDF genere: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de generer le PDF:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, BaseColor borderColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(12);
        labelCell.setBackgroundColor(new BaseColor(232, 245, 233));
        labelCell.setBorderColor(borderColor);
        labelCell.setBorderWidth(1);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setPadding(12);
        valueCell.setBorderColor(borderColor);
        valueCell.setBorderWidth(1);
        table.addCell(valueCell);
    }

    @FXML
    private void handleSupprimer() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner une offre a supprimer!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'offre?");
        confirm.setContentText("Voulez-vous vraiment supprimer: " + selected.getNomOffre() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = offreController.supprimerOffre(selected.getIdOffre());

            if (success) {
                chargerDonnees();
                showAlert("Succes", "Offre supprimee avec succes!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Impossible de supprimer l'offre!", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRechercher() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String keyword = txtRecherche.getText().trim();
            if (keyword.isEmpty()) {
                chargerDonnees();
                return;
            }

            conn = AgrifundDBConnection.getConnection();
            if (conn == null) {
                showAlert("Erreur", "Connexion a la base de donnees impossible", Alert.AlertType.ERROR);
                return;
            }

            String sql = "SELECT o.*, p.nom_produit FROM offre_financiere o " +
                    "JOIN produit_financier p ON o.id_produit = p.id_produit " +
                    "WHERE o.nom_offre LIKE ? OR o.statut LIKE ? OR p.nom_produit LIKE ? " +
                    "ORDER BY o.id_offre DESC";
            ps = conn.prepareStatement(sql);

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            rs = ps.executeQuery();

            offresList.clear();
            while (rs.next()) {
                OffreFinanciere offre = new OffreFinanciere();
                offre.setIdOffre(rs.getInt("id_offre"));
                offre.setNomOffre(rs.getString("nom_offre"));
                offre.setConditions(rs.getString("conditions"));
                offre.setStatut(rs.getString("statut"));
                offre.setIdProduit(rs.getInt("id_produit"));
                offre.setNomProduit(rs.getString("nom_produit"));
                offresList.add(offre);
            }

            tableOffres.setItems(offresList);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur recherche: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerDonnees();
        System.out.println("✅ Donnees actualisees");
    }

    @FXML
    private void handleVoirDetails() {
        OffreFinanciere selected = tableOffres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez selectionner une offre!", Alert.AlertType.WARNING);
            return;
        }

        String details = "Nom: " + selected.getNomOffre() + "\n\n" +
                "ID: #" + selected.getIdOffre() + "\n\n" +
                "Produit: " + selected.getNomProduit() + "\n\n" +
                "Statut: " + selected.getStatut() + "\n\n" +
                "Conditions: " +
                (selected.getConditions() != null ? selected.getConditions() : "Aucune");

        showAlert("Details - " + selected.getNomOffre(), details, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
