package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.Exam;
import com.tuition.new_tuition.repository.ExamRepository;
import com.tuition.new_tuition.repository.ExamSubmissionRepository;
import com.tuition.new_tuition.repository.QuestionRepository;
import com.tuition.new_tuition.repository.SubmissionAnswerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;

    public ExamService(ExamRepository examRepository,
                       QuestionRepository questionRepository,
                       ExamSubmissionRepository examSubmissionRepository,
                       SubmissionAnswerRepository submissionAnswerRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examSubmissionRepository = examSubmissionRepository;
        this.submissionAnswerRepository = submissionAnswerRepository;
    }

    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    public Exam save(Exam exam) {
        return examRepository.save(exam);
    }

    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
    }

    public Exam update(Long id, Exam updatedExam) {
        Exam existingExam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));

        existingExam.setExamName(updatedExam.getExamName());
        existingExam.setSubject(updatedExam.getSubject());
        existingExam.setBatchName(updatedExam.getBatchName());
        existingExam.setExamDate(updatedExam.getExamDate());

        return examRepository.save(existingExam);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!examRepository.existsById(id)) {
            throw new RuntimeException("Exam not found with id: " + id);
        }

        submissionAnswerRepository.deleteBySubmissionExamId(id);
        examSubmissionRepository.deleteByExamId(id);
        questionRepository.deleteByExamId(id);
        examRepository.deleteById(id);
    }

    public List<Exam> filterExams(String subject, String batchName, String examName, String examDate) {
        return examRepository.findAll().stream()
                .filter(exam -> isBlank(subject) || equalsIgnoreCase(exam.getSubject(), subject))
                .filter(exam -> isBlank(batchName) || equalsIgnoreCase(exam.getBatchName(), batchName))
                .filter(exam -> isBlank(examName) || equalsIgnoreCase(exam.getExamName(), examName))
                .filter(exam -> {
                    if (isBlank(examDate)) {
                        return true;
                    }

                    if (exam.getExamDate() == null) {
                        return false;
                    }

                    try {
                        LocalDate selectedDate = LocalDate.parse(examDate);
                        return exam.getExamDate().isEqual(selectedDate);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public List<String> getDistinctSubjects(List<Exam> exams) {
        return exams.stream()
                .map(Exam::getSubject)
                .filter(this::isNotBlank)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public List<String> getDistinctBatches(List<Exam> exams) {
        return exams.stream()
                .map(Exam::getBatchName)
                .filter(this::isNotBlank)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public List<String> getDistinctExamNames(List<Exam> exams) {
        return exams.stream()
                .map(Exam::getExamName)
                .filter(this::isNotBlank)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
