package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Exam;
import com.tuition.new_tuition.entity.Question;
import com.tuition.new_tuition.repository.ExamRepository;
import com.tuition.new_tuition.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ExamRepository examRepository;

    public QuestionController(QuestionService questionService, ExamRepository examRepository) {
        this.questionService = questionService;
        this.examRepository = examRepository;
    }

    //  LIST (supports /questions/list/{examId} and /questions/{examId})
    @GetMapping({"/list/{examId}", "/{examId}"})
    public String list(@PathVariable Long examId, Model model) {
        Exam exam = examRepository.findById(examId).orElse(null);
        model.addAttribute("exam", exam);
        model.addAttribute("questions", questionService.findByExamId(examId));
        return "question-list";
    }

    //  SHOW ADD FORM
    @GetMapping("/add/{examId}")
    public String showAdd(@PathVariable Long examId, Model model) {
        Exam exam = examRepository.findById(examId).orElse(null);

        Question question = new Question();
        question.setType("MCQ"); // default
        question.setMarks(1);

        model.addAttribute("exam", exam);
        model.addAttribute("question", question);
        return "question-add";
    }

    //  SAVE (fixes /questions/save/9)
    @PostMapping("/save/{examId}")
    public String save(@PathVariable Long examId,
                       @ModelAttribute("question") Question question) {

        Exam exam = examRepository.findById(examId).orElseThrow();
        questionService.saveForExam(exam, question);

        return "redirect:/questions/list/" + examId;
    }

    //  SHOW EDIT FORM
    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable Long id, Model model) {
        Question q = questionService.findById(id);
        Exam exam = q.getExam();

        model.addAttribute("exam", exam);
        model.addAttribute("question", q);
        return "question-edit";
    }

    //  UPDATE (fixes /questions/update 404)
    @PostMapping("/update")
    public String update(@ModelAttribute("question") Question question,
                         @RequestParam("examId") Long examId) {

        Exam exam = examRepository.findById(examId).orElseThrow();
        questionService.updateForExam(exam, question);

        return "redirect:/questions/list/" + examId;
    }

    //  DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        Long examId = questionService.deleteAndReturnExamId(id);
        return "redirect:/questions/list/" + examId;
    }
}
