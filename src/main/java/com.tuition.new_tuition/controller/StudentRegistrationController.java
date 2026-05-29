package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.*;
import com.tuition.new_tuition.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/student")
public class StudentRegistrationController {

    private final RegistrationService registrationService;
 
    public StudentRegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "student/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam(required = false) String phone,
                             @RequestParam(required = false) String address,
                             Model model) {

        return registrationService.registerStudent(fullName, email, username, password, phone, address)
                .map(errorMsg -> {
                    model.addAttribute("error", errorMsg);
                    return "student/register";
                })
                .orElse("redirect:/student/login?registered=true");
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String studentUsername = (String) session.getAttribute("studentUsername");
        System.out.println("AUTH_TRACE: Accessing /student/dashboard. studentUsername=" + studentUsername + ", teacherId=" + session.getAttribute("teacherId"));
        
        RegistrationRequest req = registrationService.getRequestByUsername(studentUsername);
        if (req != null) {
            model.addAttribute("req", req);
            if (req.getStatus().name().equals("APPROVED") && req.getCreatedStudent() != null) {
                model.addAttribute("studentStatus", req.getCreatedStudent().getStatus());
            }
        }

        model.addAttribute("isDashboard", true);
        return "student/dashboard";
    }
}