package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {

    List<ExamSubmission> findByExamId(Long examId);

    List<ExamSubmission> findByStudentId(Long studentId);

    Optional<ExamSubmission> findByExamIdAndStudentId(Long examId, Long studentId);

    @Transactional
    void deleteByExamId(Long examId);

    @Transactional
    void deleteByStudentId(Long studentId);
}
