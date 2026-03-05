package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Exam;
import com.tuition.new_tuition.entity.Question;
import com.tuition.new_tuition.service.ExamService;
import com.tuition.new_tuition.service.QuestionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentExamController {

    private final ExamService examService;
    private final QuestionService questionService;

    public StudentExamController(ExamService examService, QuestionService questionService) {
        this.examService = examService;
        this.questionService = questionService;
    }

    // STUDENT EXAM LIST
    @GetMapping("/exams")
    public String studentExamList(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login?expired";

        model.addAttribute("exams", examService.findAll());
        return "student-exam-list";
    }

    // START EXAM
    @GetMapping("/exams/{examId}/start")
    public String startExam(@PathVariable Long examId, HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login?expired";

        Exam exam = examService.findById(examId);
        List<Question> questions = questionService.findByExamId(examId);

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        return "student-exam-start";
    }

    //  SUBMIT EXAM
    @PostMapping("/exams/{examId}/submit")
    public String submitExam(@PathVariable Long examId,
                             HttpSession session,
                             @RequestParam Map<String, String> params,
                             Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login?expired";

        Exam exam = examService.findById(examId);
        List<Question> questions = questionService.findByExamId(examId);

        // Collect answers (IMPORTANT: your HTML uses name="q_<id>")
        Map<Long, String> answers = new LinkedHashMap<>();
        int totalMarks = 0;
        int score = 0;

        boolean hasEssay = false;

        for (Question q : questions) {
            totalMarks += (q.getMarks() != null ? q.getMarks() : 0);

            String key = "q_" + q.getId();          // matches student-exam-start.html
            String studentAns = params.getOrDefault(key, "").trim();
            answers.put(q.getId(), studentAns.isEmpty() ? "-" : studentAns);

            // Marking rules
            if ("ESSAY".equalsIgnoreCase(q.getType())) {
                hasEssay = true; // teacher will mark later
                continue;
            }

            // MCQ auto marking
            if ("MCQ".equalsIgnoreCase(q.getType())) {
                String correct = (q.getCorrectOption() == null) ? "" : q.getCorrectOption().trim();
                if (!studentAns.isEmpty() && studentAns.equalsIgnoreCase(correct)) {
                    score += (q.getMarks() != null ? q.getMarks() : 0);
                }
            }
        }

        // 75% pass mark
        int passMark = (int) Math.ceil(totalMarks * 0.75);

        String status;
        if (hasEssay) {
            status = "PENDING"; // teacher marks essay later
        } else {
            status = (score >= passMark) ? "PASS" : "FAIL";
        }

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);

        model.addAttribute("score", score);
        model.addAttribute("totalMarks", totalMarks);
        model.addAttribute("passMark", passMark);
        model.addAttribute("status", status);

        return "student-exam-submitted";
    }
}
