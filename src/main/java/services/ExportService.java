package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import entities.releve_terrain;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

public class ExportService {

    public void exportPDF(List<releve_terrain> releves) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document,
                new FileOutputStream("rapport_iot.pdf"));

        document.open();


        Font titleFont = new Font(Font.FontFamily.HELVETICA,
                18, Font.BOLD,
                new BaseColor(7, 106, 57)); // #076A39

        Paragraph title = new Paragraph(
                "RAPPORT RELEVÉS IoT",
                titleFont);

        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Date : "
                + LocalDate.now()));
        document.add(new Paragraph(" "));


        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        float[] columnWidths = {3f, 2f, 2f, 2f};
        table.setWidths(columnWidths);


        addHeader(table, "Type");
        addHeader(table, "Valeur");
        addHeader(table, "Unité");
        addHeader(table, "Capteur");

        boolean alternate = false;

        for (releve_terrain r : releves) {

            BaseColor bg = alternate
                    ? new BaseColor(245, 245, 240) // #F5F5F0
                    : BaseColor.WHITE;

            addCell(table, r.getTypeMesure(), bg);
            addCell(table,
                    String.valueOf(r.getValeurMesuree()), bg);
            addCell(table, r.getUnite(), bg);
            addCell(table,
                    String.valueOf(r.getIdCapteur()), bg);

            alternate = !alternate;
        }

        document.add(table);
        document.close();
    }

    private void addHeader(PdfPTable table, String text) {

        Font font = new Font(Font.FontFamily.HELVETICA,
                12, Font.BOLD, BaseColor.WHITE);

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(8, 150, 71)); // #089647
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);

        table.addCell(cell);
    }

    private void addCell(PdfPTable table,
                         String text,
                         BaseColor bg) {

        Font font = new Font(Font.FontFamily.HELVETICA,
                11, Font.NORMAL,
                new BaseColor(6, 8, 6)); // #060806

        PdfPCell cell =
                new PdfPCell(new Phrase(text, font));

        cell.setBackgroundColor(bg);
        cell.setPadding(6);

        table.addCell(cell);
    }
}