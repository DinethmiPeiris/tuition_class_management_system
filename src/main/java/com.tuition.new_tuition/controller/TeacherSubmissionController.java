package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Exam;
import com.tuition.new_tuition.entity.ExamSubmission;
import com.tuition.new_tuition.entity.SubmissionAnswer;
import com.tuition.new_tuition.repository.ExamRepository;
import com.tuition.new_tuition.repository.SubmissionAnswerRepository;
import com.tuition.new_tuition.service.ExamSubmissionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/teacher/submissions")
public class TeacherSubmissionController {

    private final ExamSubmissionService examSubmissionService;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final ExamRepository examRepository;

    public TeacherSubmissionController(ExamSubmissionService examSubmissionService,
                                       SubmissionAnswerRepository submissionAnswerRepository,
                                       ExamRepository examRepository) {
        this.examSubmissionService = examSubmissionService;
        this.submissionAnswerRepository = submissionAnswerRepository;
        this.examRepository = examRepository;
    }




    @GetMapping("/exam/{examId}")
    public String listByExam(@PathVariable("examId") Long examId, Model model, HttpSession session) {
        // Always fetch the exam directly so the page header works even with 0 submissions
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            return "redirect:/exams/list";
        }
        model.addAttribute("exam", examOpt.get());

        List<ExamSubmission> submissions = examSubmissionService.findByExamId(examId);
        model.addAttribute("submissions", submissions);

        return "teacher-submission-list";
    }

    @GetMapping("/exam/{examId}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable("examId") Long examId, HttpSession session) {
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Exam exam = examOpt.get();
        List<ExamSubmission> submissions = examSubmissionService.findByExamId(examId);

        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.lowagie.text.Document doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 40, 40, 50, 50);
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, baos);
            doc.open();

            java.awt.Color primaryBlue   = new java.awt.Color(29, 111, 243);
            java.awt.Color headerBg      = new java.awt.Color(239, 246, 255);
            java.awt.Color rowAlt        = new java.awt.Color(248, 250, 252);
            java.awt.Color passGreen     = new java.awt.Color(22, 163, 74);
            java.awt.Color failRed       = new java.awt.Color(220, 38, 38);
            java.awt.Color pendingOrange = new java.awt.Color(234, 88, 12);
            java.awt.Color mutedGrey     = new java.awt.Color(100, 116, 139);

            com.lowagie.text.Font titleFont  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD,   primaryBlue);
            com.lowagie.text.Font subFont    = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL, mutedGrey);
            com.lowagie.text.Font colHeaderF = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  9, com.lowagie.text.Font.BOLD,   primaryBlue);
            com.lowagie.text.Font cellNormal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  9, com.lowagie.text.Font.NORMAL);
            com.lowagie.text.Font cellBold   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  9, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font passFont   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  9, com.lowagie.text.Font.BOLD,   passGreen);
            com.lowagie.text.Font failFont   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  9, com.lowagie.text.Font.BOLD,   failRed);
            com.lowagie.text.Font pendFont   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA,  9, com.lowagie.text.Font.BOLD,   pendingOrange);
            com.lowagie.text.Font tmsFont    = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD,   primaryBlue);

            // Brand header
            com.lowagie.text.Paragraph brand = new com.lowagie.text.Paragraph("TMS — Tuition Management System", tmsFont);
            brand.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            doc.add(brand);
            com.lowagie.text.pdf.draw.LineSeparator sep = new com.lowagie.text.pdf.draw.LineSeparator(1f, 100f, primaryBlue, com.lowagie.text.Element.ALIGN_CENTER, -4);
            doc.add(new com.lowagie.text.Chunk(sep));
            doc.add(com.lowagie.text.Chunk.NEWLINE);

            // Exam title & meta
            com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph(exam.getExamName() + " — Submissions Report", titleFont);
            title.setSpacingAfter(4);
            doc.add(title);
            doc.add(new com.lowagie.text.Paragraph("Subject: " + exam.getSubject() + "   |   Batch: " + exam.getBatchName(), subFont));
            doc.add(new com.lowagie.text.Paragraph("Generated: " + java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16), subFont));
            doc.add(com.lowagie.text.Chunk.NEWLINE);

            // Table
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 16, 22, 16, 8, 10, 10});
            table.setSpacingBefore(6);

            for (String h : new String[]{"#", "Student", "Email", "Submitted At", "Score", "Total", "Status"}) {
                com.lowagie.text.pdf.PdfPCell hCell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(h, colHeaderF));
                hCell.setBackgroundColor(headerBg);
                hCell.setPadding(7);
                hCell.setBorderColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(hCell);
            }

            int idx = 1;
            for (ExamSubmission sub : submissions) {
                java.awt.Color bg = (idx % 2 == 0) ? rowAlt : java.awt.Color.WHITE;
                addPdfCell(table, String.valueOf(idx++), cellNormal, bg);
                addPdfCell(table, sub.getStudent().getName(), cellBold, bg);
                addPdfCell(table, sub.getStudent().getEmail(), cellNormal, bg);
                String ts = sub.getSubmittedAt() != null ? sub.getSubmittedAt().toString().replace("T", " ").substring(0, 16) : "-";
                addPdfCell(table, ts, cellNormal, bg);
                addPdfCell(table, String.valueOf(sub.getScore()), cellBold, bg);
                addPdfCell(table, String.valueOf(sub.getTotalMarks()), cellNormal, bg);
                com.lowagie.text.Font sf = "PASS".equalsIgnoreCase(sub.getStatus()) ? passFont : "FAIL".equalsIgnoreCase(sub.getStatus()) ? failFont : pendFont;
                com.lowagie.text.pdf.PdfPCell sc = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(sub.getStatus(), sf));
                sc.setBackgroundColor(bg); sc.setPadding(7); sc.setBorderColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(sc);
            }

            if (submissions.isEmpty()) {
                com.lowagie.text.pdf.PdfPCell empty = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase("No submissions found.", subFont));
                empty.setColspan(7); empty.setPadding(12); empty.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                table.addCell(empty);
            }

            doc.add(table);
            doc.add(com.lowagie.text.Chunk.NEWLINE);
            com.lowagie.text.Paragraph footer = new com.lowagie.text.Paragraph("© " + java.time.Year.now() + " Tuition Management System. Confidential.", subFont);
            footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            doc.add(footer);
            doc.close();

            String filename = "submissions-" + exam.getExamName().replaceAll("\\s+", "-") + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void addPdfCell(com.lowagie.text.pdf.PdfPTable table, String text,
                             com.lowagie.text.Font font, java.awt.Color bg) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(7);
        cell.setBorderColor(java.awt.Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    @GetMapping("/mark/{submissionId}")
    public String markSubmission(@PathVariable("submissionId") Long submissionId, Model model, HttpSession session) {
        ExamSubmission submission = examSubmissionService.findById(submissionId);
        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submissionId);

        model.addAttribute("submission", submission);
        model.addAttribute("answers", answers);

        return "teacher-mark-submission";
    }

    @PostMapping("/mark/{submissionId}")
    public String saveMarks(@PathVariable("submissionId") Long submissionId,
                            @RequestParam("answerIds") List<Long> answerIds,
                            @RequestParam(value = "awardedMarks", required = false) List<Integer> awardedMarks,
                            @RequestParam(value = "teacherFeedback", required = false) List<String> teacherFeedback,
                            HttpSession session) {
        ExamSubmission submission = examSubmissionService.findById(submissionId);
        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submissionId);

        int totalScore = 0;
        int totalMarks = 0;
        boolean hasPendingEssay = false;

        for (SubmissionAnswer ans : answers) {
            totalMarks += ans.getQuestion().getMarks() != null ? ans.getQuestion().getMarks() : 0;
        }

        for (int i = 0; i < answerIds.size(); i++) {
            Long answerId = answerIds.get(i);

            SubmissionAnswer ans = answers.stream()
                    .filter(a -> a.getId().equals(answerId))
                    .findFirst()
                    .orElse(null);

            if (ans == null) {
                continue;
            }

            String type = ans.getQuestion().getType();

            if ("ESSAY".equalsIgnoreCase(type)) {
                Integer marks = (awardedMarks != null && awardedMarks.size() > i) ? awardedMarks.get(i) : null;
                String feedback = (teacherFeedback != null && teacherFeedback.size() > i) ? teacherFeedback.get(i) : null;

                if (marks != null) {
                    int maxMarks = ans.getQuestion().getMarks() != null ? ans.getQuestion().getMarks() : 0;
                    if (marks < 0) marks = 0;
                    if (marks > maxMarks) marks = maxMarks;

                    ans.setAwardedMarks(marks);
                } else {
                    ans.setAwardedMarks(null);
                    hasPendingEssay = true;
                }

                ans.setTeacherFeedback(feedback);
                submissionAnswerRepository.save(ans);
            }

            if (ans.getAwardedMarks() != null) {
                totalScore += ans.getAwardedMarks();
            } else if ("ESSAY".equalsIgnoreCase(type)) {
                hasPendingEssay = true;
            }
        }

        submission.setScore(totalScore);
        submission.setTotalMarks(totalMarks);

        if (hasPendingEssay) {
            submission.setStatus("PENDING");
        } else {
            int passMark = (int) Math.ceil(totalMarks * 0.5);
            submission.setStatus(totalScore >= passMark ? "PASS" : "FAIL");
        }

        examSubmissionService.save(submission);

        return "redirect:/teacher/submissions/exam/" + submission.getExam().getId();
    }
}
