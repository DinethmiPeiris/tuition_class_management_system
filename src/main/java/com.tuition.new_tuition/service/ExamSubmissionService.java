package com.tuition.new_tuition.service;

import com.tuition.new_tuition.dto.AllStudentResultDTO;
import com.tuition.new_tuition.dto.ProgressReportDTO;
import com.tuition.new_tuition.dto.StudentProgressReportDTO;
import com.tuition.new_tuition.dto.StudentReportItemDTO;
import com.tuition.new_tuition.entity.AppUser;
import com.tuition.new_tuition.entity.ExamSubmission;
import com.tuition.new_tuition.entity.Role;
import com.tuition.new_tuition.repository.ExamSubmissionRepository;
import com.tuition.new_tuition.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExamSubmissionService {

    private final ExamSubmissionRepository examSubmissionRepository;
    private final UserRepository userRepository;

    public ExamSubmissionService(ExamSubmissionRepository examSubmissionRepository,
                                 UserRepository userRepository) {
        this.examSubmissionRepository = examSubmissionRepository;
        this.userRepository = userRepository;
    }

    public ExamSubmission save(ExamSubmission submission) {
        return examSubmissionRepository.save(submission);
    }

    public List<ExamSubmission> findByExamId(Long examId) {
        return examSubmissionRepository.findByExamId(examId);
    }

    public List<ExamSubmission> findByStudentId(Long studentId) {
        return examSubmissionRepository.findByStudentId(studentId);
    }

    public Optional<ExamSubmission> findByExamIdAndStudentId(Long examId, Long studentId) {
        return examSubmissionRepository.findByExamIdAndStudentId(examId, studentId);
    }

    public ExamSubmission findById(Long id) {
        return examSubmissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));
    }

    public List<ProgressReportDTO> getProgressReports() {
        List<ExamSubmission> all = examSubmissionRepository.findAll();
        List<ProgressReportDTO> reports = new ArrayList<>();

        // get distinct subject+batch combinations
        List<String> keys = all.stream()
                .map(s -> s.getExam().getSubject() + "|" + s.getExam().getBatchName())
                .distinct()
                .toList();

        for (String key : keys) {
            String[] parts = key.split("\\|", 2);
            String subject   = parts[0];
            String batchName = parts.length > 1 ? parts[1] : "";

            List<ExamSubmission> group = all.stream()
                    .filter(s -> subject.equals(s.getExam().getSubject())
                              && batchName.equals(s.getExam().getBatchName()))
                    .toList();

            long studentCount = group.stream()
                    .map(s -> s.getStudent().getId())
                    .distinct().count();

            double avgPct = group.stream()
                    .filter(s -> s.getTotalMarks() != null && s.getTotalMarks() > 0)
                    .mapToDouble(s -> ((double) s.getScore() / s.getTotalMarks()) * 100.0)
                    .average().orElse(0.0);

            long passCount    = group.stream().filter(s -> "PASS".equalsIgnoreCase(s.getStatus())).count();
            long failCount    = group.stream().filter(s -> "FAIL".equalsIgnoreCase(s.getStatus())).count();
            long pendingCount = group.stream().filter(s -> "PENDING".equalsIgnoreCase(s.getStatus())).count();

            reports.add(new ProgressReportDTO(
                    subject, batchName, (int) studentCount, group.size(),
                    Math.round(avgPct * 100.0) / 100.0,
                    passCount, failCount, pendingCount
            ));
        }
        return reports;
    }

    /** Returns every individual exam result for every student as a flat list. */
    public List<AllStudentResultDTO> getAllSubmissionsReport() {
        List<ExamSubmission> all = examSubmissionRepository.findAll();
        List<AllStudentResultDTO> results = new ArrayList<>();
        for (ExamSubmission s : all) {
            int score = s.getScore() != null ? s.getScore() : 0;
            int total = s.getTotalMarks() != null ? s.getTotalMarks() : 0;
            double pct = total > 0 ? Math.round(((double) score / total) * 10000.0) / 100.0 : 0.0;
            results.add(new AllStudentResultDTO(
                    s.getStudent().getName(),
                    s.getStudent().getEmail(),
                    s.getExam().getExamName(),
                    s.getExam().getSubject(),
                    s.getExam().getBatchName(),
                    s.getExam().getExamDate() != null ? s.getExam().getExamDate().toString() : "-",
                    score, total, pct, s.getStatus()
            ));
        }
        return results;
    }


    public StudentProgressReportDTO getStudentProgressReport(Long studentId) {
        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        List<ExamSubmission> submissions = examSubmissionRepository.findByStudentId(studentId);

        int totalExams = submissions.size();

        double averagePercentage = submissions.stream()
                .filter(s -> s.getTotalMarks() != null && s.getTotalMarks() > 0)
                .mapToDouble(s -> ((double) s.getScore() / s.getTotalMarks()) * 100.0)
                .average()
                .orElse(0.0);

        long passCount = submissions.stream()
                .filter(s -> "PASS".equalsIgnoreCase(s.getStatus()))
                .count();

        long failCount = submissions.stream()
                .filter(s -> "FAIL".equalsIgnoreCase(s.getStatus()))
                .count();

        long pendingCount = submissions.stream()
                .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus()))
                .count();

        List<StudentReportItemDTO> examItems = submissions.stream()
                .map(s -> {
                    int score = s.getScore() != null ? s.getScore() : 0;
                    int totalMarks = s.getTotalMarks() != null ? s.getTotalMarks() : 0;
                    double percentage = totalMarks > 0 ? ((double) score / totalMarks) * 100.0 : 0.0;

                    return new StudentReportItemDTO(
                            s.getExam().getExamName(),
                            s.getExam().getSubject(),
                            s.getExam().getExamDate() != null ? s.getExam().getExamDate().toString() : "-",
                            score,
                            totalMarks,
                            Math.round(percentage * 100.0) / 100.0,
                            s.getStatus()
                    );
                })
                .toList();

        return new StudentProgressReportDTO(
                student.getName(),
                student.getEmail(),
                totalExams,
                Math.round(averagePercentage * 100.0) / 100.0,
                passCount,
                failCount,
                pendingCount,
                examItems
        );
    }
}
