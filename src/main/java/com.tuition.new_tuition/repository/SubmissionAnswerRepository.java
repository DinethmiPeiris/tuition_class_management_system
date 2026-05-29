package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.SubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionAnswerRepository extends JpaRepository<SubmissionAnswer, Long> {
    List<SubmissionAnswer> findBySubmissionId(Long submissionId);
    void deleteBySubmissionExamId(Long examId);
    void deleteByQuestionId(Long questionId);
}
