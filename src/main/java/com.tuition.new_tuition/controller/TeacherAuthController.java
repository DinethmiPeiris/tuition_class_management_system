package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teacher")
public class TeacherAuthController {

    private final AuthService authService;

    public TeacherAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "teacher/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        return authService.loginTeacher(username, password)
                .map(t -> {
                    System.out.println("Teacher login SUCCESS: " + username + ", redirecting to /teacher/dashboard");
                    session.removeAttribute("studentUsername");
                    session.setAttribute("teacherId", t.getId());
                    return "redirect:/teacher/dashboard";
                })
                .orElseGet(() -> {
                    System.out.println("Teacher login FAILED: " + username);
                    model.addAttribute("error", "Invalid teacher username or password");
                    return "teacher/login";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("teacherId");
        session.removeAttribute("studentUsername");
        return "redirect:/teacher/login";
    }
}