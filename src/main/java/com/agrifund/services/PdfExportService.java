package com.agrifund.services;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import com.agrifund.entities.AnalyseRisqueAgricole;
import com.agrifund.entities.Banque;
import com.agrifund.entities.DonneesSatellite;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PdfExportService {

    // Couleurs
    private static final DeviceRgb PRIMARY = new DeviceRgb(8, 150, 71);       // Vert
    private static final DeviceRgb SECONDARY = new DeviceRgb(225, 179, 35);   // Jaune
    private static final DeviceRgb DARK = new DeviceRgb(33, 37, 41);          // Noir
    private static final DeviceRgb GRAY = new DeviceRgb(108, 117, 125);       // Gris
    private static final DeviceRgb LIGHT = new DeviceRgb(248, 249, 250);      // Gris clair
    private static final DeviceRgb DANGER = new DeviceRgb(220, 53, 69);       // Rouge
    private static final DeviceRgb WARNING = new DeviceRgb(255, 193, 7);      // Jaune warning
    private static final DeviceRgb WARNING_ORANGE = new DeviceRgb(253, 126, 20); // Orange
    private static final DeviceRgb SUCCESS = new DeviceRgb(40, 167, 69);      // Vert succès

    private PdfFont bold;
    private PdfFont regular;

    /**
     * Exporte l'analyse en PDF
     */
    public File exportAnalyse(DonneesSatellite data, AnalyseRisqueAgricole analyse,
                              Banque banque, String filePath) throws Exception {

        System.out.println("📄 Génération du rapport PDF...");

        // Créer le dossier si nécessaire
        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        // Initialiser le document PDF
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 40, 40, 40);

        // Charger les polices
        bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        try {
            // 1. En-tête avec logo et infos banque
            addHeader(doc, banque);

            // 2. Titre du rapport
            addTitle(doc, analyse.getRegion());

            // 3. Informations de localisation
            addLocationInfo(doc, data, analyse);

            // 4. Score de risque avec barre visuelle
            addRiskScore(doc, analyse);

            // 5. Données climatiques
            addClimateData(doc, data);

            // 6. Analyse détaillée (facteurs + recommandations)
            addDetailedAnalysis(doc, analyse);

            // 7. Informations techniques
            addTechnicalInfo(doc);

            // 8. Pied de page
            addFooter(doc);

            doc.close();

            System.out.println("✅ Rapport PDF généré avec succès: " + filePath);
            return file;

        } catch (Exception e) {
            doc.close();
            throw e;
        }
    }

    /**
     * Ajoute l'en-tête du document
     */
    private void addHeader(Document doc, Banque banque) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth();

        // Colonne gauche - Logo et nom de l'application
        Cell leftCell = new Cell().setBorder(Border.NO_BORDER);

        Paragraph appTitle = new Paragraph()
                .add(new Text("🌾 AgriFund").setFont(bold).setFontSize(24).setFontColor(PRIMARY));
        leftCell.add(appTitle);

        Paragraph appSubtitle = new Paragraph("Plateforme de financement agricole")
                .setFont(regular).setFontSize(10).setFontColor(GRAY);
        leftCell.add(appSubtitle);

        // Colonne droite - Informations de la banque
        Cell rightCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);

        if (banque != null) {
            // ✅ CORRECTION: Utiliser getNom() hérité de Utilisateur
            String nomBanque = banque.getNom();
            if (nomBanque != null && !nomBanque.isEmpty()) {
                rightCell.add(new Paragraph(nomBanque)
                        .setFont(bold).setFontSize(14).setFontColor(DARK));
            }

            // Code banque
            String codeBanque = banque.getCodeBanque();
            if (codeBanque != null && !codeBanque.isEmpty()) {
                rightCell.add(new Paragraph("Code: " + codeBanque)
                        .setFont(regular).setFontSize(10).setFontColor(GRAY));
            }

            // Email (hérité de Utilisateur)
            String email = banque.getEmail();
            if (email != null && !email.isEmpty()) {
                rightCell.add(new Paragraph(email)
                        .setFont(regular).setFontSize(9).setFontColor(GRAY));
            }

            // Site web
            String siteWeb = banque.getSiteWeb();
            if (siteWeb != null && !siteWeb.isEmpty()) {
                rightCell.add(new Paragraph(siteWeb)
                        .setFont(regular).setFontSize(9).setFontColor(PRIMARY));
            }
        } else {
            rightCell.add(new Paragraph("Banque")
                    .setFont(bold).setFontSize(14).setFontColor(DARK));
        }

        header.addCell(leftCell);
        header.addCell(rightCell);

        doc.add(header);

        // Ligne de séparation
        doc.add(new LineSeparator(new SolidLine(1.5f))
                .setFontColor(PRIMARY)
                .setMarginTop(10)
                .setMarginBottom(20));
    }

    /**
     * Ajoute le titre du rapport
     */
    private void addTitle(Document doc, String region) {
        // Titre principal
        Paragraph title = new Paragraph("RAPPORT D'ANALYSE SATELLITAIRE")
                .setFont(bold)
                .setFontSize(22)
                .setFontColor(DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        doc.add(title);

        // Sous-titre avec région
        String regionText = (region != null && !region.isEmpty()) ? region : "Position non spécifiée";
        Paragraph subtitle = new Paragraph("🛰️ Région analysée: " + regionText)
                .setFont(regular)
                .setFontSize(14)
                .setFontColor(GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        doc.add(subtitle);

        // Date de génération
        String dateStr = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH));
        Paragraph date = new Paragraph("Rapport généré le " + dateStr)
                .setFont(regular)
                .setFontSize(10)
                .setFontColor(GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(25);
        doc.add(date);
    }

    /**
     * Ajoute les informations de localisation
     */
    private void addLocationInfo(Document doc, DonneesSatellite data, AnalyseRisqueAgricole analyse) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        // Coordonnées
        Cell coordCell = new Cell()
                .setBackgroundColor(LIGHT)
                .setPadding(12)
                .setBorder(Border.NO_BORDER);

        coordCell.add(new Paragraph("📍 Coordonnées GPS")
                .setFont(bold).setFontSize(11).setFontColor(DARK));
        coordCell.add(new Paragraph("Latitude: " + String.format(Locale.US, "%.4f", data.getLatitude()))
                .setFont(regular).setFontSize(10).setFontColor(GRAY));
        coordCell.add(new Paragraph("Longitude: " + String.format(Locale.US, "%.4f", data.getLongitude()))
                .setFont(regular).setFontSize(10).setFontColor(GRAY));

        // Période d'analyse
        Cell periodCell = new Cell()
                .setBackgroundColor(LIGHT)
                .setPadding(12)
                .setBorder(Border.NO_BORDER);

        periodCell.add(new Paragraph("📅 Période d'analyse")
                .setFont(bold).setFontSize(11).setFontColor(DARK));

        if (data.getDateMesure() != null) {
            periodCell.add(new Paragraph("Date: " + data.getDateMesure()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(regular).setFontSize(10).setFontColor(GRAY));
        }

        if (analyse.getDateAnalyse() != null) {
            periodCell.add(new Paragraph("Analysé le: " + analyse.getDateAnalyse()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFont(regular).setFontSize(10).setFontColor(GRAY));
        }

        infoTable.addCell(coordCell);
        infoTable.addCell(periodCell);

        doc.add(infoTable);
    }

    /**
     * Ajoute le score de risque avec barre de progression
     */
    private void addRiskScore(Document doc, AnalyseRisqueAgricole analyse) {
        int score = analyse.getScoreRisque();
        String niveau = analyse.getNiveauRisque();
        DeviceRgb scoreColor = getScoreColor(score);

        // Titre de section
        doc.add(new Paragraph("⚠️ ÉVALUATION DU RISQUE")
                .setFont(bold).setFontSize(14).setFontColor(DARK).setMarginBottom(10));

        // Tableau score
        Table scoreTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        // Score principal
        Cell scoreCell = new Cell()
                .setBackgroundColor(LIGHT)
                .setPadding(20)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        scoreCell.add(new Paragraph("SCORE DE RISQUE")
                .setFont(bold).setFontSize(10).setFontColor(GRAY));
        scoreCell.add(new Paragraph(String.valueOf(score))
                .setFont(bold).setFontSize(48).setFontColor(scoreColor));
        scoreCell.add(new Paragraph("/ 100")
                .setFont(regular).setFontSize(14).setFontColor(GRAY));
        scoreCell.add(new Paragraph(formatNiveau(niveau))
                .setFont(bold).setFontSize(14).setFontColor(scoreColor).setMarginTop(5));

        // Barre de progression et légende
        Cell barCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // Barre de progression (4 segments)
        Table progressBar = new Table(4).useAllAvailableWidth();

        String[] niveaux = {"FAIBLE", "MOYEN", "ÉLEVÉ", "CRITIQUE"};
        DeviceRgb[] colors = {SUCCESS, WARNING, WARNING_ORANGE, DANGER};
        String[] plages = {"0-24", "25-49", "50-74", "75-100"};

        for (int i = 0; i < 4; i++) {
            boolean isActive = isScoreInRange(score, i);

            Cell segment = new Cell()
                    .setBackgroundColor(isActive ? colors[i] : LIGHT)
                    .setPadding(12)
                    .setTextAlignment(TextAlignment.CENTER);

            segment.add(new Paragraph(niveaux[i])
                    .setFont(bold).setFontSize(9)
                    .setFontColor(isActive ? ColorConstants.WHITE : GRAY));
            segment.add(new Paragraph(plages[i])
                    .setFont(regular).setFontSize(8)
                    .setFontColor(isActive ? ColorConstants.WHITE : GRAY));

            progressBar.addCell(segment);
        }

        barCell.add(progressBar);

        // Légende
        barCell.add(new Paragraph("\n📊 Interprétation du score:")
                .setFont(bold).setFontSize(10).setFontColor(DARK).setMarginTop(10));
        barCell.add(new Paragraph("• 0-24: Conditions favorables pour l'agriculture")
                .setFont(regular).setFontSize(9).setFontColor(GRAY));
        barCell.add(new Paragraph("• 25-49: Risque modéré, surveillance recommandée")
                .setFont(regular).setFontSize(9).setFontColor(GRAY));
        barCell.add(new Paragraph("• 50-74: Risque élevé, précautions nécessaires")
                .setFont(regular).setFontSize(9).setFontColor(GRAY));
        barCell.add(new Paragraph("• 75-100: Risque critique, évaluation terrain requise")
                .setFont(regular).setFontSize(9).setFontColor(GRAY));

        scoreTable.addCell(scoreCell);
        scoreTable.addCell(barCell);

        doc.add(scoreTable);
    }

    /**
     * Ajoute les données climatiques
     */
    private void addClimateData(Document doc, DonneesSatellite data) {
        doc.add(new Paragraph("📊 DONNÉES CLIMATIQUES")
                .setFont(bold).setFontSize(14).setFontColor(PRIMARY).setMarginTop(10).setMarginBottom(10));

        // Tableau des données
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 25, 45}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // En-têtes
        table.addHeaderCell(createHeaderCell("Indicateur"));
        table.addHeaderCell(createHeaderCell("Valeur"));
        table.addHeaderCell(createHeaderCell("Interprétation"));

        // Température
        table.addCell(createDataCell("🌡️ Température moyenne"));
        table.addCell(createValueCell(data.getTemperatureMoyenne() != null ?
                String.format(Locale.US, "%.1f °C", data.getTemperatureMoyenne()) : "N/A"));
        table.addCell(createInterpCell(interpretTemperature(data.getTemperatureMoyenne())));

        // Précipitations
        table.addCell(createDataCell("🌧️ Précipitations"));
        table.addCell(createValueCell(data.getPrecipitation() != null ?
                String.format(Locale.US, "%.1f mm", data.getPrecipitation()) : "N/A"));
        table.addCell(createInterpCell(interpretPrecipitation(data.getPrecipitation())));

        // Humidité
        table.addCell(createDataCell("💧 Humidité relative"));
        table.addCell(createValueCell(data.getHumidite() != null ?
                String.format(Locale.US, "%.1f %%", data.getHumidite()) : "N/A"));
        table.addCell(createInterpCell(interpretHumidity(data.getHumidite())));

        // NDVI
        table.addCell(createDataCell("🌿 Indice de végétation (NDVI)"));
        table.addCell(createValueCell(data.getNdvi() != null ?
                String.format(Locale.US, "%.3f", data.getNdvi()) : "N/A"));
        table.addCell(createInterpCell(interpretNDVI(data.getNdvi())));

        // Indice de sécheresse
        table.addCell(createDataCell("☀️ Indice de sécheresse"));
        table.addCell(createValueCell(data.getIndiceSecheresse() != null ?
                String.format(Locale.US, "%.0f / 100", data.getIndiceSecheresse()) : "N/A"));
        table.addCell(createInterpCell(interpretDrought(data.getIndiceSecheresse())));

        doc.add(table);
    }

    /**
     * Ajoute l'analyse détaillée (facteurs de risque + recommandations)
     */
    private void addDetailedAnalysis(Document doc, AnalyseRisqueAgricole analyse) {
        // Facteurs de risque
        doc.add(new Paragraph("⚠️ FACTEURS DE RISQUE IDENTIFIÉS")
                .setFont(bold).setFontSize(14).setFontColor(new DeviceRgb(133, 100, 4)).setMarginBottom(10));

        Table factorsTable = new Table(1).useAllAvailableWidth();
        Cell factorsCell = new Cell()
                .setBackgroundColor(new DeviceRgb(255, 243, 205))
                .setPadding(15)
                .setBorder(new SolidBorder(WARNING, 1));

        String facteurs = analyse.getFacteursRisque();
        if (facteurs != null && !facteurs.trim().isEmpty()) {
            for (String ligne : facteurs.split("\n")) {
                if (!ligne.trim().isEmpty()) {
                    factorsCell.add(new Paragraph(ligne.trim())
                            .setFont(regular).setFontSize(10)
                            .setFontColor(new DeviceRgb(133, 100, 4)));
                }
            }
        } else {
            factorsCell.add(new Paragraph("Aucun facteur de risque majeur identifié")
                    .setFont(regular).setFontSize(10)
                    .setFontColor(new DeviceRgb(133, 100, 4)));
        }

        factorsTable.addCell(factorsCell);
        doc.add(factorsTable);

        doc.add(new Paragraph().setMarginBottom(15));

        // Recommandations
        doc.add(new Paragraph("💡 RECOMMANDATIONS")
                .setFont(bold).setFontSize(14).setFontColor(new DeviceRgb(21, 87, 36)).setMarginBottom(10));

        Table recoTable = new Table(1).useAllAvailableWidth();
        Cell recoCell = new Cell()
                .setBackgroundColor(new DeviceRgb(212, 237, 218))
                .setPadding(15)
                .setBorder(new SolidBorder(SUCCESS, 1));

        String recommandations = analyse.getRecommandations();
        if (recommandations != null && !recommandations.trim().isEmpty()) {
            for (String ligne : recommandations.split("\n")) {
                if (!ligne.trim().isEmpty()) {
                    recoCell.add(new Paragraph(ligne.trim())
                            .setFont(regular).setFontSize(10)
                            .setFontColor(new DeviceRgb(21, 87, 36)));
                }
            }
        } else {
            recoCell.add(new Paragraph("Aucune recommandation spécifique")
                    .setFont(regular).setFontSize(10)
                    .setFontColor(new DeviceRgb(21, 87, 36)));
        }

        recoTable.addCell(recoCell);
        doc.add(recoTable);
    }

    /**
     * Ajoute les informations techniques
     */
    private void addTechnicalInfo(Document doc) {
        doc.add(new Paragraph().setMarginBottom(20));

        doc.add(new Paragraph("ℹ️ INFORMATIONS TECHNIQUES")
                .setFont(bold).setFontSize(12).setFontColor(GRAY).setMarginBottom(10));

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();

        // Source des données
        Cell sourceCell = new Cell().setBorder(Border.NO_BORDER).setPadding(10);
        sourceCell.add(new Paragraph("📡 Source des données")
                .setFont(bold).setFontSize(10).setFontColor(DARK));
        sourceCell.add(new Paragraph("NASA POWER API")
                .setFont(regular).setFontSize(9).setFontColor(GRAY));
        sourceCell.add(new Paragraph("power.larc.nasa.gov")
                .setFont(regular).setFontSize(8).setFontColor(PRIMARY));

        // Méthodologie
        Cell methodCell = new Cell().setBorder(Border.NO_BORDER).setPadding(10);
        methodCell.add(new Paragraph("🔬 Méthodologie")
                .setFont(bold).setFontSize(10).setFontColor(DARK));
        methodCell.add(new Paragraph("Données satellitaires journalières")
                .setFont(regular).setFontSize(9).setFontColor(GRAY));
        methodCell.add(new Paragraph("Résolution spatiale: 0.5° × 0.5° (~55 km)")
                .setFont(regular).setFontSize(8).setFontColor(GRAY));

        infoTable.addCell(sourceCell);
        infoTable.addCell(methodCell);

        doc.add(infoTable);

        // Avertissement
        doc.add(new Paragraph()
                .add(new Text("⚠️ Avertissement: ").setFont(bold).setFontSize(8).setFontColor(GRAY))
                .add(new Text("Ce rapport est généré automatiquement à partir de données satellitaires. " +
                        "Il est fourni à titre indicatif et ne constitue pas un conseil financier définitif. " +
                        "Une évaluation sur le terrain est recommandée pour toute décision d'investissement importante.")
                        .setFont(regular).setFontSize(8).setFontColor(GRAY))
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMarginTop(15));
    }

    /**
     * Ajoute le pied de page
     */
    private void addFooter(Document doc) {
        doc.add(new LineSeparator(new SolidLine(0.5f))
                .setFontColor(GRAY)
                .setMarginTop(30)
                .setMarginBottom(10));

        Table footer = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();

        Cell leftFooter = new Cell().setBorder(Border.NO_BORDER);
        leftFooter.add(new Paragraph("🌾 AgriFinance - Plateforme de financement agricole")
                .setFont(regular).setFontSize(8).setFontColor(GRAY));
        leftFooter.add(new Paragraph("Rapport généré automatiquement")
                .setFont(regular).setFontSize(7).setFontColor(GRAY));

        Cell rightFooter = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        rightFooter.add(new Paragraph(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setFont(regular).setFontSize(8).setFontColor(GRAY));
        rightFooter.add(new Paragraph("© " + LocalDateTime.now().getYear() + " AgriFinance")
                .setFont(regular).setFontSize(7).setFontColor(GRAY));

        footer.addCell(leftFooter);
        footer.addCell(rightFooter);

        doc.add(footer);
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private Cell createHeaderCell(String text) {
        return new Cell()
                .setBackgroundColor(PRIMARY)
                .setPadding(10)
                .add(new Paragraph(text)
                        .setFont(bold)
                        .setFontSize(10)
                        .setFontColor(ColorConstants.WHITE));
    }

    private Cell createDataCell(String text) {
        return new Cell()
                .setPadding(10)
                .setBackgroundColor(LIGHT)
                .add(new Paragraph(text)
                        .setFont(regular)
                        .setFontSize(10)
                        .setFontColor(DARK));
    }

    private Cell createValueCell(String text) {
        return new Cell()
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(text)
                        .setFont(bold)
                        .setFontSize(11)
                        .setFontColor(DARK));
    }

    private Cell createInterpCell(String text) {
        return new Cell()
                .setPadding(10)
                .add(new Paragraph(text)
                        .setFont(regular)
                        .setFontSize(9)
                        .setFontColor(GRAY));
    }

    private DeviceRgb getScoreColor(int score) {
        if (score >= 75) return DANGER;
        if (score >= 50) return WARNING_ORANGE;
        if (score >= 25) return WARNING;
        return SUCCESS;
    }

    private boolean isScoreInRange(int score, int rangeIndex) {
        switch (rangeIndex) {
            case 0: return score < 25;
            case 1: return score >= 25 && score < 50;
            case 2: return score >= 50 && score < 75;
            case 3: return score >= 75;
            default: return false;
        }
    }

    private String formatNiveau(String niveau) {
        if (niveau == null) return "INCONNU";
        switch (niveau.toLowerCase()) {
            case "critique": return "🔴 CRITIQUE";
            case "eleve": return "🟠 ÉLEVÉ";
            case "moyen": return "🟡 MOYEN";
            case "faible": return "🟢 FAIBLE";
            default: return niveau.toUpperCase();
        }
    }

    // ==================== INTERPRÉTATIONS ====================

    private String interpretTemperature(Double temp) {
        if (temp == null) return "Données non disponibles";
        if (temp > 40) return "🔴 Chaleur extrême - Stress thermique pour les cultures";
        if (temp > 35) return "🟠 Température très élevée - Vigilance requise";
        if (temp > 28) return "🟡 Température chaude - Normal pour l'été";
        if (temp > 15) return "🟢 Température idéale pour la plupart des cultures";
        if (temp > 5) return "🟡 Température fraîche - Certaines cultures sensibles";
        return "🔴 Risque de gel - Protection nécessaire";
    }

    private String interpretPrecipitation(Double precip) {
        if (precip == null) return "Données non disponibles";
        if (precip < 10) return "🔴 Déficit hydrique sévère - Irrigation urgente";
        if (precip < 30) return "🟠 Précipitations insuffisantes - Irrigation recommandée";
        if (precip < 80) return "🟢 Précipitations normales - Conditions favorables";
        if (precip < 150) return "🟡 Précipitations abondantes - Surveiller le drainage";
        return "🔴 Risque d'inondation - Précautions nécessaires";
    }

    private String interpretHumidity(Double humidity) {
        if (humidity == null) return "Données non disponibles";
        if (humidity < 30) return "🔴 Air très sec - Risque de stress hydrique";
        if (humidity < 50) return "🟠 Humidité faible - Irrigation à surveiller";
        if (humidity < 70) return "🟢 Humidité optimale pour l'agriculture";
        if (humidity < 85) return "🟡 Humidité élevée - Risque de maladies fongiques";
        return "🔴 Humidité excessive - Risque élevé de maladies";
    }

    private String interpretNDVI(Double ndvi) {
        if (ndvi == null) return "Données non disponibles";
        if (ndvi < 0.1) return "🔴 Sol nu ou végétation absente";
        if (ndvi < 0.2) return "🟠 Végétation très faible ou en stress sévère";
        if (ndvi < 0.4) return "🟡 Végétation clairsemée ou en stress modéré";
        if (ndvi < 0.6) return "🟢 Végétation modérée en bonne santé";
        if (ndvi < 0.8) return "🟢 Végétation dense et saine";
        return "🟢 Végétation très dense et vigoureuse";
    }

    private String interpretDrought(Double index) {
        if (index == null) return "Données non disponibles";
        if (index < 25) return "🟢 Conditions hydriques normales";
        if (index < 50) return "🟡 Sécheresse modérée - Surveillance recommandée";
        if (index < 75) return "🟠 Sécheresse sévère - Irrigation nécessaire";
        return "🔴 Sécheresse extrême - Mesures urgentes requises";
    }
}