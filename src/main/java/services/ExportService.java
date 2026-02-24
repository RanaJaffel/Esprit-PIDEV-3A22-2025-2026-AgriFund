package services;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import entities.releve_terrain;

import java.io.FileOutputStream;
import java.util.List;

public class ExportService {

    public void exportPDF(List<releve_terrain> releves) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document,
                new FileOutputStream("rapport_iot.pdf"));

        document.open();

        document.add(new Paragraph("Rapport IoT Intelligent\n\n"));

        for (releve_terrain r : releves) {
            document.add(new Paragraph(
                    r.getTypeMesure() + " - "
                            + r.getValeurMesuree()
            ));
        }

        document.close();
    }
}