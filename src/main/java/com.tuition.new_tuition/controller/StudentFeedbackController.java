package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.service.FeedbackService;
import com.tuition.new_tuition.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student")
public class StudentFeedbackController {

    private final FeedbackService feedbackService;
    private final StudentRepository studentRepository;

    public StudentFeedbackController(FeedbackService feedbackService,
                                     StudentRepository studentRepository) {
        this.feedbackService = feedbackService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/feedback")
    public String feedbackPage(HttpSession session, Model model) {

        String username = (String) session.getAttribute("studentUsername");
        
        // Use findByUsernameIgnoreCase to prevent intermittent redirection due to Case mismatches
        Student student = studentRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("Student record not found for username=" + username));

        model.addAttribute("feedbacks",
                feedbackService.getFeedbackByStudent(student.getId()));

        return "student/feedback";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@RequestParam int rating,
                                 @RequestParam(required = false) String comment,
                                 @RequestParam(required = false) boolean anonymous,
                                 HttpSession session) {

        String username = (String) session.getAttribute("studentUsername");
        
        // Use findByUsernameIgnoreCase for consistent auth
        feedbackService.submitFeedback(username, rating, comment, anonymous);
        return "redirect:/student/feedback";
    }
}