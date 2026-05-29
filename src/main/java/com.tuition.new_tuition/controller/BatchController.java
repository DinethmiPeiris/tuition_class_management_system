package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.BatchForm;
import com.tuition.new_tuition.entity.Batch;
import com.tuition.new_tuition.entity.Course;
import com.tuition.new_tuition.repository.BatchRepository;
import com.tuition.new_tuition.repository.CourseRepository;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teacher/batches")
public class BatchController {

    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public BatchController(BatchRepository batchRepository,
                           CourseRepository courseRepository,
                           EnrollmentRepository enrollmentRepository) {
        this.batchRepository = batchRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping
    public String showBatches(Model model,
                              @ModelAttribute("batchForm") BatchForm batchForm) {
        loadPageData(model);
        return "teacher/batches";
    }

    @PostMapping("/save")
    public String saveBatch(@Valid @ModelAttribute("batchForm") BatchForm form,
                            BindingResult br,
                            Model model,
                            RedirectAttributes ra) {

        Course course = courseRepository.findById(form.getCourseId()).orElse(null);
        if (course == null) {
            br.rejectValue("courseId", "invalid", "Selected course does not exist.");
        } else if (course.isArchived()) {
            br.rejectValue("courseId", "archived", "Archived courses cannot have batch management.");
        }

        if (form.getCourseId() != null && form.getBatchName() != null && form.getYear() != null) {
            boolean duplicate;

            if (form.getBatchId() == null) {
                duplicate = batchRepository.existsByCourse_CourseIdAndBatchNameIgnoreCaseAndYear(
                        form.getCourseId().trim().toUpperCase(),
                        form.getBatchName().trim(),
                        form.getYear()
                );
            } else {
                duplicate = batchRepository.existsByCourse_CourseIdAndBatchNameIgnoreCaseAndYearAndBatchIdNot(
                        form.getCourseId().trim().toUpperCase(),
                        form.getBatchName().trim(),
                        form.getYear(),
                        form.getBatchId()
                );
            }

            if (duplicate) {
                br.reject("duplicate", "A batch with the same name, course, and year already exists.");
            }
        }

        if (br.hasErrors()) {
            loadPageData(model);
            return "teacher/batches";
        }

        Batch batch;
        if (form.getBatchId() != null) {
            batch = batchRepository.findById(form.getBatchId()).orElse(new Batch());
        } else {
            batch = new Batch();
        }

        batch.setBatchName(form.getBatchName().trim());
        batch.setYear(form.getYear());
        batch.setStatus(form.getStatus().trim().toUpperCase());
        batch.setCourse(course);

        batchRepository.save(batch);

        ra.addFlashAttribute("success",
                form.getBatchId() == null ? "Batch created successfully!" : "Batch updated successfully!");
        return "redirect:/teacher/batches";
    }

    @GetMapping("/edit/{id}")
    public String editBatch(@PathVariable Long id,
                            Model model,
                            RedirectAttributes ra) {

        Batch batch = batchRepository.findById(id).orElse(null);
        if (batch == null) {
            ra.addFlashAttribute("error", "Batch not found.");
            return "redirect:/teacher/batches";
        }

        BatchForm form = new BatchForm();
        form.setBatchId(batch.getBatchId());
        form.setBatchName(batch.getBatchName());
        form.setYear(batch.getYear());
        form.setStatus(batch.getStatus());
        form.setCourseId(batch.getCourse().getCourseId());

        model.addAttribute("batchForm", form);
        loadPageData(model);
        return "teacher/batches";
    }

    @PostMapping("/delete/{id}")
    public String deleteBatch(@PathVariable Long id,
                              RedirectAttributes ra) {

        Batch batch = batchRepository.findById(id).orElse(null);
        if (batch == null) {
            ra.addFlashAttribute("error", "Batch not found.");
            return "redirect:/teacher/batches";
        }

        boolean hasEnrollments = enrollmentRepository.findAll().stream()
                .anyMatch(e -> e.getBatch() != null
                        && e.getBatch().getBatchId() != null
                        && e.getBatch().getBatchId().equals(id));

        if (hasEnrollments) {
            ra.addFlashAttribute("error", "Cannot delete. Students are already linked to this batch.");
            return "redirect:/teacher/batches";
        }

        batchRepository.delete(batch);
        ra.addFlashAttribute("success", "Batch deleted successfully!");
        return "redirect:/teacher/batches";
    }

    @PostMapping("/toggle/{id}")
    public String toggleBatchStatus(@PathVariable Long id,
                                    RedirectAttributes ra) {

        Batch batch = batchRepository.findById(id).orElse(null);
        if (batch == null) {
            ra.addFlashAttribute("error", "Batch not found.");
            return "redirect:/teacher/batches";
        }

        if ("ACTIVE".equalsIgnoreCase(batch.getStatus())) {
            batch.setStatus("INACTIVE");
        } else {
            batch.setStatus("ACTIVE");
        }

        batchRepository.save(batch);
        ra.addFlashAttribute("success", "Batch status updated successfully!");
        return "redirect:/teacher/batches";
    }

    private void loadPageData(Model model) {
        List<Batch> batches = batchRepository.findAllByOrderByBatchIdDesc();
        List<Course> courses = courseRepository.findAll();

        model.addAttribute("batches", batches);
        model.addAttribute("courses", courses);
    }
}
