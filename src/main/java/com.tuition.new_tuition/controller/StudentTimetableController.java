package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.TimetableSession;
import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.entity.Batch;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.repository.StudentRepository;
import com.tuition.new_tuition.repository.TimetableSessionRepository;
import com.tuition.new_tuition.repository.BatchRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentTimetableController {

    private final TimetableSessionRepository timetableSessionRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BatchRepository batchRepository;

    public StudentTimetableController(TimetableSessionRepository timetableSessionRepository,
                                     StudentRepository studentRepository,
                                     EnrollmentRepository enrollmentRepository,
                                     BatchRepository batchRepository) {
        this.timetableSessionRepository = timetableSessionRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.batchRepository = batchRepository;
    }

    @GetMapping("/timetable")
    public String showStudentTimetable(@RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "subject", required = false) String subject,
            Model model, HttpSession session) {

        String username = (String) session.getAttribute("studentUsername");

        Student student = studentRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (student == null) {
            System.out.println("DEBUG: Student not found for username: " + username);
            sendEmpty(model);
            return "student-timetable";
        }

        // Step 1: Get student's enrollments (APPROVED and PENDING only)
        List<EnrollmentStatus> statusList = Arrays.asList(EnrollmentStatus.APPROVED, EnrollmentStatus.PENDING);
        List<Enrollment> enrollments = enrollmentRepository.findByStudent_IdAndEnrollmentStatusIn(student.getId(), statusList);
        
        System.out.println("DEBUG: Student " + username + " (ID: " + student.getId() + ") has " + enrollments.size() + " enrollments.");

        if (enrollments.isEmpty()) {
            sendEmpty(model);
            return "student-timetable";
        }

        // Step 2: Collect the batchIds the student is enrolled in
        Set<Long> enrolledBatchIds = enrollments.stream()
                .filter(e -> e.getBatch() != null && e.getBatch().getBatchId() != null)
                .map(e -> e.getBatch().getBatchId())
                .collect(Collectors.toSet());

        // Also track the valid Grade/Subject pairs for safety
        Set<String> validCombinations = enrollments.stream()
                .filter(e -> e.getBatch() != null && e.getBatch().getCourse() != null)
                .map(e -> e.getBatch().getCourse().getGrade() + "|" + e.getBatch().getCourse().getSubject())
                .collect(Collectors.toSet());

        System.out.println("DEBUG: Enrolled Batch IDs: " + enrolledBatchIds);
        System.out.println("DEBUG: Valid Combinations: " + validCombinations);
        if (enrolledBatchIds.isEmpty()) {
            sendEmpty(model);
            return "student-timetable";
        }

        // Step 3: Load ALL sessions belonging to the student's enrolled batches
        List<TimetableSession> allEnrolledSessions = timetableSessionRepository.findByBatchIdIn(enrolledBatchIds);
        
        // STRICTOR FILTER: Ensure the session's grade/subject matches the student's actual enrollment metadata
        // (This prevents cross-grade leaks if batch IDs are reused/misconfigured)
        allEnrolledSessions = allEnrolledSessions.stream().filter(s -> {
            Integer sGrade = s.getGrade();
            Long sBatchId = s.getBatchId();
            
            return enrollments.stream().anyMatch(e -> {
                if (e.getBatch() == null || e.getBatch().getCourse() == null) return false;
                if (!e.getBatch().getBatchId().equals(sBatchId)) return false;
                
                String eGradeStr = e.getBatch().getCourse().getGrade();
                try {
                    Integer eGrade = Integer.parseInt(eGradeStr.replaceAll("[^0-9]", ""));
                    return eGrade.equals(sGrade);
                } catch (Exception ex) {
                    return true; // Fallback if grade is not numeric
                }
            });
        }).collect(Collectors.toList());

        allEnrolledSessions.sort(Comparator.comparing(TimetableSession::getDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        // Step 4: Map sessions to Subject Names using the Batch relationship (more robust than IDs)
        Map<Long, String> sessionSubjectMap = new HashMap<>();
        List<Batch> allBatches = batchRepository.findAll();
        for (TimetableSession s : allEnrolledSessions) {
            allBatches.stream()
                    .filter(b -> b.getBatchId().equals(s.getBatchId()))
                    .findFirst()
                    .ifPresent(b -> {
                        if (b.getCourse() != null && b.getCourse().getSubject() != null) {
                            sessionSubjectMap.put(s.getId(), b.getCourse().getSubject());
                        }
                    });
        }

        // Step 5: Populate dropdowns from ALL enrolled sessions (BEFORE filtering for display)
        List<Integer> grades = allEnrolledSessions.stream()
                .map(TimetableSession::getGrade)
                .filter(java.util.Objects::nonNull)
                .distinct().sorted()
                .collect(Collectors.toList());

        List<String> subjectsList = allEnrolledSessions.stream()
                .map(s -> sessionSubjectMap.get(s.getId()))
                .filter(java.util.Objects::nonNull)
                .distinct().sorted()
                .collect(Collectors.toList());

        // Step 6: Apply user-selected filters (Subject/Grade) ONLY to the display list
        List<TimetableSession> displaySessions = new ArrayList<>(allEnrolledSessions);
        if (grade != null) {
            displaySessions = displaySessions.stream()
                    .filter(s -> grade.equals(s.getGrade()))
                    .collect(Collectors.toList());
        }
        if (subject != null && !subject.isBlank()) {
            displaySessions = displaySessions.stream()
                    .filter(s -> {
                        String name = sessionSubjectMap.get(s.getId());
                        return name != null && name.equalsIgnoreCase(subject);
                    })
                    .collect(Collectors.toList());
        }

        Map<Long, String> batchMap = allBatches.stream()
                .filter(b -> b.getBatchId() != null)
                .collect(Collectors.toMap(
                        Batch::getBatchId,
                        b -> b.getBatchName() != null ? b.getBatchName() : "Unnamed Batch",
                        (v1, v2) -> v1));

        model.addAttribute("sessions", displaySessions);
        model.addAttribute("subjectMap", sessionSubjectMap);
        model.addAttribute("batchMap", batchMap);
        model.addAttribute("grades", grades);
        model.addAttribute("subjects", subjectsList);
        model.addAttribute("selectedGrade", grade);
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("totalSchedules", displaySessions.size());

        return "student-timetable";
    }

    private void sendEmpty(Model model) {
        model.addAttribute("sessions", List.of());
        model.addAttribute("subjectMap", new HashMap<>());
        model.addAttribute("batchMap", new HashMap<>());
        model.addAttribute("grades", List.of());
        model.addAttribute("subjects", List.of());
        model.addAttribute("selectedGrade", null);
        model.addAttribute("selectedSubject", null);
        model.addAttribute("totalSchedules", 0);
    }
}
