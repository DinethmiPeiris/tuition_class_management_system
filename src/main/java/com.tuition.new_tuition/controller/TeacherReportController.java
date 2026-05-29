package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.ProgressReportDTO;
import com.tuition.new_tuition.service.ExamSubmissionService;
import com.tuition.new_tuition.service.TeacherProgressReportPdfService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/teacher/reports")
public class TeacherReportController {

    private final ExamSubmissionService examSubmissionService;
    private final TeacherProgressReportPdfService teacherProgressReportPdfService;

    public TeacherReportController(ExamSubmissionService examSubmissionService,
                                   TeacherProgressReportPdfService teacherProgressReportPdfService) {
        this.examSubmissionService = examSubmissionService;
        this.teacherProgressReportPdfService = teacherProgressReportPdfService;
    }


    @GetMapping
    public String viewTeacherReports(Model model, HttpSession session) {
        List<ProgressReportDTO> reports = examSubmissionService.getProgressReports();
        model.addAttribute("reports", reports);
        model.addAttribute("allResults", examSubmissionService.getAllSubmissionsReport());

        return "teacher-progress-report";
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadTeacherReport(HttpSession session) {
        List<ProgressReportDTO> reports = examSubmissionService.getProgressReports();
        byte[] pdfBytes = teacherProgressReportPdfService.generateProgressReportPdf(reports);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("teacher-progress-report.pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
