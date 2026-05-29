package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.StudentStatus;
import com.tuition.new_tuition.service.StudentStatusService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teacher/students")
public class TeacherStudentController {

    private final StudentStatusService statusService;

    public TeacherStudentController(StudentStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping
    public String listStudents(@RequestParam(required = false) StudentStatus status,
                               HttpSession session,
                               Model model) {

        if (status != null) {
            model.addAttribute("students", statusService.getStudentsByStatus(status));
        } else {
            model.addAttribute("students", statusService.getAllStudents());
        }

        return "teacher/students";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam StudentStatus newStatus,
                               @RequestParam(required = false) String note,
                               HttpSession session) {

        if (session.getAttribute("teacherId") == null) return "redirect:/teacher/login";

        statusService.changeStatus(id, newStatus, note);
        return "redirect:/teacher/students";
    }

    @GetMapping("/{id}/history")
    public String viewHistory(@PathVariable Long id,
                              HttpSession session,
                              Model model) {

        model.addAttribute("history", statusService.getHistory(id));
        return "teacher/history";
    }
}