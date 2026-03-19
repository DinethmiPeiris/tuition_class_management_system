package com.tuition.new_tuition.service;

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
        List<AppUser> students = userRepository.findByRole(Role.STUDENT);
        List<ProgressReportDTO> reports = new ArrayList<>();

        for (AppUser student : students) {
            List<ExamSubmission> allSubmissions = examSubmissionRepository.findByStudentId(student.getId());

            if (allSubmissions == null || allSubmissions.isEmpty()) {
                continue;
            }

            List<String> subjects = allSubmissions.stream()
                    .map(sub -> sub.getExam().getSubject())
                    .filter(subject -> subject != null && !subject.isBlank())
                    .distinct()
                    .toList();

            for (String subject : subjects) {
                List<ExamSubmission> subjectSubmissions = allSubmissions.stream()
                        .filter(sub -> subject.equalsIgnoreCase(sub.getExam().getSubject()))
                        .toList();

                if (subjectSubmissions.isEmpty()) {
                    continue;
                }

                int totalExams = subjectSubmissions.size();

                double averagePercentage = subjectSubmissions.stream()
                        .filter(s -> s.getTotalMarks() != null && s.getTotalMarks() > 0)
                        .mapToDouble(s -> ((double) s.getScore() / s.getTotalMarks()) * 100.0)
                        .average()
                        .orElse(0.0);

                long passCount = subjectSubmissions.stream()
                        .filter(s -> "PASS".equalsIgnoreCase(s.getStatus()))
                        .count();

                long failCount = subjectSubmissions.stream()
                        .filter(s -> "FAIL".equalsIgnoreCase(s.getStatus()))
                        .count();

                long pendingCount = subjectSubmissions.stream()
                        .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus()))
                        .count();

                reports.add(new ProgressReportDTO(
                        student.getName(),
                        student.getEmail(),
                        subject,
                        totalExams,
                        Math.round(averagePercentage * 100.0) / 100.0,
                        passCount,
                        failCount,
                        pendingCount
                ));
            }
        }

        return reports;
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
