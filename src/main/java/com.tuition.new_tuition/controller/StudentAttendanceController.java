package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.dto.StudentMonthlyAttendanceSummary;
import com.tuition.new_tuition.entity.AbsenceNotification;
import com.tuition.new_tuition.entity.AttendanceRecord;
import com.tuition.new_tuition.entity.AttendanceStatus;
import com.tuition.new_tuition.entity.AppUser;
import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.entity.Role;
import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.entity.TimetableSession;
import com.tuition.new_tuition.repository.CourseRepository;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.repository.StudentRepository;
import com.tuition.new_tuition.repository.UserRepository;
import com.tuition.new_tuition.service.AbsenceNotificationService;
import com.tuition.new_tuition.service.AttendanceService;
import com.tuition.new_tuition.service.TimetableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student/attendance")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;
    private final TimetableService timetableService;
    private final UserRepository userRepository;
    private final AbsenceNotificationService absenceNotificationService;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final com.tuition.new_tuition.repository.BatchRepository batchRepository;
    private final CourseRepository courseRepository;

    public StudentAttendanceController(AttendanceService attendanceService,
            TimetableService timetableService,
            UserRepository userRepository,
            AbsenceNotificationService absenceNotificationService,
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            com.tuition.new_tuition.repository.BatchRepository batchRepository,
            CourseRepository courseRepository) {
        this.attendanceService = attendanceService;
        this.timetableService = timetableService;
        this.userRepository = userRepository;
        this.absenceNotificationService = absenceNotificationService;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
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

    private TimetableSession safeGetSession(Long sessionId) {
        try {
            return timetableService.getById(sessionId);
        } catch (RuntimeException ex) {
            return null;
        }
    }



    @GetMapping
    public String showAttendanceSessions(@RequestParam(value = "grade", required = false) String grade,
            @RequestParam(value = "subject", required = false) String subject,
            HttpSession httpSession,
            Model model) {

        // --- Step 1: Get the Student record directly from session (enrollment key) ---
        String sessionUsername = (String) httpSession.getAttribute("studentUsername");

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) return "redirect:/student/login";

        // --- Step 2: Resolve AppUser ID for attendance record matching (stored in attendance_records.student_id) ---
        // AppUser (users table) may or may not exist for this student
        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long studentId = appUser != null ? appUser.getId() : loggedStudent.getId();

        // Display name: prefer AppUser.name, else Student.fullName
        String studentName = (appUser != null && appUser.getName() != null && !appUser.getName().isBlank())
                ? appUser.getName()
                : (loggedStudent.getFullName() != null ? loggedStudent.getFullName() : "Unknown Student");

        // --- Step 3: Build allowed batch IDs from the student's active enrollments ---
        Set<Long> enrolledBatchIds = new HashSet<>();
        List<Enrollment> enrollments = enrollmentRepository.findByStudent_Id(loggedStudent.getId());
        for (Enrollment e : enrollments) {
            if (e.getEnrollmentStatus() == EnrollmentStatus.APPROVED
                    || e.getEnrollmentStatus() == EnrollmentStatus.PENDING) {
                if (e.getBatch() != null && e.getBatch().getBatchId() != null) {
                    enrolledBatchIds.add(e.getBatch().getBatchId());
                }
            }
        }

        Map<Long, String> subjectMap = subjectMap();
        Map<Long, String> batchMap = batchMap();

        // --- Step 4: Filter sessions to only those matching enrolled batches ---
        List<TimetableSession> sessions;
        if (!enrolledBatchIds.isEmpty()) {
            LocalDate today = LocalDate.now();
            YearMonth currentMonth = YearMonth.now();

            sessions = timetableService.getAllSessionsOldestFirst().stream()
                    .filter(s -> s.getBatchId() != null && enrolledBatchIds.contains(s.getBatchId()))
                    .filter(s -> s.getDate() != null &&
                            YearMonth.from(s.getDate()).equals(currentMonth) &&
                            !s.getDate().isAfter(today))
                    .collect(Collectors.toList());
        } else {
            sessions = Collections.emptyList();
        }

        // Apply optional grade/subject UI filters on top
        if (grade != null && !grade.isBlank()) {
            sessions = sessions.stream()
                    .filter(session -> {
                        if (session.getGrade() == null) return false;
                        String storedGrade = String.valueOf(session.getGrade());
                        return storedGrade.equalsIgnoreCase(grade) 
                            || ("Grade " + storedGrade).equalsIgnoreCase(grade)
                            || grade.equalsIgnoreCase(storedGrade);
                    })
                    .collect(Collectors.toList());
        }
        if (subject != null && !subject.isBlank()) {
            sessions = sessions.stream()
                    .filter(session -> {
                        String subjectName = subjectMap.get(session.getSubjectId());
                        return subjectName != null && subjectName.equalsIgnoreCase(subject);
                    })
                    .collect(Collectors.toList());
        }

        List<String> grades = courseRepository.findDistinctGrades();
        if (grades.isEmpty()) grades = Arrays.asList("Grade 10", "Grade 11");

        List<String> subjects = courseRepository.findDistinctSubjects();
        if (subjects.isEmpty()) subjects = Arrays.asList("Mathematics", "Science");

        model.addAttribute("studentId", studentId);
        model.addAttribute("student", appUser);
        model.addAttribute("studentName", studentName);
        model.addAttribute("sessions", sessions);
        model.addAttribute("subjectMap", subjectMap);
        model.addAttribute("batchMap", batchMap);
        model.addAttribute("grades", grades);
        model.addAttribute("subjects", subjects);
        model.addAttribute("selectedGrade", grade);
        model.addAttribute("selectedSubject", subject);

        return "attendance-student-select";
    }

    @GetMapping("/students")
    public String showStudentsForSession(@RequestParam("sessionId") Long sessionId, Model model) {
        TimetableSession selectedSession = timetableService.getById(sessionId);

        List<AppUser> students = userRepository.findByRole(Role.STUDENT);

        model.addAttribute("selectedSession", selectedSession);
        model.addAttribute("students", students);
        model.addAttribute("subjectMap", subjectMap());
        model.addAttribute("batchMap", batchMap());

        return "student-attendance-students";
    }

    @GetMapping("/view")
    public String showStudentAttendance(@RequestParam(value = "studentId", required = false) Long studentIdFromReq,
            @RequestParam("sessionId") Long sessionId,
            HttpSession httpSession,
            Model model) {

        // Resolve logged-in student from session (studentUsername → Student → AppUser)
        String sessionUsername = (String) httpSession.getAttribute("studentUsername");

        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) return "redirect:/student/login";

        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long loggedId = appUser != null ? appUser.getId() : loggedStudent.getId();

        // Always use the logged-in student's ID (ignore any studentId from URL to prevent viewing others)
        Long studentId = loggedId;

        // Display name
        String studentName = (appUser != null && appUser.getName() != null && !appUser.getName().isBlank())
                ? appUser.getName()
                : (loggedStudent.getFullName() != null ? loggedStudent.getFullName() : "Unknown Student");

        TimetableSession selectedSession = timetableService.getById(sessionId);

        List<AttendanceRecord> records = attendanceService.getForStudentAndSessionAndDate(
                studentId,
                sessionId,
                selectedSession.getDate());

        Map<Long, TimetableSession> sessionMap = new LinkedHashMap<>();
        for (AttendanceRecord record : records) {
            if (record.getSessionId() == null) continue;
            if (!sessionMap.containsKey(record.getSessionId())) {
                TimetableSession s = safeGetSession(record.getSessionId());
                if (s != null) sessionMap.put(record.getSessionId(), s);
            }
        }

        long unreadNotificationCount = absenceNotificationService.getUnreadNotificationCountForSessionAndDate(
                studentId, sessionId, selectedSession.getDate());
        boolean isAbsent = records.stream().anyMatch(r -> r.getStatus() == AttendanceStatus.ABSENT);
        long totalNotificationCount = absenceNotificationService
                .getNotificationsForStudentAndSessionAndDate(studentId, sessionId, selectedSession.getDate()).size();

        model.addAttribute("student", appUser);
        model.addAttribute("studentName", studentName);
        model.addAttribute("records", records);
        model.addAttribute("sessionMap", sessionMap);
        model.addAttribute("selectedSession", selectedSession);
        model.addAttribute("subjectMap", subjectMap());
        model.addAttribute("batchMap", batchMap());
        model.addAttribute("unreadNotificationCount", unreadNotificationCount);
        model.addAttribute("totalNotificationCount", totalNotificationCount);
        model.addAttribute("isAbsent", isAbsent);

        return "student-attendance";
    }

    @GetMapping("/monthly-summary")
    public String showStudentMonthlySummary(@RequestParam(value = "studentId", required = false) Long studentIdFromReq,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "month", required = false) String monthStr,
            HttpSession httpSession,
            Model model) {

        // Resolve logged-in student from session
        String sessionUsername = (String) httpSession.getAttribute("studentUsername");
        if (sessionUsername == null) return "redirect:/student/login";
        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) return "redirect:/student/login";

        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long studentId = appUser != null ? appUser.getId() : loggedStudent.getId();

        YearMonth selectedMonth;
        if (monthStr == null || monthStr.isBlank()) {
            selectedMonth = YearMonth.now();
        } else {
            selectedMonth = YearMonth.parse(monthStr);
        }

        StudentMonthlyAttendanceSummary summary = attendanceService.getStudentMonthlySummary(studentId, selectedMonth);

        Map<Long, TimetableSession> sessionMap = new LinkedHashMap<>();
        for (AttendanceRecord record : summary.getRecords()) {
            if (record.getSessionId() == null) continue;
            if (!sessionMap.containsKey(record.getSessionId())) {
                TimetableSession s = safeGetSession(record.getSessionId());
                if (s != null) sessionMap.put(record.getSessionId(), s);
            }
        }

        model.addAttribute("summary", summary);
        model.addAttribute("studentId", studentId);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("selectedMonth", selectedMonth.toString());
        model.addAttribute("monthLabel", selectedMonth.getMonth().name() + " " + selectedMonth.getYear());
        model.addAttribute("sessionMap", sessionMap);
        model.addAttribute("subjectMap", subjectMap());
        model.addAttribute("batchMap", batchMap());

        return "student-monthly-attendance-summary";
    }

    @GetMapping("/notifications")
    public String showStudentNotifications(@RequestParam(value = "studentId", required = false) Long studentIdFromReq,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            HttpSession httpSession,
            Model model) {

        // Resolve logged-in student from session
        String sessionUsername = (String) httpSession.getAttribute("studentUsername");
        if (sessionUsername == null) return "redirect:/student/login";
        Student loggedStudent = studentRepository.findByUsernameIgnoreCase(sessionUsername).orElse(null);
        if (loggedStudent == null) return "redirect:/student/login";

        AppUser appUser = userRepository.findByEmailIgnoreCase(loggedStudent.getEmail()).orElse(null);
        Long studentId = appUser != null ? appUser.getId() : loggedStudent.getId();

        // Display name
        String studentName = (appUser != null && appUser.getName() != null && !appUser.getName().isBlank())
                ? appUser.getName()
                : (loggedStudent.getFullName() != null ? loggedStudent.getFullName() : "Unknown Student");

        TimetableSession timetableSession = sessionId != null ? timetableService.getById(sessionId) : null;
        List<AbsenceNotification> notifications;

        if (timetableSession != null && timetableSession.getDate() != null) {
            notifications = absenceNotificationService.getNotificationsForStudentAndSessionAndDate(
                    studentId, sessionId, timetableSession.getDate());
            absenceNotificationService.markAsReadForSessionAndDate(studentId, sessionId, timetableSession.getDate());
        } else {
            notifications = absenceNotificationService.getNotificationsForStudent(studentId);
            absenceNotificationService.markAllAsRead(studentId);
        }

        // Build a sessionMap so the template can look up session details per notification
        Map<Long, TimetableSession> sessionMap = new LinkedHashMap<>();
        for (AbsenceNotification n : notifications) {
            if (n.getSessionId() != null && !sessionMap.containsKey(n.getSessionId())) {
                TimetableSession s = safeGetSession(n.getSessionId());
                if (s != null) sessionMap.put(n.getSessionId(), s);
            }
        }

        model.addAttribute("student", appUser);
        model.addAttribute("studentId", studentId);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("studentName", studentName);
        model.addAttribute("notifications", notifications);
        model.addAttribute("sessionMap", sessionMap);
        model.addAttribute("subjectMap", subjectMap());
        model.addAttribute("batchMap", batchMap());

        return "student-absence-notifications";
    }
}
