package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.RegistrationRequest;
import com.tuition.new_tuition.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student")
public class StudentAuthController {

    private final AuthService authService;

    public StudentAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "student/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        return authService.loginStudentRequest(username, password)
                .map(req -> {
                    session.removeAttribute("teacherId");
                    session.setAttribute("studentUsername", req.getUsername());
                    return "redirect:/student/dashboard";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid username or password");
                    return "student/login";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("studentUsername");
        session.removeAttribute("teacherId");
        return "redirect:/student/login";
    }
}