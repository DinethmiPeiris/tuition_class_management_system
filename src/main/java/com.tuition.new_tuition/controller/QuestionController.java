package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Exam;
import com.tuition.new_tuition.entity.Question;
import com.tuition.new_tuition.repository.ExamRepository;
import com.tuition.new_tuition.service.QuestionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ExamRepository examRepository;

    public QuestionController(QuestionService questionService, ExamRepository examRepository) {
        this.questionService = questionService;
        this.examRepository = examRepository;
    }


    @GetMapping("/exam/{examId}")
    public String list(@PathVariable("examId") Long examId, Model model, HttpSession session) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));

        List<Question> questions = questionService.findByExamId(examId);

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);

        return "question-list";
    }

    @GetMapping("/add/{examId}")
    public String showAdd(@PathVariable("examId") Long examId, Model model, HttpSession session) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));

        Question question = new Question();
        question.setQuestionText("");
        question.setMarks(1);

        model.addAttribute("exam", exam);
        model.addAttribute("question", question);

        return "question-add";
    }

    @PostMapping("/save/{examId}")
    public String save(@PathVariable("examId") Long examId,
                       @ModelAttribute("question") Question question,
                       HttpSession session) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));

        questionService.saveForExam(exam, question);

        return "redirect:/questions/exam/" + examId;
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable("id") Long id, Model model, HttpSession session) {
        Question question = questionService.findById(id);

        model.addAttribute("question", question);
        model.addAttribute("exam", question.getExam());

        return "question-edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @ModelAttribute("question") Question question,
                         HttpSession session) {
        Question existing = questionService.findById(id);
        Long examId = existing.getExam().getId();

        questionService.updateForExam(existing.getExam(), question);

        return "redirect:/questions/exam/" + examId;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        Long examId = questionService.deleteAndReturnExamId(id);
        return "redirect:/questions/exam/" + examId;
    }
}
