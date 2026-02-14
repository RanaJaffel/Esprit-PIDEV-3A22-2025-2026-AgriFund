package com.agrifund.util;

import com.agrifund.model.ProduitFinancier;
import com.agrifund.model.OffreFinanciere;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFGenerator {

    // Couleurs AgriFund
    private static final BaseColor VERT_FONCE = new BaseColor(9, 80, 50);
    private static final BaseColor VERT_CLAIR = new BaseColor(8, 150, 71);
    private static final BaseColor LIME = new BaseColor(178, 217, 68);
    private static final BaseColor GOLD = new BaseColor(225, 179, 35);
    private static final BaseColor GRIS_CLAIR = new BaseColor(245, 245, 245);
    private static final BaseColor VERT_PALE = new BaseColor(240, 248, 240);

    // Fonts
    private static final Font FONT_TITRE = new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, VERT_FONCE);
    private static final Font FONT_SOUS_TITRE = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, VERT_CLAIR);
    private static final Font FONT_SECTION = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, VERT_FONCE);
    private static final Font FONT_LABEL = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, VERT_FONCE);
    private static final Font FONT_VALEUR = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_VALEUR_GRAND = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, VERT_CLAIR);
    private static final Font FONT_STATUT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
    private static final Font FONT_FOOTER = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
    private static final Font FONT_COPYRIGHT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, VERT_CLAIR);

    // ==================== GENERER PDF PRODUIT ====================

    public static String genererPDFProduit(ProduitFinancier produit) throws Exception {
        String home = System.getProperty("user.home");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "Produit_" + produit.getIdProduit() + "_" + timestamp + ".pdf";
        String filePath = home + "/Downloads/" + fileName;

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Header
        ajouterHeader(document, "Fiche Produit Financier");

        // Informations principales
        PdfPTable tableInfo = new PdfPTable(2);
        tableInfo.setWidthPercentage(100);
        tableInfo.setWidths(new float[]{1, 2});
        tableInfo.setSpacingBefore(20);

        ajouterLigneTableau(tableInfo, "ID Produit", "#" + produit.getIdProduit());
        ajouterLigneTableau(tableInfo, "Nom du Produit", produit.getNomProduit());
        ajouterLigneTableau(tableInfo, "Type de Financement", produit.getTypeFinancement());
        ajouterLigneTableau(tableInfo, "Taux d'Interet", String.format("%.2f %%", produit.getTauxInteret()));
        ajouterLigneTableau(tableInfo, "Montant Minimum", String.format("%,.2f DH", produit.getMontantMin()));
        ajouterLigneTableau(tableInfo, "Montant Maximum", String.format("%,.2f DH", produit.getMontantMax()));

        document.add(tableInfo);
        document.add(new Paragraph(" "));

        // Statistiques
        double moyenne = (produit.getMontantMin() + produit.getMontantMax()) / 2;
        double ecart = produit.getMontantMax() - produit.getMontantMin();

        Paragraph statsTitle = new Paragraph("STATISTIQUES", FONT_SECTION);
        statsTitle.setSpacingBefore(15);
        statsTitle.setSpacingAfter(10);
        document.add(statsTitle);

        PdfPTable tableStats = new PdfPTable(2);
        tableStats.setWidthPercentage(100);
        tableStats.setWidths(new float[]{1, 1});

        ajouterCelluleStats(tableStats, "Montant Moyen", String.format("%,.2f DH", moyenne), VERT_CLAIR);
        ajouterCelluleStats(tableStats, "Ecart Min-Max", String.format("%,.2f DH", ecart), GOLD);

        document.add(tableStats);
        document.add(new Paragraph(" "));

        // Regles financieres
        Paragraph reglesTitle = new Paragraph("REGLES FINANCIERES", FONT_SECTION);
        reglesTitle.setSpacingBefore(15);
        reglesTitle.setSpacingAfter(10);
        document.add(reglesTitle);

        String regles = produit.getReglesFinancieres();
        if (regles == null || regles.trim().isEmpty()) {
            regles = "Aucune regle financiere specifiee pour ce produit.";
        }

        PdfPTable tableRegles = new PdfPTable(1);
        tableRegles.setWidthPercentage(100);

        PdfPCell cellRegles = new PdfPCell(new Phrase(regles, FONT_VALEUR));
        cellRegles.setPadding(15);
        cellRegles.setBackgroundColor(GRIS_CLAIR);
        cellRegles.setBorderColor(VERT_CLAIR);
        cellRegles.setBorderWidth(2);
        tableRegles.addCell(cellRegles);

        document.add(tableRegles);

        // Footer
        ajouterFooter(document);

        document.close();

        System.out.println("PDF Produit genere: " + filePath);
        return filePath;
    }

    // ==================== GENERER PDF OFFRE ====================

    public static String genererPDFOffre(OffreFinanciere offre) throws Exception {
        String home = System.getProperty("user.home");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "Offre_" + offre.getIdOffre() + "_" + timestamp + ".pdf";
        String filePath = home + "/Downloads/" + fileName;

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Header
        ajouterHeader(document, "Fiche Offre Financiere");

        // Informations principales
        PdfPTable tableInfo = new PdfPTable(2);
        tableInfo.setWidthPercentage(100);
        tableInfo.setWidths(new float[]{1, 2});
        tableInfo.setSpacingBefore(20);

        ajouterLigneTableau(tableInfo, "ID Offre", "#" + offre.getIdOffre());
        ajouterLigneTableau(tableInfo, "Nom de l'Offre", offre.getNomOffre());

        String nomProduit = offre.getNomProduit();
        if (nomProduit == null || nomProduit.isEmpty()) {
            nomProduit = "Produit ID: " + offre.getIdProduit();
        }
        ajouterLigneTableau(tableInfo, "Produit Associe", nomProduit);
        ajouterLigneTableau(tableInfo, "Statut", offre.getStatut());

        document.add(tableInfo);
        document.add(new Paragraph(" "));

        // Statut avec couleur
        PdfPTable statutTable = new PdfPTable(1);
        statutTable.setWidthPercentage(50);
        statutTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        statutTable.setSpacingBefore(10);

        BaseColor statutColor;
        String statut = offre.getStatut();
        if ("Active".equals(statut)) {
            statutColor = VERT_CLAIR;
        } else if ("En pause".equals(statut)) {
            statutColor = GOLD;
        } else {
            statutColor = new BaseColor(204, 68, 68);
        }

        PdfPCell statutCell = new PdfPCell(new Phrase("STATUT: " + statut.toUpperCase(), FONT_STATUT));
        statutCell.setBackgroundColor(statutColor);
        statutCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        statutCell.setPadding(15);
        statutCell.setBorder(Rectangle.NO_BORDER);
        statutTable.addCell(statutCell);

        document.add(statutTable);
        document.add(new Paragraph(" "));

        // Conditions
        Paragraph conditionsTitle = new Paragraph("CONDITIONS ET DETAILS", FONT_SECTION);
        conditionsTitle.setSpacingBefore(15);
        conditionsTitle.setSpacingAfter(10);
        document.add(conditionsTitle);

        String conditions = offre.getConditions();
        if (conditions == null || conditions.trim().isEmpty()) {
            conditions = "Aucune condition specifiee pour cette offre.";
        }

        PdfPTable tableConditions = new PdfPTable(1);
        tableConditions.setWidthPercentage(100);

        PdfPCell cellConditions = new PdfPCell(new Phrase(conditions, FONT_VALEUR));
        cellConditions.setPadding(15);
        cellConditions.setBackgroundColor(GRIS_CLAIR);
        cellConditions.setBorderColor(VERT_CLAIR);
        cellConditions.setBorderWidth(2);
        tableConditions.addCell(cellConditions);

        document.add(tableConditions);

        // Footer
        ajouterFooter(document);

        document.close();

        System.out.println("PDF Offre genere: " + filePath);
        return filePath;
    }

    // ==================== METHODES UTILITAIRES ====================

    private static void ajouterHeader(Document document, String sousTitre) throws Exception {
        Paragraph titre = new Paragraph("AGRIFUND", FONT_TITRE);
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        Paragraph subTitre = new Paragraph(sousTitre, FONT_SOUS_TITRE);
        subTitre.setAlignment(Element.ALIGN_CENTER);
        subTitre.setSpacingAfter(20);
        document.add(subTitre);

        LineSeparator line = new LineSeparator();
        line.setLineColor(LIME);
        line.setLineWidth(3);
        document.add(line);
        document.add(new Paragraph(" "));
    }

    private static void ajouterFooter(Document document) throws Exception {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        LineSeparator footerLine = new LineSeparator();
        footerLine.setLineColor(LIME);
        footerLine.setLineWidth(2);
        document.add(footerLine);

        String dateGeneration = new SimpleDateFormat("dd/MM/yyyy a HH:mm").format(new Date());
        Paragraph footer = new Paragraph("Document genere le " + dateGeneration, FONT_FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);

        Paragraph copyright = new Paragraph("AgriFund - Plateforme de Financement Agricole", FONT_COPYRIGHT);
        copyright.setAlignment(Element.ALIGN_CENTER);
        document.add(copyright);
    }

    private static void ajouterLigneTableau(PdfPTable table, String label, String valeur) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_LABEL));
        labelCell.setPadding(12);
        labelCell.setBackgroundColor(VERT_PALE);
        labelCell.setBorderColor(LIME);
        labelCell.setBorderWidth(1);
        table.addCell(labelCell);

        PdfPCell valeurCell = new PdfPCell(new Phrase(valeur != null ? valeur : "", FONT_VALEUR));
        valeurCell.setPadding(12);
        valeurCell.setBorderColor(LIME);
        valeurCell.setBorderWidth(1);
        table.addCell(valeurCell);
    }

    private static void ajouterCelluleStats(PdfPTable table, String label, String valeur, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(15);
        cell.setBackgroundColor(bgColor);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph labelPara = new Paragraph(label, new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.WHITE));
        labelPara.setAlignment(Element.ALIGN_CENTER);

        Paragraph valeurPara = new Paragraph(valeur, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.WHITE));
        valeurPara.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(labelPara);
        cell.addElement(valeurPara);

        table.addCell(cell);
    }
}