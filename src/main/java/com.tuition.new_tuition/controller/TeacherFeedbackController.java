package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teacher")
public class TeacherFeedbackController {

    private final FeedbackService feedbackService;

    public TeacherFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/feedback")
    public String viewFeedback(HttpSession session, Model model) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        
        // Fallback session identifiers for the common navigation fragment
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("feedbacks", feedbackService.getFeedbackForTeacher(teacherId));

        model.addAttribute("feedbacks", feedbackService.getFeedbackForTeacher(teacherId));
        return "teacher/feedback";
    }
}