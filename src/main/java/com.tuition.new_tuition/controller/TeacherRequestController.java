package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.service.TeacherApprovalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teacher")
public class TeacherRequestController {

    private final TeacherApprovalService approvalService;

    public TeacherRequestController(TeacherApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/requests")
    public String viewRequests(HttpSession session, Model model) {
        // Fallback session identifiers for the common navigation fragment
        model.addAttribute("teacherId", session.getAttribute("teacherId"));

        model.addAttribute("requests", approvalService.getPendingRequests());
        return "teacher/requests";
    }

    @PostMapping("/requests/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session) {
        approvalService.approveRequest(id);
        return "redirect:/teacher/requests";
    }

    @PostMapping("/requests/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String reason,
                         HttpSession session) {
        approvalService.rejectRequest(id, reason);
        return "redirect:/teacher/requests";
    }
}