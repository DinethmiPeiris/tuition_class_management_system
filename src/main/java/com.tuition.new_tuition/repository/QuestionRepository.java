package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExamId(Long examId);

    @Transactional
    void deleteByExamId(Long examId);
}
