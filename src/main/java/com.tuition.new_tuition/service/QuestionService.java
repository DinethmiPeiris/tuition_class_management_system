package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.Exam;
import com.tuition.new_tuition.entity.Question;
import com.tuition.new_tuition.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
    public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> findByExamId(Long examId) {
        return questionRepository.findByExamId(examId);
    }

    public Question findById(Long id) {
        return questionRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Question not found with id: " + id));
    }

    public void saveForExam(Exam exam, Question question) {
        question.setExam(exam);

        // simple cleanup: if ESSAY, clear options/correct
        if ("ESSAY".equalsIgnoreCase(question.getType())) {
            question.setOptionA(null);
            question.setOptionB(null);
            question.setOptionC(null);
            question.setOptionD(null);
            question.setCorrectOption(null);
        }

        questionRepository.save(question);
    }

    public void updateForExam(Exam exam, Question question) {
        // keep same id + exam
        question.setExam(exam);

        if ("ESSAY".equalsIgnoreCase(question.getType())) {
            question.setOptionA(null);
            question.setOptionB(null);
            question.setOptionC(null);
            question.setOptionD(null);
            question.setCorrectOption(null);
        }

        questionRepository.save(question);
    }

    public Long deleteAndReturnExamId(Long questionId) {
        Question q = findById(questionId);
        Long examId = q.getExam().getId();
        questionRepository.deleteById(questionId);
        return examId;
    }
}
