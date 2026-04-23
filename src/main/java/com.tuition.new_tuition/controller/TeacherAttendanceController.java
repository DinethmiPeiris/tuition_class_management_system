package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.AttendanceMarkForm;
import com.tuition.new_tuition.dto.AttendanceMarkRow;
import com.tuition.new_tuition.dto.MonthlyAttendanceSummaryRow;
import com.tuition.new_tuition.dto.SessionAttendanceStatusDTO;
import com.tuition.new_tuition.entity.AppUser;
import com.tuition.new_tuition.entity.AttendanceRecord;
import com.tuition.new_tuition.entity.AttendanceStatus;
import com.tuition.new_tuition.entity.TimetableSession;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.repository.CourseRepository;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.repository.UserRepository;
import com.tuition.new_tuition.service.AbsenceNotificationService;
import com.tuition.new_tuition.service.AttendanceService;
import com.tuition.new_tuition.service.TimetableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/attendance")
public class TeacherAttendanceController {

    private final TimetableService timetableService;
    private final AttendanceService attendanceService;
    private final AbsenceNotificationService absenceNotificationService;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final com.tuition.new_tuition.repository.BatchRepository batchRepository;
    private final CourseRepository courseRepository;

    public TeacherAttendanceController(TimetableService timetableService,
            AttendanceService attendanceService,
            AbsenceNotificationService absenceNotificationService,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            com.tuition.new_tuition.repository.BatchRepository batchRepository,
            CourseRepository courseRepository) {
        this.timetableService = timetableService;
        this.attendanceService = attendanceService;
        this.absenceNotificationService = absenceNotificationService;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
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
        // DYNAMIC: Show real batches from the database instead of hardcoded A/B/C
        batchRepository.findAll().forEach(b -> {
            if (b.getBatchId() != null) {
                map.put(b.getBatchId(), b.getBatchName());
            }
        });
        return map;
    }

    private Long getTeacherId(HttpSession session) {
        return (Long) session.getAttribute("teacherId");
    }

    @GetMapping("/select")
    public String selectPage(@RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "grade", required = false) String grade,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "batchId", required = false) Long batchId,
            HttpSession httpSession,
            Model model) {

        List<TimetableSession> sessions = timetableService.getAllSessionsOldestFirst();
        Map<Long, String> subjectMap = subjectMap();
        Map<Long, String> batchMap = batchMap();

        sessions = sessions.stream()
                .filter(s -> s.getDate() != null)
                .sorted(Comparator.comparing(TimetableSession::getDate))
                .collect(Collectors.toList());

        LocalDate selectedDate = null;

        if (dateStr != null && !dateStr.isBlank()) {
            selectedDate = LocalDate.parse(dateStr);
            LocalDate finalSelectedDate = selectedDate;
            sessions = sessions.stream()
                    .filter(s -> finalSelectedDate.equals(s.getDate()))
                    .collect(Collectors.toList());
        }

        if (grade != null && !grade.isBlank()) {
            sessions = sessions.stream()
                    .filter(s -> {
                        if (s.getGrade() == null) return false;
                        String storedGrade = String.valueOf(s.getGrade());
                        return storedGrade.equalsIgnoreCase(grade) 
                            || ("Grade " + storedGrade).equalsIgnoreCase(grade)
                            || grade.equalsIgnoreCase(storedGrade);
                    })
                    .collect(Collectors.toList());
        }

        if (subject != null && !subject.isBlank()) {
            String finalSubject = subject.trim();
            sessions = sessions.stream()
                    .filter(s -> {
                        String subjectName = subjectMap.get(s.getSubjectId());
                        return subjectName != null && subjectName.equalsIgnoreCase(finalSubject);
                    })
                    .collect(Collectors.toList());
        }

        if (batchId != null) {
            sessions = sessions.stream()
                    .filter(s -> batchId.equals(s.getBatchId()))
                    .collect(Collectors.toList());
        }

        List<SessionAttendanceStatusDTO> sessionStatuses = new ArrayList<>();

        for (TimetableSession session : sessions) {
            String status;

            if (session.getDate() != null && session.getDate().isAfter(LocalDate.now())) {
                status = "FUTURE";
            } else if (attendanceService.isAttendanceMarked(session.getId(), session.getDate())) {
                status = "MARKED";
            } else {
                status = "NOT_MARKED";
            }

            sessionStatuses.add(new SessionAttendanceStatusDTO(session, status));
        }

        List<String> grades = courseRepository.findDistinctGrades();
        if (grades.isEmpty()) grades = Arrays.asList("Grade 10", "Grade 11");

        List<String> subjects = courseRepository.findDistinctSubjects();
        if (subjects.isEmpty()) subjects = Arrays.asList("Mathematics", "Science");

        model.addAttribute("sessionStatuses", sessionStatuses);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedGrade", grade);
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedBatchId", batchId);
        model.addAttribute("grades", grades);
        model.addAttribute("subjects", subjects);
        model.addAttribute("subjectMap", subjectMap);
        model.addAttribute("batchMap", batchMap);
        model.addAttribute("allBatches", batchRepository.findAll());

        return "attendance-select";
    }

    @GetMapping("/mark")
    public String markPage(@RequestParam("sessionId") Long sessionId,
            @RequestParam("date") LocalDate date,
            HttpSession httpSession,
            Model model) {

        TimetableSession selectedSession = timetableService.getById(sessionId);

        // Fetch ONLY students enrolled in this specific Batch
        Set<AppUser> sessionStudentsSet = new HashSet<>();
        List<EnrollmentStatus> statuses = List.of(EnrollmentStatus.APPROVED, EnrollmentStatus.PENDING);
        
        if (selectedSession.getBatchId() != null) {
            List<Enrollment> enrollments = enrollmentRepository.findByBatch_BatchIdAndEnrollmentStatusIn(
                    selectedSession.getBatchId(), statuses);
            
            for (Enrollment enrollment : enrollments) {
                userRepository.findByEmailIgnoreCase(enrollment.getStudent().getEmail())
                    .ifPresent(sessionStudentsSet::add);
            }
        }
        
        // Convert set to list for the roster form
        List<AppUser> sessionStudents = new ArrayList<>(sessionStudentsSet);

        List<AttendanceRecord> existing = attendanceService.getExisting(sessionId, date);

        Map<Long, AttendanceStatus> statusMap = new HashMap<>();
        for (AttendanceRecord record : existing) {
            statusMap.put(record.getStudentId(), record.getStatus());
        }

        AttendanceMarkForm form = new AttendanceMarkForm();
        form.setSessionId(sessionId);
        form.setDate(date);

        List<AttendanceMarkRow> rows = new ArrayList<>();
        for (AppUser user : sessionStudents) {
            AttendanceMarkRow row = new AttendanceMarkRow();
            row.setStudentId(user.getId());
            row.setStudentName(user.getName() != null ? user.getName() : user.getEmail());
            row.setStudentEmail(user.getEmail());
            row.setStatus(statusMap.getOrDefault(user.getId(), AttendanceStatus.ABSENT));
            rows.add(row);
        }

        form.setRows(rows);

        model.addAttribute("form", form);
        model.addAttribute("selectedSession", selectedSession);
        model.addAttribute("selectedDate", date);
        model.addAttribute("subjectMap", subjectMap());
        model.addAttribute("batchMap", batchMap());

        return "attendance-mark";
    }

    @PostMapping("/save")
    public String saveMarking(@ModelAttribute("form") AttendanceMarkForm form, HttpSession httpSession) {

        Long teacherId = getTeacherId(httpSession);

        for (AttendanceMarkRow row : form.getRows()) {
            attendanceService.saveOne(
                    teacherId,
                    form.getSessionId(),
                    row.getStudentId(),
                    form.getDate(),
                    row.getStatus());

            if (row.getStatus() == AttendanceStatus.ABSENT) {
                absenceNotificationService.createAbsenceNotification(
                        row.getStudentId(),
                        form.getSessionId(),
                        form.getDate());
            } else {
                absenceNotificationService.removeAbsenceNotification(
                        row.getStudentId(),
                        form.getSessionId(),
                        form.getDate());
            }
        }

        return "redirect:/teacher/attendance/select?date=" + form.getDate().toString();
    }

    @GetMapping("/monthly-summary")
    public String monthlySummaryPage(@RequestParam(value = "month", required = false) String monthStr,
            @RequestParam(value = "grade", required = false) String grade,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "batchId", required = false) Long batchId,
            HttpSession httpSession,
            Model model) {

        YearMonth selectedMonth;
        if (monthStr == null || monthStr.isBlank()) {
            selectedMonth = YearMonth.now();
        } else {
            selectedMonth = YearMonth.parse(monthStr);
        }

        List<String> grades = courseRepository.findDistinctGrades();
        List<String> subjects = courseRepository.findDistinctSubjects();
        
        List<MonthlyAttendanceSummaryRow> summaries = new ArrayList<>();
        if (grade != null && !grade.isBlank() && subject != null && !subject.isBlank()) {
            summaries = attendanceService.getMonthlySummary(selectedMonth, grade, subject, batchId);
        }

        model.addAttribute("selectedMonth", selectedMonth.toString());
        model.addAttribute("selectedGrade", grade);
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedBatchId", batchId);
        model.addAttribute("grades", grades);
        model.addAttribute("subjects", subjects);
        model.addAttribute("allBatches", batchRepository.findAllByOrderByBatchIdAsc());
        model.addAttribute("summaries", summaries);

        return "teacher-monthly-attendance-summary";
    }
}
