package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.ProgressReportDTO;
import com.tuition.new_tuition.service.ExamSubmissionService;
import com.tuition.new_tuition.service.TeacherProgressReportPdfService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class TeacherReportController {

    private final ExamSubmissionService examSubmissionService;
    private final TeacherProgressReportPdfService teacherProgressReportPdfService;

    public TeacherReportController(ExamSubmissionService examSubmissionService,
                                   TeacherProgressReportPdfService teacherProgressReportPdfService) {
        this.examSubmissionService = examSubmissionService;
        this.teacherProgressReportPdfService = teacherProgressReportPdfService;
    }

    @GetMapping("/teacher/reports")
    public String showProgressReport(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");

        if (email == null || role == null || !role.equalsIgnoreCase("TEACHER")) {
            return "redirect:/login?expired";
        }

        List<ProgressReportDTO> reports = examSubmissionService.getProgressReports();
        model.addAttribute("reports", reports);

        return "teacher-progress-report";
    }

    @GetMapping("/teacher/reports/download")
    public ResponseEntity<ByteArrayResource> downloadTeacherProgressReport(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");

        if (email == null || role == null || !role.equalsIgnoreCase("TEACHER")) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/login?expired")
                    .build();
        }

        List<ProgressReportDTO> reports = examSubmissionService.getProgressReports();
        byte[] pdfData = teacherProgressReportPdfService.generateTeacherProgressReportPdf(reports);

        ByteArrayResource resource = new ByteArrayResource(pdfData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teacher-progress-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfData.length)
                .body(resource);
    }
}
