package com.agrifund.services;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.agrifund.entities.EvaluationRisque;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfExportService1 {

    // ── Palette de couleurs ──────────────────────────────────────────────────
    private static final DeviceRgb COLOR_PRIMARY      = new DeviceRgb(0x1A, 0x73, 0x2F); // vert agri foncé
    private static final DeviceRgb COLOR_PRIMARY_LIGHT= new DeviceRgb(0xE8, 0xF5, 0xE9);
    private static final DeviceRgb COLOR_ACCENT       = new DeviceRgb(0x4C, 0xAF, 0x50);
    private static final DeviceRgb COLOR_HEADER_BG    = new DeviceRgb(0x1A, 0x73, 0x2F);
    private static final DeviceRgb COLOR_ROW_ODD      = new DeviceRgb(0xFF, 0xFF, 0xFF);
    private static final DeviceRgb COLOR_ROW_EVEN     = new DeviceRgb(0xF1, 0xF8, 0xF1);
    private static final DeviceRgb COLOR_BORDER       = new DeviceRgb(0xC8, 0xE6, 0xC9);
    private static final DeviceRgb COLOR_TEXT_DARK    = new DeviceRgb(0x21, 0x21, 0x21);
    private static final DeviceRgb COLOR_TEXT_LIGHT   = new DeviceRgb(0xFF, 0xFF, 0xFF);
    private static final DeviceRgb COLOR_SUBTITLE     = new DeviceRgb(0x55, 0x55, 0x55);

    // Couleurs niveaux de risque
    private static final DeviceRgb COLOR_FAIBLE       = new DeviceRgb(0xB2, 0xD9, 0x44);
    private static final DeviceRgb COLOR_MOYEN        = new DeviceRgb(0xE1, 0xB3, 0x23);
    private static final DeviceRgb COLOR_ELEVE        = new DeviceRgb(0xE1, 0x7D, 0x23);
    private static final DeviceRgb COLOR_CRITIQUE     = new DeviceRgb(0xD9, 0x44, 0x44);

    // ── Fonts ────────────────────────────────────────────────────────────────
    private PdfFont fontRegular;
    private PdfFont fontBold;

    public void exportToPdf(List<EvaluationRisque> evaluations, String filePath) throws IOException {
        fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        fontBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        PdfWriter   writer   = new PdfWriter(filePath);
        PdfDocument pdfDoc   = new PdfDocument(writer);
        Document    document = new Document(pdfDoc, PageSize.A4.rotate()); // paysage pour tout afficher
        document.setMargins(40, 40, 60, 40);

        // Gestionnaire de pied de page
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE,
                new FooterHandler(pdfDoc, fontRegular, fontBold, COLOR_PRIMARY));

        // ── Contenu ──────────────────────────────────────────────────────────
        addHeader(document);
        addSummaryBanner(document, evaluations);
        document.add(new Paragraph("\n").setFontSize(4));
        addTable(document, evaluations);
        addFooterNote(document);

        document.close();
    }

    // ── En-tête ──────────────────────────────────────────────────────────────
    private void addHeader(Document document) throws IOException {
        // Bande verte supérieure
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth();

        // Titre côté gauche
        Cell titleCell = new Cell()
                .add(new Paragraph("🌿 Gestion Agricole Pro")
                        .setFont(fontBold).setFontSize(22)
                        .setFontColor(COLOR_TEXT_LIGHT))
                .add(new Paragraph("Rapport d'Évaluations de Risque")
                        .setFont(fontRegular).setFontSize(13)
                        .setFontColor(new DeviceRgb(0xC8, 0xE6, 0xC9)))
                .setBackgroundColor(COLOR_HEADER_BG)
                .setBorder(Border.NO_BORDER)
                .setPadding(18);

        // Date côté droit
        String dateStr = new SimpleDateFormat("dd MMMM yyyy  HH:mm",
                new java.util.Locale("fr", "FR")).format(new Date());
        Cell dateCell = new Cell()
                .add(new Paragraph("Généré le")
                        .setFont(fontRegular).setFontSize(9)
                        .setFontColor(new DeviceRgb(0xC8, 0xE6, 0xC9)))
                .add(new Paragraph(dateStr)
                        .setFont(fontBold).setFontSize(11)
                        .setFontColor(COLOR_TEXT_LIGHT))
                .setBackgroundColor(COLOR_HEADER_BG)
                .setBorder(Border.NO_BORDER)
                .setPadding(18)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        headerTable.addCell(titleCell);
        headerTable.addCell(dateCell);
        headerTable.setBorderRadius(new BorderRadius(8));
        document.add(headerTable);
        document.add(new Paragraph("").setMarginBottom(12));
    }

    // ── Bandeau statistiques ─────────────────────────────────────────────────
    private void addSummaryBanner(Document document, List<EvaluationRisque> evaluations) {
        long total    = evaluations.size();
        long faible   = evaluations.stream().filter(e -> "Faible".equals(e.getNiveauRisque())).count();
        long moyen    = evaluations.stream().filter(e -> "Moyen".equals(e.getNiveauRisque())).count();
        long eleve    = evaluations.stream().filter(e -> "Élevé".equals(e.getNiveauRisque())).count();
        long critique = evaluations.stream().filter(e -> "Critique".equals(e.getNiveauRisque())).count();

        Table stats = new Table(UnitValue.createPercentArray(new float[]{20, 20, 20, 20, 20}))
                .useAllAvailableWidth()
                .setMarginBottom(8);

        stats.addCell(statCard("📋 Total", String.valueOf(total), COLOR_ACCENT, COLOR_TEXT_LIGHT));
        stats.addCell(statCard("🟢 Faible", String.valueOf(faible), COLOR_FAIBLE, COLOR_TEXT_DARK));
        stats.addCell(statCard("🟡 Moyen", String.valueOf(moyen), COLOR_MOYEN, COLOR_TEXT_DARK));
        stats.addCell(statCard("🟠 Élevé", String.valueOf(eleve), COLOR_ELEVE, COLOR_TEXT_LIGHT));
        stats.addCell(statCard("🔴 Critique", String.valueOf(critique), COLOR_CRITIQUE, COLOR_TEXT_LIGHT));

        document.add(stats);
    }

    private Cell statCard(String label, String value, DeviceRgb bg, DeviceRgb fg) {
        Cell cell = new Cell()
                .add(new Paragraph(value)
                        .setFont(fontBold).setFontSize(28)
                        .setFontColor(fg)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(0))
                .add(new Paragraph(label)
                        .setFont(fontRegular).setFontSize(9)
                        .setFontColor(fg)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setPadding(10)
                .setMargin(3)
                .setBorderRadius(new BorderRadius(6));
        return cell;
    }

    // ── Tableau principal ────────────────────────────────────────────────────
    private void addTable(Document document, List<EvaluationRisque> evaluations) {
        // Largeurs relatives des colonnes (total = 100)
        float[] colWidths = {5f, 6f, 7f, 10f, 9f, 28f, 11f, 13f};
        Table table = new Table(UnitValue.createPercentArray(colWidths))
                .useAllAvailableWidth()
                .setFontSize(8.5f);

        // En-têtes
        String[] headers = {"ID", "Projet", "Score", "Niveau Risque",
                "Fiabilité", "Facteur Principal", "Recommandation", "Date Évaluation"};

        for (String h : headers) {
            table.addHeaderCell(
                    new Cell()
                            .add(new Paragraph(h).setFont(fontBold).setFontSize(9).setFontColor(COLOR_TEXT_LIGHT))
                            .setBackgroundColor(COLOR_PRIMARY)
                            .setBorderBottom(new SolidBorder(COLOR_ACCENT, 2))
                            .setBorderTop(Border.NO_BORDER)
                            .setBorderLeft(Border.NO_BORDER)
                            .setBorderRight(new SolidBorder(new DeviceRgb(0x2E, 0x7D, 0x32), 0.5f))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                            .setPaddingTop(10).setPaddingBottom(10)
            );
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy\nHH:mm");
        boolean odd = true;

        for (EvaluationRisque e : evaluations) {
            DeviceRgb rowBg = odd ? COLOR_ROW_ODD : COLOR_ROW_EVEN;
            odd = !odd;

            table.addCell(bodyCell(String.valueOf(e.getIdEvaluation()), rowBg, TextAlignment.CENTER, fontBold));
            table.addCell(bodyCell(String.valueOf(e.getIdProjet()), rowBg, TextAlignment.CENTER, fontRegular));
            table.addCell(scoreCell(e.getScoreGlobal(), rowBg));
            table.addCell(niveauCell(e.getNiveauRisque(), rowBg));
            table.addCell(bodyCell(e.getFiabiliteDonnees(), rowBg, TextAlignment.CENTER, fontRegular));
            table.addCell(facteurCell(e.getFacteurPrincipal(), rowBg));
            table.addCell(recommandationCell(e.getRecommandation(), rowBg));
            table.addCell(bodyCell(
                    e.getDateEvaluation() != null ? sdf.format(e.getDateEvaluation()) : "-",
                    rowBg, TextAlignment.CENTER, fontRegular));
        }

        document.add(table);
    }

    // ── Cellule générique ────────────────────────────────────────────────────
    private Cell bodyCell(String text, DeviceRgb bg, TextAlignment align, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "-").setFont(font))
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBorderRight(new SolidBorder(COLOR_BORDER, 0.5f))
                .setTextAlignment(align)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(7);
    }

    // ── Cellule score avec mini barre ─────────────────────────────────────────
    private Cell scoreCell(int score, DeviceRgb bg) {
        // Barre de progression colorée selon le score (0-100)
        DeviceRgb barColor = score < 30 ? COLOR_FAIBLE
                : score < 60 ? COLOR_MOYEN
                : score < 80 ? COLOR_ELEVE
                : COLOR_CRITIQUE;

        Paragraph p = new Paragraph(score + "/100")
                .setFont(fontBold)
                .setFontColor(score >= 80 ? COLOR_CRITIQUE : score >= 60 ? COLOR_ELEVE : COLOR_TEXT_DARK)
                .setTextAlignment(TextAlignment.CENTER);

        return new Cell()
                .add(p)
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBorderRight(new SolidBorder(COLOR_BORDER, 0.5f))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(7);
    }

    // ── Cellule niveau de risque colorée ─────────────────────────────────────
    private Cell niveauCell(String niveau, DeviceRgb rowBg) {
        DeviceRgb bg;
        DeviceRgb fg;
        String emoji;
        switch (niveau != null ? niveau : "") {
            case "Faible":
                bg = COLOR_FAIBLE; fg = COLOR_TEXT_DARK; emoji = "🟢 "; break;
            case "Moyen":
                bg = COLOR_MOYEN; fg = COLOR_TEXT_DARK; emoji = "🟡 "; break;
            case "Élevé":
                bg = COLOR_ELEVE; fg = COLOR_TEXT_LIGHT; emoji = "🟠 "; break;
            case "Critique":
                bg = COLOR_CRITIQUE; fg = COLOR_TEXT_LIGHT; emoji = "🔴 "; break;
            default:
                bg = new DeviceRgb(0xEE, 0xEE, 0xEE); fg = COLOR_TEXT_DARK; emoji = "";
        }

        Paragraph p = new Paragraph(emoji + (niveau != null ? niveau : "-"))
                .setFont(fontBold).setFontSize(8)
                .setFontColor(fg)
                .setTextAlignment(TextAlignment.CENTER);

        Cell inner = new Cell()
                .add(p)
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setBorderRadius(new BorderRadius(4))
                .setPadding(4);

        return new Cell()
                .add(new Table(1).addCell(inner).setBorder(Border.NO_BORDER))
                .setBackgroundColor(rowBg)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBorderRight(new SolidBorder(COLOR_BORDER, 0.5f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(5);
    }

    // ── Cellule facteur principal (tronquée avec note) ────────────────────────
    private Cell facteurCell(String facteur, DeviceRgb bg) {
        String display = (facteur != null && facteur.length() > 80)
                ? facteur.substring(0, 77) + "..." : (facteur != null ? facteur : "-");
        return new Cell()
                .add(new Paragraph(display).setFont(fontRegular).setFontSize(8))
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBorderRight(new SolidBorder(COLOR_BORDER, 0.5f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(7);
    }

    // ── Cellule recommandation ─────────────────────────────────────────────────
    private Cell recommandationCell(int rec, DeviceRgb rowBg) {
        String label;
        DeviceRgb color;
        switch (rec) {
            case 0: label = "Aucune";           color = COLOR_FAIBLE; break;
            case 1: label = "Surveillance";     color = COLOR_MOYEN;  break;
            case 2: label = "Action immédiate"; color = COLOR_CRITIQUE; break;
            default: label = "Inconnu";         color = new DeviceRgb(0xCC, 0xCC, 0xCC);
        }

        Paragraph p = new Paragraph(label)
                .setFont(fontBold).setFontSize(7.5f)
                .setFontColor(rec == 2 ? COLOR_TEXT_LIGHT : COLOR_TEXT_DARK)
                .setTextAlignment(TextAlignment.CENTER);

        Cell inner = new Cell()
                .add(p)
                .setBackgroundColor(color)
                .setBorder(Border.NO_BORDER)
                .setBorderRadius(new BorderRadius(4))
                .setPadding(4);

        return new Cell()
                .add(new Table(1).addCell(inner).setBorder(Border.NO_BORDER))
                .setBackgroundColor(rowBg)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(COLOR_BORDER, 0.5f))
                .setBorderRight(new SolidBorder(COLOR_BORDER, 0.5f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(5);
    }

    // ── Note de bas de tableau ────────────────────────────────────────────────
    private void addFooterNote(Document document) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph(
                "* Ce rapport a été généré automatiquement par Gestion Agricole Pro. "
                        + "Les données reflètent l'état au moment de l'export.")
                .setFont(fontRegular).setFontSize(7.5f)
                .setFontColor(COLOR_SUBTITLE)
                .setItalic());
    }

    // ── Gestionnaire pied de page ─────────────────────────────────────────────
    private static class FooterHandler implements IEventHandler {
        private final PdfDocument pdfDoc;
        private final PdfFont    fontRegular;
        private final PdfFont    fontBold;
        private final DeviceRgb  color;

        FooterHandler(PdfDocument pdfDoc, PdfFont fontRegular, PdfFont fontBold, DeviceRgb color) {
            this.pdfDoc      = pdfDoc;
            this.fontRegular = fontRegular;
            this.fontBold    = fontBold;
            this.color       = color;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfPage   page   = docEvent.getPage();
            PdfCanvas canvas = new PdfCanvas(page);
            Rectangle rect   = page.getPageSize();

            float y = 25;
            // Ligne séparatrice
            canvas.setStrokeColor(color)
                    .setLineWidth(0.8f)
                    .moveTo(40, y + 12)
                    .lineTo(rect.getWidth() - 40, y + 12)
                    .stroke();

            // Texte gauche
            canvas.beginText()
                    .setFontAndSize(fontRegular, 7.5f)
                    .setColor(color, true)
                    .moveText(40, y)
                    .showText("Gestion Agricole Pro  |  Rapport Confidentiel")
                    .endText();

            // Numéro de page centré
            int pageNum   = pdfDoc.getPageNumber(page);
            int pageTotal = pdfDoc.getNumberOfPages();
            String pageStr = "Page " + pageNum + " / " + pageTotal;
            canvas.beginText()
                    .setFontAndSize(fontBold, 7.5f)
                    .setColor(color, true)
                    .moveText(rect.getWidth() / 2 - 20, y)
                    .showText(pageStr)
                    .endText();

            // Date à droite
            String dateStr = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            canvas.beginText()
                    .setFontAndSize(fontRegular, 7.5f)
                    .setColor(color, true)
                    .moveText(rect.getWidth() - 120, y)
                    .showText("Exporté le " + dateStr)
                    .endText();

            canvas.release();
        }
    }
}