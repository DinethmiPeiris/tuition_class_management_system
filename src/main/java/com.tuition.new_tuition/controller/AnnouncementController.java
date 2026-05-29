package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Announcement;
import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.repository.StudentRepository;
import com.tuition.new_tuition.service.AnnouncementService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/e5/announcements")
public class AnnouncementController {

    private final AnnouncementService service;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AnnouncementController(AnnouncementService service,
                                  StudentRepository studentRepository,
                                  EnrollmentRepository enrollmentRepository) {
        this.service = service;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/teacher")
    public String teacherList(HttpSession session, Model model) {
        if (session.getAttribute("teacherId") == null) {
            return "redirect:/teacher/login";
        }
        model.addAttribute("announcements", service.listAll());
        return "e5/announcements/teacher_list";
    }

    @GetMapping("/student")
    public String studentList(HttpSession session, Model model) {
        String studentUsername = (String) session.getAttribute("studentUsername");
        if (studentUsername == null) {
            return "redirect:/student/login";
        }

        Student student = studentRepository.findByUsernameIgnoreCase(studentUsername)
                .orElseThrow(() -> new RuntimeException("Student not found for username: " + studentUsername));

        String studentGroup = resolveStudentGroup(student.getId());

        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("studentGroup", studentGroup);
        model.addAttribute("announcements", service.listForStudentType(studentGroup));
        return "e5/announcements/student_list";
    }

    private String resolveStudentGroup(Long studentId) {
        List<Enrollment> approvedEnrollments = enrollmentRepository
                .findByStudent_IdAndEnrollmentStatus(studentId, EnrollmentStatus.APPROVED);

        boolean hasMaths = approvedEnrollments.stream()
                .map(enrollment -> enrollment.getBatch().getCourse().getSubject())
                .filter(subject -> subject != null)
                .anyMatch(subject -> subject.equalsIgnoreCase("MATHS") || subject.equalsIgnoreCase("MATHEMATICS"));

        boolean hasScience = approvedEnrollments.stream()
                .map(enrollment -> enrollment.getBatch().getCourse().getSubject())
                .filter(subject -> subject != null)
                .anyMatch(subject -> subject.equalsIgnoreCase("SCIENCE"));

        if (hasMaths && hasScience) return "BOTH";
        if (hasMaths) return "MATHS";
        if (hasScience) return "SCIENCE";
        return "NONE";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        if (session.getAttribute("teacherId") == null) {
            return "redirect:/teacher/login";
        }
        model.addAttribute("announcement", new Announcement());
        return "e5/announcements/form";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("announcement") Announcement announcement,
                      BindingResult br, HttpSession session) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) {
            return "redirect:/teacher/login";
        }

        if (br.hasErrors()) return "e5/announcements/form";

        announcement.setTeacherId(teacherId);
        service.create(announcement);
        return "redirect:/e5/announcements/teacher";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("teacherId") == null) {
            return "redirect:/teacher/login";
        }
        model.addAttribute("announcement", service.getById(id));
        return "e5/announcements/form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("announcement") Announcement announcement,
                         BindingResult br, HttpSession session) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) {
            return "redirect:/teacher/login";
        }

        if (br.hasErrors()) return "e5/announcements/form";

        service.update(id, announcement, teacherId);
        return "redirect:/e5/announcements/teacher";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) {
            return "redirect:/teacher/login";
        }

        service.delete(id, teacherId);
        return "redirect:/e5/announcements/teacher";
    }
}
