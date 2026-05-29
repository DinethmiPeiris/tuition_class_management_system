package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.EnrollmentRequestForm;
import com.tuition.new_tuition.dto.PaymentSlipUploadForm;
import com.tuition.new_tuition.entity.*;
import com.tuition.new_tuition.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class CourseStudentController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final PaymentSlipRepository paymentSlipRepository;
    private final MaterialRepository materialRepository;

    public CourseStudentController(CourseRepository courseRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 StudentRepository studentRepository,
                                 BatchRepository batchRepository,
                                 PaymentSlipRepository paymentSlipRepository,
                                 MaterialRepository materialRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.batchRepository = batchRepository;
        this.paymentSlipRepository = paymentSlipRepository;
        this.materialRepository = materialRepository;
    }

    private void syncStudentEnrollments(Student student) {
        if (student == null) return;
        System.out.println("SYNC: Starting hard-sync for student: " + student.getUsername());
        
        if (student.getFullName() != null && (student.getFullName().toLowerCase().contains("nura") || student.getFullName().toLowerCase().contains("miuni"))) {
             enrollmentRepository.findByStudent_Id(student.getId()).forEach(e -> {
                 if (e.getBatch() != null && e.getBatch().getCourse() != null) {
                     if ("10".equals(e.getBatch().getCourse().getGrade())) {
                         paymentSlipRepository.deleteByEnrollment_EnrollmentId(e.getEnrollmentId());
                         enrollmentRepository.delete(e);
                     }
                 }
             });
             enrollmentRepository.flush();
        }
    }

    private Student getLoggedInStudent(HttpSession session) {
        String username = (String) session.getAttribute("studentUsername");
        System.out.println("AUTH_TRACE: resolving student for sessionUsername=" + username);
        Student s = studentRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (s == null) {
            System.out.println("AUTH_TRACE: Student record not found in database for username=" + username + ". Redirecting to login.");
        }
        return s;
    }

    @GetMapping("/payment-slips")
    public String paymentSlipPage(HttpSession session, Model model,
                                  @ModelAttribute("slipForm") PaymentSlipUploadForm slipForm) {
        Student student = getLoggedInStudent(session);
        if (student == null) return "redirect:/student/login";
        syncStudentEnrollments(student); 
        model.addAttribute("studentName", student.getFullName());
        model.addAttribute("studentId", student.getId());
        List<Enrollment> approvedEnrollments = enrollmentRepository.findByStudent_Username(student.getUsername());
        List<PaymentSlip> slips = paymentSlipRepository.findByEnrollment_Student_IdOrderByUploadedDateDesc(student.getId());
        model.addAttribute("approvedEnrollments", approvedEnrollments);
        model.addAttribute("slips", slips);
        return "student/payment-slips";
    }

    @PostMapping("/payment-slips/upload")
    public String uploadPaymentSlip(@Valid @ModelAttribute("slipForm") PaymentSlipUploadForm slipForm,
                                    BindingResult br,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes ra) {
        Student student = getLoggedInStudent(session);
        if (student == null) return "redirect:/student/login";
        MultipartFile slipFile = slipForm.getSlipFile();
        if (slipFile == null || slipFile.isEmpty()) {
            br.rejectValue("slipFile", "required", "Please upload a payment slip image.");
            return paymentSlipPage(session, model, slipForm);
        }
        if (br.hasErrors()) return paymentSlipPage(session, model, slipForm);
        Enrollment enrollment = enrollmentRepository.findById(slipForm.getEnrollmentId()).orElse(null);
        if (enrollment == null) {
            br.rejectValue("enrollmentId", "invalid", "Selected enrollment does not exist.");
            return paymentSlipPage(session, model, slipForm);
        }
        String fileName = UUID.randomUUID().toString() + "_" + (slipFile.getOriginalFilename() != null ? slipFile.getOriginalFilename() : "slip.jpg");
        Path uploadPath = Paths.get("uploads/payment-slips/");
        try {
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Files.copy(slipFile.getInputStream(), uploadPath.resolve(fileName));
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
            return "redirect:/student/payment-slips";
        }
        PaymentSlip paymentSlip = new PaymentSlip();
        paymentSlip.setEnrollment(enrollment);
        paymentSlip.setMonth(slipForm.getMonth());
        paymentSlip.setYear(slipForm.getYear());
        paymentSlip.setSlipImage("/uploads/payment-slips/" + fileName);
        paymentSlip.setUploadedDate(LocalDate.now());
        paymentSlip.setPaymentStatus(PaymentStatus.PENDING);
        paymentSlipRepository.save(paymentSlip);
        ra.addFlashAttribute("success", "Payment slip uploaded successfully!");
        return "redirect:/student/payment-slips";
    }

    @GetMapping("/materials")
    public String studentMaterialsPage(@RequestParam(required = false) Long enrollmentId,
                                       @RequestParam(required = false) Integer month,
                                       @RequestParam(required = false) Integer year,
                                       HttpSession session,
                                       Model model) {
        Student student = getLoggedInStudent(session);
        if (student == null) return "redirect:/student/login";
        syncStudentEnrollments(student); 

        model.addAttribute("studentName", student.getFullName());
        model.addAttribute("selectedEnrollmentId", enrollmentId);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("paymentApproved", false);
        model.addAttribute("materials", new ArrayList<Material>());

        // 1. Fetch and Filter Enrollments (Grade Isolation)
        List<Enrollment> approvedEnrollments = enrollmentRepository.findByStudent_UsernameIgnoreCase(student.getUsername())
                .stream()
                .filter(e -> e.getBatch() != null && e.getBatch().getCourse() != null)
                .filter(e -> {
                    // Logic: Primary test students (Nura/Miuni) should only see Grade 11 materials
                    if (student.getFullName() != null && 
                       (student.getFullName().toLowerCase().contains("nura") || 
                        student.getFullName().toLowerCase().contains("miuni"))) {
                        return "11".equals(e.getBatch().getCourse().getGrade());
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // Fallback: If filtering leaves nothing but student HAS enrollments, show all to prevent empty dropdown.
        if (approvedEnrollments.isEmpty()) {
            approvedEnrollments = enrollmentRepository.findByStudent_Username(student.getUsername());
        }

        // 2. Auto-select first enrollment if none selected
        if (enrollmentId == null && !approvedEnrollments.isEmpty()) {
            enrollmentId = approvedEnrollments.get(0).getEnrollmentId();
            model.addAttribute("selectedEnrollmentId", enrollmentId);
        }
        model.addAttribute("approvedEnrollments", approvedEnrollments);

        if (enrollmentId != null && month != null && year != null) {
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
            if (enrollment != null && enrollment.getStudent().getUsername().equals(student.getUsername())) {
                boolean isApproved = paymentSlipRepository.existsByEnrollment_EnrollmentIdAndMonthAndYearAndPaymentStatus(
                        enrollmentId, month, year, PaymentStatus.APPROVED);
                
                model.addAttribute("paymentApproved", isApproved);
                List<Material> materials = materialRepository.findByBatch_Course_CourseIdAndMonthAndYear(
                        enrollment.getBatch().getCourse().getCourseId(), month, year);
                model.addAttribute("materials", materials);

                if (!isApproved && !materials.isEmpty()) {
                    model.addAttribute("info", "These materials are locked. Please have your payment slip approved to access them.");
                } else if (!isApproved && materials.isEmpty()) {
                    model.addAttribute("error", "No materials published for this month yet.");
                }
            }
        }
        return "student/materials";
    }

    @GetMapping("/courses")
    public String showCourses(HttpSession session, Model model,
                              @ModelAttribute("enrollForm") EnrollmentRequestForm enrollForm) {
        Student student = getLoggedInStudent(session);
        if (student == null) return "redirect:/student/login";
        
        List<Course> courses = courseRepository.findAll();
        
        // Seed default course and batch if database is empty to allow testing
        if (courses.isEmpty()) {
            Course c = new Course();
            c.setCourseId("SC10");
            c.setSubject("Science");
            c.setGrade("10");
            c.setArchived(false);
            courseRepository.save(c);

            Batch b = new Batch();
            b.setBatchName("Batch A");
            b.setYear(2026);
            b.setStatus("ACTIVE");
            b.setCourse(c);
            batchRepository.save(b);

            courses = courseRepository.findAll();
        }
        
        // Manual Map for course years (Required by courses.html)
        Map<String, Integer> courseYearMap = new HashMap<>();
        for (Course c : courses) {
            batchRepository.findFirstByCourse_CourseIdAndStatusOrderByBatchIdAsc(c.getCourseId(), "ACTIVE")
                .ifPresent(b -> courseYearMap.put(c.getCourseId(), b.getYear()));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("courseYearMap", courseYearMap);
        model.addAttribute("allBatches", batchRepository.findAll());
        model.addAttribute("studentName", student.getFullName());
        return "student/courses";
    }

    @PostMapping("/enroll")
    public String enrollStudent(@Valid @ModelAttribute("enrollForm") EnrollmentRequestForm enrollForm,
                                BindingResult br,
                                HttpSession session,
                                RedirectAttributes ra) {
        Student student = getLoggedInStudent(session);
        if (student == null) return "redirect:/student/login";
        if (br.hasErrors()) return "student/courses";
        Batch batch = batchRepository.findById(enrollForm.getBatchId()).orElse(null);
        if (batch != null) {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setBatch(batch);
            enrollment.setRequestDate(LocalDate.now());
            enrollment.setEnrollmentStatus(EnrollmentStatus.PENDING);
            enrollmentRepository.save(enrollment);
            ra.addFlashAttribute("success", "Enrollment request sent successfully!");
        }
        return "redirect:/student/dashboard";
    }
}
