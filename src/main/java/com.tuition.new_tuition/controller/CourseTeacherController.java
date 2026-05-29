package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.CourseCreateForm;
import com.tuition.new_tuition.dto.CourseUpdateForm;
import com.tuition.new_tuition.dto.MaterialUploadForm;
import com.tuition.new_tuition.dto.PaymentTrackingFilterForm;
import com.tuition.new_tuition.entity.Batch;
import com.tuition.new_tuition.entity.Course;
import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.entity.Material;
import com.tuition.new_tuition.entity.PaymentSlip;
import com.tuition.new_tuition.entity.PaymentStatus;
import com.tuition.new_tuition.repository.BatchRepository;
import com.tuition.new_tuition.repository.CourseRepository;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.repository.MaterialRepository;
import com.tuition.new_tuition.repository.PaymentSlipRepository;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/teacher")
public class CourseTeacherController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BatchRepository batchRepository;
    private final PaymentSlipRepository paymentSlipRepository;
    private final MaterialRepository materialRepository;

    public CourseTeacherController(CourseRepository courseRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 BatchRepository batchRepository,
                                 PaymentSlipRepository paymentSlipRepository,
                                 MaterialRepository materialRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.batchRepository = batchRepository;
        this.paymentSlipRepository = paymentSlipRepository;
        this.materialRepository = materialRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(jakarta.servlet.http.HttpSession session, Model model) {
        model.addAttribute("teacherId", session.getAttribute("teacherId"));
        model.addAttribute("totalCourses", courseRepository.count());
        model.addAttribute("totalBatches", batchRepository.count());

        long pendingEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getEnrollmentStatus() == EnrollmentStatus.PENDING)
                .count();
        model.addAttribute("pendingEnrollments", pendingEnrollments);

        long pendingPaymentSlips = paymentSlipRepository.findAll().stream()
                .filter(s -> s.getPaymentStatus() == PaymentStatus.PENDING)
                .count();
        model.addAttribute("pendingPaymentSlips", pendingPaymentSlips);
        model.addAttribute("isDashboard", true);

        return "teacher/dashboard";
    }

    @GetMapping("/courses")
    public String coursesPage(jakarta.servlet.http.HttpSession session, Model model,
                              @ModelAttribute("createForm") CourseCreateForm createForm) {
        model.addAttribute("teacherId", session.getAttribute("teacherId"));

        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);

        Map<String, Integer> courseYearMap = new HashMap<>();
        List<Batch> batches = batchRepository.findAll();

        for (Course c : courses) {
            Integer year = null;
            for (Batch b : batches) {
                if (b.getCourse() != null
                        && b.getCourse().getCourseId() != null
                        && b.getCourse().getCourseId().equalsIgnoreCase(c.getCourseId())) {
                    year = b.getYear();
                    break;
                }
            }
            courseYearMap.put(c.getCourseId(), year);
        }
        model.addAttribute("courseYearMap", courseYearMap);

        return "teacher/courses";
    }

    @PostMapping("/courses/create")
    public String createCourse(@Valid @ModelAttribute("createForm") CourseCreateForm form,
                               BindingResult br,
                               jakarta.servlet.http.HttpSession session,
                               Model model,
                               RedirectAttributes ra) {

        if (br.hasErrors()) {
            return coursesPage(session, model, form);
        }

        String id = form.getCourseId().trim().toUpperCase();
        String subject = form.getSubject().trim();
        String grade = form.getGrade().trim();
        int year = form.getYear();

        if (courseRepository.existsById(id)) {
            br.rejectValue("courseId", "duplicate", "Course ID already exists!");
            return coursesPage(session, model, form);
        }

        Course course = new Course();
        course.setCourseId(id);
        course.setSubject(subject);
        course.setGrade(grade);
        course.setArchived(false);
        courseRepository.save(course);

        Batch batch = new Batch();
        batch.setCourse(course);
        batch.setBatchName(id + "-B1");
        batch.setYear(year);
        batch.setStatus("ACTIVE");
        batchRepository.save(batch);

        ra.addFlashAttribute("success", "Course created + default batch created!");
        return "redirect:/teacher/courses";
    }

    @GetMapping("/courses/{id}/edit")
    public String editCoursePage(@PathVariable String id, Model model) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseUpdateForm form = new CourseUpdateForm();
        form.setSubject(course.getSubject());
        form.setGrade(course.getGrade());

        model.addAttribute("course", course);
        model.addAttribute("updateForm", form);

        return "teacher/course-edit";
    }

    @PostMapping("/courses/{id}/update")
    public String updateCourse(@PathVariable String id,
                               @Valid @ModelAttribute("updateForm") CourseUpdateForm form,
                               BindingResult br,
                               Model model,
                               RedirectAttributes ra) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (br.hasErrors()) {
            model.addAttribute("course", course);
            return "teacher/course-edit";
        }

        course.setSubject(form.getSubject().trim());
        course.setGrade(form.getGrade().trim());
        courseRepository.save(course);

        ra.addFlashAttribute("success", "Course updated successfully!");
        return "redirect:/teacher/courses";
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable String id, RedirectAttributes ra) {

        String courseId = id.trim().toUpperCase();

        boolean hasEnrollments = enrollmentRepository.findAll().stream()
                .anyMatch(e -> e.getBatch() != null
                        && e.getBatch().getCourse() != null
                        && e.getBatch().getCourse().getCourseId() != null
                        && courseId.equalsIgnoreCase(e.getBatch().getCourse().getCourseId()));

        if (hasEnrollments) {
            ra.addFlashAttribute("error", "Cannot delete. Students are enrolled. Please archive instead.");
            return "redirect:/teacher/courses";
        }

        batchRepository.findAll().stream()
                .filter(b -> b.getCourse() != null
                        && b.getCourse().getCourseId() != null
                        && courseId.equalsIgnoreCase(b.getCourse().getCourseId()))
                .forEach(b -> {
                    // Delete materials first (FK: material.batch_id → batch.batch_id)
                    materialRepository.deleteByBatch_BatchId(b.getBatchId());
                    batchRepository.deleteById(b.getBatchId());
                });

        courseRepository.deleteById(courseId);

        ra.addFlashAttribute("success", "Course deleted!");
        return "redirect:/teacher/courses";
    }

    @PostMapping("/courses/{id}/archive")
    public String archiveCourse(@PathVariable String id, RedirectAttributes ra) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setArchived(true);
        courseRepository.save(course);
        ra.addFlashAttribute("success", "Course archived!");
        return "redirect:/teacher/courses";
    }

    @PostMapping("/courses/{id}/unarchive")
    public String unarchiveCourse(@PathVariable String id, RedirectAttributes ra) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setArchived(false);
        courseRepository.save(course);
        ra.addFlashAttribute("success", "Course unarchived!");
        return "redirect:/teacher/courses";
    }

    @GetMapping("/enrollments")
    public String enrollmentsPage(jakarta.servlet.http.HttpSession session, Model model) {
        model.addAttribute("teacherId", session.getAttribute("teacherId"));
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        model.addAttribute("enrollments", enrollments);
        return "teacher/enrollments";
    }

    @PostMapping("/enrollments/{id}/approve")
    public String approveEnrollment(@PathVariable Long id, RedirectAttributes ra) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        enrollment.setEnrollmentStatus(EnrollmentStatus.APPROVED);
        enrollmentRepository.save(enrollment);
        ra.addFlashAttribute("success", "Enrollment approved!");
        return "redirect:/teacher/enrollments";
    }

    @PostMapping("/enrollments/{id}/reject")
    public String rejectEnrollment(@PathVariable Long id, RedirectAttributes ra) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        enrollment.setEnrollmentStatus(EnrollmentStatus.REJECTED);
        enrollmentRepository.save(enrollment);
        ra.addFlashAttribute("success", "Enrollment rejected!");
        return "redirect:/teacher/enrollments";
    }

    @GetMapping("/payment-slips")
    public String paymentSlipsPage(jakarta.servlet.http.HttpSession session, Model model) {
        model.addAttribute("teacherId", session.getAttribute("teacherId"));
        model.addAttribute("slips", paymentSlipRepository.findAllByOrderByUploadedDateDesc());
        return "teacher/payment-slips";
    }

    @PostMapping("/payment-slips/{id}/approve")
    public String approvePaymentSlip(@PathVariable Long id, RedirectAttributes ra) {
        PaymentSlip slip = paymentSlipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment slip not found"));

        slip.setPaymentStatus(PaymentStatus.APPROVED);
        paymentSlipRepository.save(slip);

        ra.addFlashAttribute("success", "Payment slip approved!");
        return "redirect:/teacher/payment-slips";
    }

    @PostMapping("/payment-slips/{id}/reject")
    public String rejectPaymentSlip(@PathVariable Long id, RedirectAttributes ra) {
        PaymentSlip slip = paymentSlipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment slip not found"));

        slip.setPaymentStatus(PaymentStatus.REJECTED);
        paymentSlipRepository.save(slip);

        ra.addFlashAttribute("success", "Payment slip rejected!");
        return "redirect:/teacher/payment-slips";
    }

    @GetMapping("/materials")
    public String materialsPage(jakarta.servlet.http.HttpSession session, Model model,
                                @ModelAttribute("materialForm") MaterialUploadForm materialForm) {
        model.addAttribute("teacherId", session.getAttribute("teacherId"));
        model.addAttribute("batches", batchRepository.findAllByOrderByBatchIdDesc());
        model.addAttribute("materials", materialRepository.findAll());
        return "teacher/materials";
    }

    @PostMapping("/materials/upload")
    public String uploadMaterial(@Valid @ModelAttribute("materialForm") MaterialUploadForm materialForm,
                                 BindingResult br,
                                 jakarta.servlet.http.HttpSession session,
                                 Model model,
                                 RedirectAttributes ra) {

        Batch batch = null;
        if (materialForm.getGrade() == null || materialForm.getSubject() == null || materialForm.getGrade().isEmpty() || materialForm.getSubject().isEmpty()) {
            br.rejectValue("grade", "required", "Please select grade and subject.");
        } else {
            String searchGrade = materialForm.getGrade().trim();
            String searchSubject = materialForm.getSubject().trim();

            Course course = courseRepository.findByGradeIgnoreCaseAndSubjectIgnoreCase(searchGrade, searchSubject)
                    .orElseGet(() -> courseRepository.findByGradeIgnoreCaseAndSubjectIgnoreCase("Grade " + searchGrade, searchSubject).orElse(null));

            if (course == null) {
                br.rejectValue("subject", "invalid", "No course found for this grade and subject.");
            } else {
                batch = batchRepository.findFirstByCourse_CourseIdAndStatusOrderByBatchIdAsc(course.getCourseId(), "ACTIVE").orElse(null);
                if (batch == null) {
                    br.rejectValue("subject", "invalid", "No active batch found for this course.");
                }
            }
        }

        MultipartFile file = materialForm.getMaterialFile();

        Material material;
        boolean isEdit = materialForm.getMaterialId() != null;

        if (isEdit) {
            material = materialRepository.findById(materialForm.getMaterialId()).orElse(null);
            if (material == null) {
                ra.addFlashAttribute("error", "Material not found.");
                return "redirect:/teacher/materials";
            }
        } else {
            material = new Material();
        }

        if (!isEdit && (file == null || file.isEmpty())) {
            br.rejectValue("materialFile", "required", "Please upload a material file.");
        }

        if (br.hasErrors()) {
            return materialsPage(session, model, materialForm);
        }

        material.setBatch(batch);
        material.setTitle(materialForm.getTitle().trim());
        material.setMonth(materialForm.getMonth());
        material.setYear(materialForm.getYear());

        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String extension = "";

            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;

            try {
                Path uploadPath = Paths.get("uploads/materials");
                Files.createDirectories(uploadPath);
                Files.write(uploadPath.resolve(fileName), file.getBytes());
            } catch (IOException e) {
                br.reject("fileError", "An error occurred while saving the material file.");
                return materialsPage(session, model, materialForm);
            }

            material.setFilePath("/uploads/materials/" + fileName);
        }

        if (!isEdit) {
            material.setUploadedDate(LocalDate.now());
        }

        materialRepository.save(material);

        ra.addFlashAttribute("success", isEdit ? "Material updated successfully!" : "Material uploaded successfully!");
        return "redirect:/teacher/materials";
    }

    @GetMapping("/materials/{id}/edit")
    public String editMaterial(@PathVariable Long id,
                               Model model,
                               RedirectAttributes ra) {

        Material material = materialRepository.findById(id).orElse(null);
        if (material == null) {
            ra.addFlashAttribute("error", "Material not found.");
            return "redirect:/teacher/materials";
        }

        MaterialUploadForm form = new MaterialUploadForm();
        form.setMaterialId(material.getMaterialId());
        if (material.getBatch() != null && material.getBatch().getCourse() != null) {
            form.setGrade(material.getBatch().getCourse().getGrade());
            form.setSubject(material.getBatch().getCourse().getSubject());
        }
        form.setTitle(material.getTitle());
        form.setMonth(material.getMonth());
        form.setYear(material.getYear());

        model.addAttribute("materialForm", form);
        model.addAttribute("batches", batchRepository.findAllByOrderByBatchIdDesc());
        model.addAttribute("materials", materialRepository.findAll());

        return "teacher/materials";
    }

    @PostMapping("/materials/{id}/delete")
    public String deleteMaterial(@PathVariable Long id,
                                 RedirectAttributes ra) {

        Material material = materialRepository.findById(id).orElse(null);
        if (material == null) {
            ra.addFlashAttribute("error", "Material not found.");
            return "redirect:/teacher/materials";
        }

        materialRepository.delete(material);
        ra.addFlashAttribute("success", "Material deleted successfully!");
        return "redirect:/teacher/materials";
    }

    @GetMapping("/payment-tracking")
    public String paymentTrackingPage(jakarta.servlet.http.HttpSession session, Model model,
                                      @ModelAttribute("filterForm") PaymentTrackingFilterForm filterForm) {
        model.addAttribute("teacherId", session.getAttribute("teacherId"));

        model.addAttribute("paidEnrollments", new ArrayList<Enrollment>());
        model.addAttribute("unpaidEnrollments", new ArrayList<Enrollment>());

        return "teacher/payment-tracking";
    }

    @PostMapping("/payment-tracking")
    public String paymentTrackingResults(@Valid @ModelAttribute("filterForm") PaymentTrackingFilterForm filterForm,
                                         BindingResult br,
                                         Model model) {

        List<Enrollment> paidEnrollments = new ArrayList<>();
        List<Enrollment> unpaidEnrollments = new ArrayList<>();

        if (!br.hasErrors()) {
            List<Enrollment> approvedEnrollments =
                    enrollmentRepository.findByEnrollmentStatusOrderByEnrollmentIdDesc(EnrollmentStatus.APPROVED);

            List<PaymentSlip> approvedSlips =
                    paymentSlipRepository.findByMonthAndYearAndPaymentStatus(
                            filterForm.getMonth(),
                            filterForm.getYear(),
                            PaymentStatus.APPROVED
                    );

            Set<Long> paidEnrollmentIds = new HashSet<>();
            for (PaymentSlip slip : approvedSlips) {
                if (slip.getEnrollment() != null && slip.getEnrollment().getEnrollmentId() != null) {
                    paidEnrollmentIds.add(slip.getEnrollment().getEnrollmentId());
                }
            }

            for (Enrollment enrollment : approvedEnrollments) {
                if (paidEnrollmentIds.contains(enrollment.getEnrollmentId())) {
                    paidEnrollments.add(enrollment);
                } else {
                    unpaidEnrollments.add(enrollment);
                }
            }
        }

        model.addAttribute("paidEnrollments", paidEnrollments);
        model.addAttribute("unpaidEnrollments", unpaidEnrollments);

        return "teacher/payment-tracking";
    }
}
