package com.agrifund.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.agrifund.entities.releve_terrain;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

public class ExportService {

    /**
     * Exporte la liste de relevés dans un fichier PDF.
     *
     * @param releves  liste des relevés à exporter
     * @param destFile fichier de destination choisi par l'utilisateur
     */
    public void exportPDF(List<releve_terrain> releves, File destFile) throws Exception {

        Document document = new Document(PageSize.A4);
        PdfWriter writer = null;
        FileOutputStream fos = null;

        try {
            // ── Ouvrir le flux ──────────────────────────────
            fos = new FileOutputStream(destFile);
            writer = PdfWriter.getInstance(document, fos);
            document.open();

            // ── Police avec support UTF-8 (accents) ────────
            BaseFont baseFont = BaseFont.createFont(
                    BaseFont.HELVETICA,
                    BaseFont.CP1252,
                    BaseFont.EMBEDDED
            );

            // ── Titre ──────────────────────────────────────
            Font titleFont = new Font(baseFont, 18, Font.BOLD,
                    new BaseColor(7, 106, 57));

            Paragraph title = new Paragraph("RAPPORT RELEVÉS IoT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // ── Sous-titre date ────────────────────────────
            Font dateFont = new Font(baseFont, 12, Font.NORMAL, BaseColor.DARK_GRAY);
            Paragraph datePara = new Paragraph("Date : " + LocalDate.now(), dateFont);
            datePara.setSpacingAfter(15);
            document.add(datePara);

            // ── Vérification liste vide ────────────────────
            if (releves == null || releves.isEmpty()) {
                Font emptyFont = new Font(baseFont, 12, Font.ITALIC, BaseColor.GRAY);
                document.add(new Paragraph("Aucun relevé à afficher.", emptyFont));
                return;
            }

            // ── Tableau ────────────────────────────────────
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new float[]{3f, 2f, 2f, 2f});

            // En-têtes
            addHeader(table, "Type", baseFont);
            addHeader(table, "Valeur", baseFont);
            addHeader(table, "Unité", baseFont);
            addHeader(table, "Capteur", baseFont);

            // Lignes
            boolean alternate = false;
            for (releve_terrain r : releves) {

                BaseColor bg = alternate
                        ? new BaseColor(245, 245, 240)
                        : BaseColor.WHITE;

                addCell(table, safe(r.getTypeMesure()), bg, baseFont);
                addCell(table, String.valueOf(r.getValeurMesuree()), bg, baseFont);
                addCell(table, safe(r.getUnite()), bg, baseFont);
                addCell(table, String.valueOf(r.getIdCapteur()), bg, baseFont);

                alternate = !alternate;
            }

            document.add(table);

            // ── Pied de page ───────────────────────────────
            Paragraph footer = new Paragraph(
                    "\nTotal relevés : " + releves.size(),
                    new Font(baseFont, 11, Font.BOLD, BaseColor.DARK_GRAY)
            );
            footer.setSpacingBefore(15);
            document.add(footer);

        } finally {
            // ── Fermeture garantie ─────────────────────────
            if (document.isOpen()) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    // ═══════════════════════════════════════════════════
    //  Cellules
    // ═══════════════════════════════════════════════════

    private void addHeader(PdfPTable table, String text, BaseFont baseFont) {

        Font font = new Font(baseFont, 12, Font.BOLD, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        cell.setBackgroundColor(new BaseColor(8, 150, 71));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setBorderColor(BaseColor.WHITE);

        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text,
                         BaseColor bg, BaseFont baseFont) {

        Font font = new Font(baseFont, 11, Font.NORMAL,
                new BaseColor(6, 8, 6));

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(cell);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}