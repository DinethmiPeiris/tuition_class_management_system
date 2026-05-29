package com.tuition.new_tuition.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tuition.new_tuition.dto.ProgressReportDTO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TeacherProgressReportPdfService {

    public byte[] generateProgressReportPdf(List<ProgressReportDTO> reports) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4.rotate(), 28, 28, 30, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            Color brandBlue = new Color(53, 87, 214);
            Color brandPurple = new Color(72, 52, 212);
            Color headerBg = new Color(240, 244, 255);
            Color tableHeaderBg = new Color(53, 87, 214);
            Color borderColor = new Color(220, 226, 232);
            Color lightText = new Color(100, 116, 139);
            Color zebra = new Color(248, 250, 252);
            Color success = new Color(22, 108, 67);
            Color warning = new Color(148, 98, 0);
            Color danger = new Color(176, 42, 55);

            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, brandBlue);
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, lightText);
            Font sectionTitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, brandPurple);
            Font cardLabelFont = new Font(Font.HELVETICA, 9, Font.NORMAL, lightText);
            Font cardValueFont = new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK);
            Font tableHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
            Font boldBodyFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, lightText);

            addTitleSection(document, titleFont, subtitleFont);
            addSummarySection(document, reports, headerBg, borderColor, cardLabelFont, cardValueFont);
            addReportTable(document, reports, tableHeaderBg, borderColor, zebra, tableHeaderFont, bodyFont, boldBodyFont, success, warning, danger);
            addFooter(document, footerFont);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate teacher progress report PDF", e);
        }
    }

    private void addTitleSection(Document document,
                                 Font titleFont,
                                 Font subtitleFont) throws Exception {

        Paragraph title = new Paragraph("Student Progress Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6f);
        document.add(title);

        Paragraph subtitle = new Paragraph("Subject-wise performance analysis based on exam results", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(4f);
        document.add(subtitle);

        String generatedDate = "Generated on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        Paragraph dateLine = new Paragraph(generatedDate, subtitleFont);
        dateLine.setAlignment(Element.ALIGN_CENTER);
        dateLine.setSpacingAfter(18f);
        document.add(dateLine);
    }

    private void addSummarySection(Document document,
                                   List<ProgressReportDTO> reports,
                                   Color headerBg,
                                   Color borderColor,
                                   Font labelFont,
                                   Font valueFont) throws Exception {

        int totalRows = reports.size();
        long excellent = reports.stream().filter(r -> r.getAveragePercentage() >= 75).count();
        long average = reports.stream().filter(r -> r.getAveragePercentage() >= 50 && r.getAveragePercentage() < 75).count();
        long weak = reports.stream().filter(r -> r.getAveragePercentage() < 50).count();

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(18f);
        summaryTable.setWidths(new float[]{1f, 1f, 1f, 1f});

        summaryTable.addCell(createSummaryCell("Total Report Rows", String.valueOf(totalRows), headerBg, borderColor, labelFont, valueFont));
        summaryTable.addCell(createSummaryCell("Excellent Performance", String.valueOf(excellent), headerBg, borderColor, labelFont, valueFont));
        summaryTable.addCell(createSummaryCell("Average Performance", String.valueOf(average), headerBg, borderColor, labelFont, valueFont));
        summaryTable.addCell(createSummaryCell("Weak Performance", String.valueOf(weak), headerBg, borderColor, labelFont, valueFont));

        document.add(summaryTable);
    }

    private PdfPCell createSummaryCell(String label,
                                       String value,
                                       Color bgColor,
                                       Color borderColor,
                                       Font labelFont,
                                       Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(borderColor);
        cell.setPadding(14f);
        cell.setBorderWidth(1f);
        cell.setMinimumHeight(62f);

        Paragraph p1 = new Paragraph(label, labelFont);
        p1.setSpacingAfter(8f);

        Paragraph p2 = new Paragraph(value, valueFont);
        p2.setSpacingAfter(0f);

        cell.addElement(p1);
        cell.addElement(p2);

        return cell;
    }

    private void addReportTable(Document document,
                                List<ProgressReportDTO> reports,
                                Color tableHeaderBg,
                                Color borderColor,
                                Color zebra,
                                Font tableHeaderFont,
                                Font bodyFont,
                                Font boldBodyFont,
                                Color success,
                                Color warning,
                                Color danger) throws Exception {

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 2.0f, 2.0f, 1.0f, 1.0f, 1.2f, 0.8f, 0.8f, 0.8f});

        addHeaderCell(table, "#",            tableHeaderBg, borderColor, tableHeaderFont);
        addHeaderCell(table, "Subject",      tableHeaderBg, borderColor, tableHeaderFont);
        addHeaderCell(table, "Grade/Batch",  tableHeaderBg, borderColor, tableHeaderFont);
        addHeaderCell(table, "Students",     tableHeaderBg, borderColor, tableHeaderFont);
        addHeaderCell(table, "Total Exams",  tableHeaderBg, borderColor, tableHeaderFont);
        addHeaderCell(table, "Average %",    tableHeaderBg, borderColor, tableHeaderFont);
        addHeaderCell(table, "Pass",         tableHeaderBg, borderColor, tableHeaderFont);
        addHeaderCell(table, "Fail",         tableHeaderBg, borderColor, tableHeaderFont);
        addHeaderCell(table, "Performance",  tableHeaderBg, borderColor, tableHeaderFont);

        int count = 1;
        for (ProgressReportDTO report : reports) {
            Color rowBg = (count % 2 == 0) ? zebra : Color.WHITE;

            addBodyCell(table, String.valueOf(count),                        rowBg, borderColor, bodyFont,     Element.ALIGN_CENTER);
            addBodyCell(table, safe(report.getSubject()),                    rowBg, borderColor, boldBodyFont, Element.ALIGN_LEFT);
            addBodyCell(table, safe(report.getBatchName()),                  rowBg, borderColor, bodyFont,     Element.ALIGN_LEFT);
            addBodyCell(table, String.valueOf(report.getStudentCount()),     rowBg, borderColor, bodyFont,     Element.ALIGN_CENTER);
            addBodyCell(table, String.valueOf(report.getTotalExams()),       rowBg, borderColor, bodyFont,     Element.ALIGN_CENTER);
            addBodyCell(table, String.format("%.2f%%", report.getAveragePercentage()), rowBg, borderColor, boldBodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, String.valueOf(report.getPassCount()),        rowBg, borderColor, bodyFont,     Element.ALIGN_CENTER);
            addBodyCell(table, String.valueOf(report.getFailCount()),        rowBg, borderColor, bodyFont,     Element.ALIGN_CENTER);

            String performance;
            Font performanceFont;
            if (report.getAveragePercentage() >= 75) {
                performance = "Excellent";
                performanceFont = new Font(Font.HELVETICA, 9, Font.BOLD, success);
            } else if (report.getAveragePercentage() >= 50) {
                performance = "Average";
                performanceFont = new Font(Font.HELVETICA, 9, Font.BOLD, warning);
            } else {
                performance = "Weak";
                performanceFont = new Font(Font.HELVETICA, 9, Font.BOLD, danger);
            }
            addBodyCell(table, performance, rowBg, borderColor, performanceFont, Element.ALIGN_CENTER);
            count++;
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table,
                               String text,
                               Color bgColor,
                               Color borderColor,
                               Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(10f);
        cell.setBorderColor(borderColor);
        cell.setBorderWidth(1f);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table,
                             String text,
                             Color bgColor,
                             Color borderColor,
                             Font font,
                             int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBorderColor(borderColor);
        cell.setBorderWidth(0.8f);
        table.addCell(cell);
    }

    private void addFooter(Document document, Font footerFont) throws Exception {
        Paragraph footer = new Paragraph("This report was generated automatically by the Tuition Management System.", footerFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(14f);
        document.add(footer);
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
