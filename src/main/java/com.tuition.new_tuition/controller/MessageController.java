package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.entity.Message;
import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.entity.Teacher;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.repository.StudentRepository;
import com.tuition.new_tuition.repository.TeacherRepository;
import com.tuition.new_tuition.service.MessageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/e5/messages")
public class MessageController {

    private final MessageService service;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;

    public MessageController(MessageService service,
                             StudentRepository studentRepository,
                             TeacherRepository teacherRepository,
                             EnrollmentRepository enrollmentRepository) {
        this.service = service;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/inbox")
    public String inbox(HttpSession session, Model model) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) {
            return "redirect:/teacher/login";
        }

        model.addAttribute("messages", service.listForUser(teacherId, "TEACHER"));
        return "e5/messages/inbox";
    }

    @GetMapping("/send")
    public String sendForm(HttpSession session, Model model) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) {
            return "redirect:/teacher/login";
        }

        model.addAttribute("message", new Message());
        model.addAttribute("students", studentRepository.findByTeacher_Id(teacherId)
                .stream()
                .sorted(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER))
                .toList());
        return "e5/messages/send";
    }

    @PostMapping("/send")
    public String send(@Valid @ModelAttribute("message") Message message,
                       BindingResult br,
                       HttpSession session,
                       Model model) {
        Long teacherId = (Long) session.getAttribute("teacherId");
        if (teacherId == null) {
            return "redirect:/teacher/login";
        }

        if (message.getReceiverId() == null) {
            br.rejectValue("receiverId", "required", "Please select a student.");
        }

        Optional<Student> selectedStudent = message.getReceiverId() == null
                ? Optional.empty()
                : studentRepository.findById(message.getReceiverId());

        if (selectedStudent.isEmpty() || selectedStudent.get().getTeacher() == null
                || !teacherId.equals(selectedStudent.get().getTeacher().getId())) {
            br.rejectValue("receiverId", "invalid", "Please select a valid registered student.");
        }

        if (br.hasErrors()) {
            model.addAttribute("students", studentRepository.findByTeacher_Id(teacherId)
                    .stream()
                    .sorted(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER))
                    .toList());
            return "e5/messages/send";
        }

        message.setSenderId(teacherId);
        message.setSenderRole("TEACHER");
        message.setReceiverRole("STUDENT");

        service.send(message);
        return "redirect:/e5/messages/inbox";
    }

    @GetMapping("/student/inbox")
    public String studentInbox(HttpSession session, Model model) {
        String studentUsername = (String) session.getAttribute("studentUsername");
        if (studentUsername == null) {
            return "redirect:/student/login";
        }

        Student student = studentRepository.findByUsernameIgnoreCase(studentUsername)
                .orElseThrow(() -> new RuntimeException("Student not found for username: " + studentUsername));

        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("messages", service.listForUser(student.getId(), "STUDENT"));
        return "e5/messages/student_inbox";
    }

    @GetMapping("/student/send")
    public String studentSendForm(HttpSession session, Model model) {
        String studentUsername = (String) session.getAttribute("studentUsername");
        if (studentUsername == null) {
            return "redirect:/student/login";
        }

        Student student = studentRepository.findByUsernameIgnoreCase(studentUsername)
                .orElseThrow(() -> new RuntimeException("Student not found for username: " + studentUsername));

        Teacher teacher = student.getTeacher();
        if (teacher == null) {
            throw new RuntimeException("No teacher assigned for student: " + studentUsername);
        }

        Message message = new Message();
        message.setSenderRole("STUDENT");
        message.setSenderId(student.getId());
        message.setReceiverRole("TEACHER");
        message.setReceiverId(teacher.getId());

        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("teacher", teacher);
        model.addAttribute("message", message);
        return "e5/messages/student_send";
    }

    @PostMapping("/student/send")
    public String studentSend(@Valid @ModelAttribute("message") Message message,
                              BindingResult br,
                              HttpSession session,
                              Model model) {
        String studentUsername = (String) session.getAttribute("studentUsername");
        if (studentUsername == null) {
            return "redirect:/student/login";
        }

        Student student = studentRepository.findByUsernameIgnoreCase(studentUsername)
                .orElseThrow(() -> new RuntimeException("Student not found for username: " + studentUsername));

        Teacher teacher = student.getTeacher();
        if (teacher == null) {
            throw new RuntimeException("No teacher assigned for student: " + studentUsername);
        }

        if (br.hasErrors()) {
            model.addAttribute("studentUsername", studentUsername);
            model.addAttribute("teacher", teacher);
            return "e5/messages/student_send";
        }

        message.setSenderRole("STUDENT");
        message.setSenderId(student.getId());
        message.setReceiverRole("TEACHER");
        message.setReceiverId(teacher.getId());

        service.send(message);
        return "redirect:/e5/messages/student/inbox";
    }

    @GetMapping("/debug/student-subject")
    public String debugStudentSubject(HttpSession session, Model model) {
        String studentUsername = (String) session.getAttribute("studentUsername");
        if (studentUsername == null) {
            return "redirect:/student/login";
        }

        Student student = studentRepository.findByUsernameIgnoreCase(studentUsername)
                .orElseThrow(() -> new RuntimeException("Student not found for username: " + studentUsername));

        List<Enrollment> approvedEnrollments = enrollmentRepository
                .findByStudent_IdAndEnrollmentStatus(student.getId(), EnrollmentStatus.APPROVED);

        model.addAttribute("messages", service.listForUser(student.getId(), "STUDENT"));
        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("approvedEnrollments", approvedEnrollments);
        return "e5/messages/student_inbox";
    }
}
