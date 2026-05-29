package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.TimetableForm;
import com.tuition.new_tuition.entity.TimetableSession;
import com.tuition.new_tuition.entity.Batch;
import com.tuition.new_tuition.exception.TimetableConflictException;
import com.tuition.new_tuition.service.TimetableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/timetable")
public class TimetablePageController {

    private final TimetableService timetableService;
    private final com.tuition.new_tuition.repository.BatchRepository batchRepository;
    private final com.tuition.new_tuition.repository.CourseRepository courseRepository;

    public TimetablePageController(TimetableService timetableService,
                                   com.tuition.new_tuition.repository.BatchRepository batchRepository,
                                   com.tuition.new_tuition.repository.CourseRepository courseRepository) {
        this.timetableService = timetableService;
        this.batchRepository = batchRepository;
        this.courseRepository = courseRepository;
    }

    private Map<Long, String> subjectMap() {
        Map<Long, String> map = new HashMap<>();
        // Standard mapping: 1L for Math, 2L for Science (compatible with legacy records)
        map.put(1L, "Mathematics");
        map.put(2L, "Science");
        return map;
    }

    private List<Batch> getAllBatches() {
        return batchRepository.findAll();
    }

    private Long getTeacherId(HttpSession session) {
        return (Long) session.getAttribute("teacherId");
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("timetableForm")) {
            model.addAttribute("timetableForm", new TimetableForm());
        }
        model.addAttribute("subjectMap", subjectMap());
        model.addAttribute("allBatches", getAllBatches());
        model.addAttribute("isEdit", false);
        return "timetable-form";
    }

    @PostMapping("/save")
    public String saveTimetable(@ModelAttribute("timetableForm") TimetableForm form,
            HttpSession httpSession,
            Model model) {
        try {
            TimetableSession timetableSession = new TimetableSession();

            timetableSession.setTeacherId(getTeacherId(httpSession));
            timetableSession.setGrade(form.getGrade());
            timetableSession.setSubjectId(form.getSubjectId());
            timetableSession.setBatchId(form.getBatchId());
            timetableSession.setDate(form.getDate());
            timetableSession.setClassType(form.getClassType());
            
            // Handle different class types
            if ("Individual".equalsIgnoreCase(form.getClassType())) {
                timetableSession.setLocation("Individual Session");
                timetableSession.setMaxMembers(1);
                timetableSession.setOnlineLink(null);
            } else if ("Batch".equalsIgnoreCase(form.getClassType())) {
                timetableSession.setLocation("Online");
                timetableSession.setMaxMembers(form.getMaxMembers());
                timetableSession.setOnlineLink(form.getOnlineLink());
            } else {
                // Group or others
                timetableSession.setLocation(form.getLocation());
                timetableSession.setMaxMembers(form.getMaxMembers());
                timetableSession.setOnlineLink(null);
            }

            if (form.getDate() != null && form.getStartTime() != null) {
                timetableSession.setStartTime(LocalDateTime.of(form.getDate(), form.getStartTime()));
            }

            if (form.getDate() != null && form.getEndTime() != null) {
                timetableSession.setEndTime(LocalDateTime.of(form.getDate(), form.getEndTime()));
            }

            timetableService.save(timetableSession);
            return "redirect:/teacher/timetable/list";

        } catch (TimetableConflictException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("timetableForm", form);
            model.addAttribute("subjectMap", subjectMap());
            model.addAttribute("allBatches", getAllBatches());
            model.addAttribute("isEdit", false);
            return "timetable-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id,
                               @RequestParam(value = "returnGrade",    required = false) String returnGrade,
                               @RequestParam(value = "returnSubject",  required = false) String returnSubject,
                               @RequestParam(value = "returnBatchId",  required = false) String returnBatchId,
                               Model model) {
        TimetableSession session = timetableService.getById(id);

        TimetableForm form = new TimetableForm();
        form.setId(session.getId());
        form.setGrade(session.getGrade());
        form.setSubjectId(session.getSubjectId());
        form.setBatchId(session.getBatchId());
        form.setDate(session.getDate());
        form.setLocation(session.getLocation());
        form.setClassType(session.getClassType());
        form.setMaxMembers(session.getMaxMembers());
        form.setOnlineLink(session.getOnlineLink());

        // Store return-to filter params so the update redirect can go back to the same filtered list
        form.setReturnGrade(returnGrade);
        form.setReturnSubject(returnSubject);
        form.setReturnBatchId(returnBatchId);

        if (session.getStartTime() != null) {
            form.setStartTime(session.getStartTime().toLocalTime());
        }

        if (session.getEndTime() != null) {
            form.setEndTime(session.getEndTime().toLocalTime());
        }

        model.addAttribute("timetableForm", form);
        model.addAttribute("subjectMap", subjectMap());
        model.addAttribute("allBatches", getAllBatches());
        model.addAttribute("isEdit", true);
        return "timetable-form";
    }

    @PostMapping("/update")
    public String updateTimetable(@ModelAttribute("timetableForm") TimetableForm form,
            HttpSession httpSession,
            Model model) {
        try {
            TimetableSession timetableSession = timetableService.getById(form.getId());

            timetableSession.setTeacherId(getTeacherId(httpSession));
            timetableSession.setGrade(form.getGrade());
            timetableSession.setSubjectId(form.getSubjectId());
            timetableSession.setBatchId(form.getBatchId());
            timetableSession.setDate(form.getDate());
            timetableSession.setClassType(form.getClassType());
            
            // Handle different class types
            if ("Individual".equalsIgnoreCase(form.getClassType())) {
                timetableSession.setLocation("Individual Session");
                timetableSession.setMaxMembers(1);
                timetableSession.setOnlineLink(null);
            } else if ("Batch".equalsIgnoreCase(form.getClassType())) {
                timetableSession.setLocation("Online");
                timetableSession.setMaxMembers(form.getMaxMembers());
                timetableSession.setOnlineLink(form.getOnlineLink());
            } else {
                // Group or others
                timetableSession.setLocation(form.getLocation());
                timetableSession.setMaxMembers(form.getMaxMembers());
                timetableSession.setOnlineLink(null);
            }

            if (form.getDate() != null && form.getStartTime() != null) {
                timetableSession.setStartTime(LocalDateTime.of(form.getDate(), form.getStartTime()));
            }

            if (form.getDate() != null && form.getEndTime() != null) {
                timetableSession.setEndTime(LocalDateTime.of(form.getDate(), form.getEndTime()));
            }

            timetableService.save(timetableSession);

            // Build the redirect URL back to the same filtered list
            String redirectUrl = buildReturnUrl(form.getReturnGrade(), form.getReturnSubject(), form.getReturnBatchId());
            return "redirect:" + redirectUrl;

        } catch (TimetableConflictException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("timetableForm", form);
            model.addAttribute("subjectMap", subjectMap());
            model.addAttribute("allBatches", getAllBatches());
            model.addAttribute("isEdit", true);
            return "timetable-form";
        }
    }

    /** Builds the redirect URL back to the teacher timetable list, preserving filter params. */
    private String buildReturnUrl(String returnGrade, String returnSubject, String returnBatchId) {
        StringBuilder url = new StringBuilder("/teacher/timetable/list");
        boolean first = true;
        if (returnGrade != null && !returnGrade.isBlank()) {
            url.append(first ? "?" : "&").append("grade=").append(returnGrade);
            first = false;
        }
        if (returnSubject != null && !returnSubject.isBlank()) {
            url.append(first ? "?" : "&").append("subject=").append(returnSubject);
            first = false;
        }
        if (returnBatchId != null && !returnBatchId.isBlank()) {
            url.append(first ? "?" : "&").append("batchId=").append(returnBatchId);
        }
        return url.toString();
    }
}
