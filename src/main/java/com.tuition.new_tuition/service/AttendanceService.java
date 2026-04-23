package com.tuition.new_tuition.service;

import com.tuition.new_tuition.dto.MonthlyAttendanceSummaryRow;
import com.tuition.new_tuition.dto.StudentMonthlyAttendanceSummary;
import com.tuition.new_tuition.entity.AttendanceRecord;
import com.tuition.new_tuition.entity.AttendanceStatus;
import com.tuition.new_tuition.entity.AppUser;
import com.tuition.new_tuition.entity.TimetableSession;
import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.repository.AttendanceRecordRepository;
import com.tuition.new_tuition.repository.BatchRepository;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.repository.StudentRepository;
import com.tuition.new_tuition.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final TimetableService timetableService;
    private final BatchRepository batchRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository,
            UserRepository userRepository,
            TimetableService timetableService,
            BatchRepository batchRepository,
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.userRepository = userRepository;
        this.timetableService = timetableService;
        this.batchRepository = batchRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
    }

    public List<AttendanceRecord> getExisting(Long sessionId, LocalDate date) {
        return attendanceRecordRepository.findBySessionIdAndDate(sessionId, date);
    }

    public List<AttendanceRecord> getForStudent(Long studentId) {
        return attendanceRecordRepository.findByStudentIdOrderByDateDesc(studentId);
    }

    public List<AttendanceRecord> getForStudentAndSession(Long studentId, Long sessionId) {
        return attendanceRecordRepository.findByStudentIdAndSessionIdOrderByDateDesc(studentId, sessionId);
    }

    public List<AttendanceRecord> getForStudentAndSessionAndDate(Long studentId, Long sessionId, LocalDate date) {
        return attendanceRecordRepository.findByStudentIdAndSessionIdAndDateOrderByDateDesc(studentId, sessionId, date);
    }

    public void saveOne(Long teacherId,
            Long sessionId,
            Long studentId,
            LocalDate date,
            AttendanceStatus status) {

        AttendanceRecord record = attendanceRecordRepository
                .findBySessionIdAndStudentIdAndDate(sessionId, studentId, date)
                .orElseGet(AttendanceRecord::new);

        record.setTeacherId(teacherId);
        record.setSessionId(sessionId);
        record.setStudentId(studentId);
        record.setDate(date);
        record.setMarkedAt(LocalDateTime.now());
        record.setStatus(status);

        attendanceRecordRepository.save(record);
    }

    public boolean isAttendanceMarked(Long sessionId, LocalDate date) {
        List<AttendanceRecord> records = attendanceRecordRepository.findBySessionIdAndDate(sessionId, date);
        return records != null && !records.isEmpty();
    }

    private TimetableSession safeGetSession(Long sessionId) {
        try {
            return timetableService.getById(sessionId);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public List<MonthlyAttendanceSummaryRow> getMonthlySummary(YearMonth month,
            String grade,
            String subject,
            Long batchId) {

        List<MonthlyAttendanceSummaryRow> summaryRows = new ArrayList<>();

        if (month == null || grade == null || grade.isBlank() || subject == null || subject.isBlank()) {
            return summaryRows;
        }

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<AttendanceRecord> allRecords = attendanceRecordRepository.findByDateBetweenOrderByDateAsc(
                startDate,
                endDate);

        Map<Long, MonthlyAttendanceSummaryRow> summaryMap = new LinkedHashMap<>();

        // 1. Pre-populate map with students enrolled in this Grade/Subject (or specific Batch)
        List<EnrollmentStatus> activeStatuses = List.of(EnrollmentStatus.APPROVED, EnrollmentStatus.PENDING);
        List<Enrollment> enrollments;
        
        if (batchId != null) {
            // Filter strictly by the selected Batch
            enrollments = enrollmentRepository.findByBatch_BatchIdAndEnrollmentStatusIn(batchId, activeStatuses);
        } else {
            // Filter by Grade and Subject across all batches
            enrollments = enrollmentRepository.findByBatch_Course_GradeAndBatch_Course_SubjectAndEnrollmentStatusIn(
                    grade, subject, activeStatuses);
            
            // Fallback for "Grade 10" vs "10" naming inconsistency
            if (enrollments.isEmpty()) {
                String altGrade = grade.startsWith("Grade ") ? grade.replace("Grade ", "").trim() : "Grade " + grade;
                enrollments = enrollmentRepository.findByBatch_Course_GradeAndBatch_Course_SubjectAndEnrollmentStatusIn(
                        altGrade, subject, activeStatuses);
            }
        }

        for (Enrollment enrollment : enrollments) {
            com.tuition.new_tuition.entity.Student eStudent = enrollment.getStudent();
            if (eStudent != null) {
                // Find AppUser to get the correct studentId used in AttendanceRecords
                userRepository.findByEmailIgnoreCase(eStudent.getEmail()).ifPresent(appUser -> {
                    if (!summaryMap.containsKey(appUser.getId())) {
                        MonthlyAttendanceSummaryRow row = new MonthlyAttendanceSummaryRow();
                        row.setStudentId(appUser.getId());
                        row.setStudentName(appUser.getName());
                        row.setGrade(parseGradeToInt(grade)); 
                        row.setBatch(enrollment.getBatch() != null ? enrollment.getBatch().getBatchName() : "Unknown");
                        row.setSubject(subject);
                        row.setTotalClasses(0);
                        row.setPresentCount(0);
                        row.setAbsentCount(0);
                        row.setAttendancePercentage(0.0);
                        summaryMap.put(appUser.getId(), row);
                    }
                });
            }
        }

        // 2. Fetch and aggregate actual Attendance records
        for (AttendanceRecord record : allRecords) {
            if (record.getStudentId() == null || record.getSessionId() == null) {
                continue;
            }

            // Check if this student belongs to our current filtered list (enrolments)
            MonthlyAttendanceSummaryRow row = summaryMap.get(record.getStudentId());
            if (row == null) {
                continue; // Only show students based on the enrolment filter
            }

            TimetableSession session = safeGetSession(record.getSessionId());
            if (session == null) {
                continue;
            }

            // Even if student is enrolled, only count sessions for the specific batch if selected
            if (batchId != null) {
                if (!batchId.equals(session.getBatchId())) continue;
            }

            // Aggregate data
            row.setTotalClasses(row.getTotalClasses() + 1);
            if (record.getStatus() == AttendanceStatus.PRESENT) {
                row.setPresentCount(row.getPresentCount() + 1);
            } else if (record.getStatus() == AttendanceStatus.ABSENT) {
                row.setAbsentCount(row.getAbsentCount() + 1);
            }
        }

        summaryRows = new ArrayList<>(summaryMap.values());

        for (MonthlyAttendanceSummaryRow row : summaryRows) {
            if (row.getTotalClasses() > 0) {
                double percentage = ((double) row.getPresentCount() / row.getTotalClasses()) * 100.0;
                row.setAttendancePercentage(percentage);
            } else {
                row.setAttendancePercentage(0.0);
            }
        }

        summaryRows.sort(Comparator.comparing(
                MonthlyAttendanceSummaryRow::getStudentName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        return summaryRows;
    }

    private Integer parseGradeToInt(String gradeStr) {
        if (gradeStr == null) return null;
        try {
            String cleaned = gradeStr.replace("Grade ", "").trim();
            return Integer.valueOf(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    public StudentMonthlyAttendanceSummary getStudentMonthlySummary(Long studentId, YearMonth month) {
        StudentMonthlyAttendanceSummary summary = new StudentMonthlyAttendanceSummary();

        if (studentId == null || month == null) {
            summary.setRecords(new ArrayList<>());
            summary.setTotalClasses(0);
            summary.setPresentCount(0);
            summary.setAbsentCount(0);
            summary.setAttendancePercentage(0.0);
            return summary;
        }

        AppUser student = userRepository.findById(studentId).orElse(null);
        summary.setStudent(student);

        if (student != null) {
            summary.setStudentName(student.getName());
        } else {
            summary.setStudentName("No Student");
        }

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        // 1. Resolve enrolled batch IDs for the student to ensure summary only shows relevant schedules
        java.util.Set<Long> enrolledBatchIds = new java.util.HashSet<>();
        com.tuition.new_tuition.entity.Student sEntity = null;
        if (student != null) {
            sEntity = studentRepository.findByEmail(student.getEmail()).orElse(null);
        } else {
            sEntity = studentRepository.findById(studentId).orElse(null);
        }

        if (sEntity != null) {
            for (Enrollment e : sEntity.getEnrollments()) {
                if (e.getEnrollmentStatus() == EnrollmentStatus.APPROVED || e.getEnrollmentStatus() == EnrollmentStatus.PENDING) {
                    if (e.getBatch() != null && e.getBatch().getBatchId() != null) {
                        enrolledBatchIds.add(e.getBatch().getBatchId());
                    }
                }
            }
        }

        List<AttendanceRecord> rawRecords = attendanceRecordRepository.findByStudentIdAndDateBetweenOrderByDateAsc(
                studentId,
                startDate,
                endDate);

        List<AttendanceRecord> validRecords = new ArrayList<>();

        for (AttendanceRecord record : rawRecords) {
            if (record.getSessionId() == null) {
                continue;
            }

            TimetableSession session = safeGetSession(record.getSessionId());
            if (session == null) {
                continue;
            }

            // Filter: Only include record if its session belongs to one of the student's enrolled batches
            if (!enrolledBatchIds.isEmpty() && !enrolledBatchIds.contains(session.getBatchId())) {
                continue;
            }

            validRecords.add(record);
        }

        summary.setRecords(validRecords);
        summary.setTotalClasses(validRecords.size());

        int presentCount = 0;
        int absentCount = 0;

        for (AttendanceRecord record : validRecords) {
            if (record.getStatus() == AttendanceStatus.PRESENT) {
                presentCount++;
            } else if (record.getStatus() == AttendanceStatus.ABSENT) {
                absentCount++;
            }
        }

        summary.setPresentCount(presentCount);
        summary.setAbsentCount(absentCount);

        if (summary.getTotalClasses() > 0) {
            double percentage = ((double) presentCount / summary.getTotalClasses()) * 100.0;
            summary.setAttendancePercentage(percentage);
        } else {
            summary.setAttendancePercentage(0.0);
        }

        return summary;
    }
}
