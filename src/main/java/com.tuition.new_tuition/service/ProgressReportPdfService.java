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
import com.tuition.new_tuition.dto.StudentProgressReportDTO;
import com.tuition.new_tuition.dto.StudentReportItemDTO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ProgressReportPdfService {

    public byte[] generateStudentProgressReport(StudentProgressReportDTO report) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 30, 30, 32, 28);
            PdfWriter.getInstance(document, out);
            document.open();

            Color brandBlue = new Color(53, 87, 214);
            Color brandPurple = new Color(72, 52, 212);
            Color cardBg = new Color(245, 247, 255);
            Color borderColor = new Color(220, 226, 232);
            Color tableHeaderBg = new Color(53, 87, 214);
            Color zebra = new Color(248, 250, 252);
            Color muted = new Color(100, 116, 139);
            Color success = new Color(22, 108, 67);
            Color warning = new Color(148, 98, 0);
            Color danger = new Color(176, 42, 55);

            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, brandBlue);
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, muted);
            Font sectionTitleFont = new Font(Font.HELVETICA, 11, Font.BOLD, brandPurple);
            Font cardLabelFont = new Font(Font.HELVETICA, 8, Font.NORMAL, muted);
            Font cardValueFont = new Font(Font.HELVETICA, 15, Font.BOLD, Color.BLACK);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
            Font boldBodyFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, muted);

            addTitleSection(document, report, titleFont, subtitleFont);
            addSummaryCards(document, report, cardBg, borderColor, cardLabelFont, cardValueFont);
            addExamTable(document, report, tableHeaderBg, borderColor, zebra, headerFont, bodyFont, boldBodyFont, success, warning, danger);
            addFooter(document, footerFont);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate student progress report PDF", e);
        }
    }

    private void addTitleSection(Document document,
                                 StudentProgressReportDTO report,
                                 Font titleFont,
                                 Font subtitleFont) throws Exception {

        Paragraph title = new Paragraph("Student Progress Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6f);
        document.add(title);

        Paragraph studentInfo = new Paragraph(
                safe(report.getStudentName()) + " • " + safe(report.getStudentEmail()),
                subtitleFont
        );
        studentInfo.setAlignment(Element.ALIGN_CENTER);
        studentInfo.setSpacingAfter(4f);
        document.add(studentInfo);

        Paragraph dateLine = new Paragraph(
                "Generated on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                subtitleFont
        );
        dateLine.setAlignment(Element.ALIGN_CENTER);
        dateLine.setSpacingAfter(18f);
        document.add(dateLine);
    }

    private void addSummaryCards(Document document,
                                 StudentProgressReportDTO report,
                                 Color cardBg,
                                 Color borderColor,
                                 Font labelFont,
                                 Font valueFont) throws Exception {

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(18f);
        summaryTable.setWidths(new float[]{1f, 1f, 1f, 1f});

        summaryTable.addCell(createSummaryCell("Total Exams", String.valueOf(report.getTotalExams()), cardBg, borderColor, labelFont, valueFont));
        summaryTable.addCell(createSummaryCell("Average Percentage", String.format("%.2f%%", report.getAveragePercentage()), cardBg, borderColor, labelFont, valueFont));
        summaryTable.addCell(createSummaryCell("Passed Exams", String.valueOf(report.getPassCount()), cardBg, borderColor, labelFont, valueFont));
        summaryTable.addCell(createSummaryCell("Failed / Pending", report.getFailCount() + " / " + report.getPendingCount(), cardBg, borderColor, labelFont, valueFont));

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
        cell.setBorderWidth(1f);
        cell.setPadding(14f);
        cell.setMinimumHeight(62f);

        Paragraph p1 = new Paragraph(label, labelFont);
        p1.setSpacingAfter(8f);

        Paragraph p2 = new Paragraph(value, valueFont);

        cell.addElement(p1);
        cell.addElement(p2);
        return cell;
    }

    private void addExamTable(Document document,
                              StudentProgressReportDTO report,
                              Color headerBg,
                              Color borderColor,
                              Color zebra,
                              Font headerFont,
                              Font bodyFont,
                              Font boldBodyFont,
                              Color success,
                              Color warning,
                              Color danger) throws Exception {

        Paragraph sectionTitle = new Paragraph("Exam Result Details", new Font(Font.HELVETICA, 12, Font.BOLD, new Color(72, 52, 212)));
        sectionTitle.setSpacingAfter(10f);
        document.add(sectionTitle);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.2f, 1.2f, 0.9f, 0.9f, 1.0f, 1.0f, 1.3f});

        addHeaderCell(table, "Exam", headerBg, borderColor, headerFont);
        addHeaderCell(table, "Subject", headerBg, borderColor, headerFont);
        addHeaderCell(table, "Date", headerBg, borderColor, headerFont);
        addHeaderCell(table, "Score", headerBg, borderColor, headerFont);
        addHeaderCell(table, "Total", headerBg, borderColor, headerFont);
        addHeaderCell(table, "Percent", headerBg, borderColor, headerFont);
        addHeaderCell(table, "Status", headerBg, borderColor, headerFont);
        addHeaderCell(table, "Remarks", headerBg, borderColor, headerFont);

        int row = 1;
        for (StudentReportItemDTO item : report.getExamItems()) {
            Color rowBg = (row % 2 == 0) ? zebra : Color.WHITE;

            addBodyCell(table, safe(item.getExamName()), rowBg, borderColor, bodyFont, Element.ALIGN_LEFT);
            addBodyCell(table, safe(item.getSubject()), rowBg, borderColor, bodyFont, Element.ALIGN_LEFT);
            addBodyCell(table, safe(item.getExamDate()), rowBg, borderColor, bodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, String.valueOf(item.getScore()), rowBg, borderColor, boldBodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, String.valueOf(item.getTotalMarks()), rowBg, borderColor, bodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, String.format("%.2f%%", item.getPercentage()), rowBg, borderColor, boldBodyFont, Element.ALIGN_CENTER);

            Font statusFont;
            String remarks;

            if ("PASS".equalsIgnoreCase(item.getStatus())) {
                statusFont = new Font(Font.HELVETICA, 9, Font.BOLD, success);
                remarks = "Good";
            } else if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                statusFont = new Font(Font.HELVETICA, 9, Font.BOLD, warning);
                remarks = "Awaiting marking";
            } else {
                statusFont = new Font(Font.HELVETICA, 9, Font.BOLD, danger);
                remarks = "Needs improvement";
            }

            addBodyCell(table, safe(item.getStatus()), rowBg, borderColor, statusFont, Element.ALIGN_CENTER);
            addBodyCell(table, remarks, rowBg, borderColor, bodyFont, Element.ALIGN_LEFT);

            row++;
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
        cell.setPadding(9f);
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
        Paragraph footer = new Paragraph(
                "This report was generated automatically by the Tuition Management System.",
                footerFont
        );
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(14f);
        document.add(footer);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
