package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.TimetableSession;
import com.tuition.new_tuition.service.TimetableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/timetable")
public class TeacherTimetableController {

    private final TimetableService timetableService;
    private final com.tuition.new_tuition.repository.BatchRepository batchRepository;
    private final com.tuition.new_tuition.repository.CourseRepository courseRepository;

    public TeacherTimetableController(TimetableService timetableService,
                                     com.tuition.new_tuition.repository.BatchRepository batchRepository,
                                     com.tuition.new_tuition.repository.CourseRepository courseRepository) {
        this.timetableService = timetableService;
        this.batchRepository = batchRepository;
        this.courseRepository = courseRepository;
    }

    private Map<Long, String> subjectMap() {
        Map<Long, String> map = new HashMap<>();
        map.put(1L, "Mathematics");
        map.put(2L, "Science");
        return map;
    }

    private Map<Long, String> batchMap() {
        Map<Long, String> map = new HashMap<>();
        batchRepository.findAll().forEach(b -> {
            if (b.getBatchId() != null) {
                map.put(b.getBatchId(), b.getBatchName());
            }
        });
        return map;
    }

    @GetMapping
    public String redirectToList() {
        return "redirect:/teacher/timetable/list";
    }

    @GetMapping("/list")
    public String showTeacherTimetable(@RequestParam(value = "grade", required = false) String grade,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "batchId", required = false) Long batchId,
            jakarta.servlet.http.HttpSession session,
            Model model) {
        if (session.getAttribute("teacherId") == null) {
            return "redirect:/teacher/login";
        }

        List<TimetableSession> sessions = timetableService.getAllSessionsOldestFirst();
        Map<Long, String> subjectMap = subjectMap();
        Map<Long, String> batchMap = batchMap();

        if (grade != null && !grade.isBlank()) {
            sessions = sessions.stream()
                    .filter(tsession -> {
                        if (tsession.getGrade() == null) return false;
                        // Support both numeric and string-prefixed grades
                        String storedGrade = String.valueOf(tsession.getGrade());
                        return storedGrade.equalsIgnoreCase(grade) 
                            || ("Grade " + storedGrade).equalsIgnoreCase(grade)
                            || grade.equalsIgnoreCase(storedGrade);
                    })
                    .collect(Collectors.toList());
        }

        if (subject != null && !subject.isBlank()) {
            sessions = sessions.stream()
                    .filter(tsession -> {
                        String subjectName = subjectMap.get(tsession.getSubjectId());
                        return subjectName != null && subjectName.equalsIgnoreCase(subject);
                    })
                    .collect(Collectors.toList());
        }

        if (batchId != null) {
            sessions = sessions.stream()
                    .filter(tsession -> batchId.equals(tsession.getBatchId()))
                    .collect(Collectors.toList());
        }

        List<String> grades = courseRepository.findDistinctGrades();
        if (grades.isEmpty()) grades = Arrays.asList("Grade 10", "Grade 11");

        List<String> subjects = courseRepository.findDistinctSubjects();
        if (subjects.isEmpty()) subjects = Arrays.asList("Mathematics", "Science");

        model.addAttribute("sessions", sessions);
        model.addAttribute("subjectMap", subjectMap);
        model.addAttribute("batchMap", batchMap);
        model.addAttribute("grades", grades);
        model.addAttribute("subjects", subjects);
        model.addAttribute("allBatches", batchRepository.findAll());
        model.addAttribute("selectedGrade", grade);
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedBatchId", batchId);
        model.addAttribute("totalSchedules", sessions.size());

        return "teacher-timetable";
    }

    @PostMapping("/delete/{id}")
    public String deleteTimetable(@PathVariable("id") Long id, jakarta.servlet.http.HttpSession session) {
        timetableService.deleteById(id);
        return "redirect:/teacher/timetable/list";
    }
}
