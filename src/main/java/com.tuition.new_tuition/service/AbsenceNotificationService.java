package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.AbsenceNotification;
import com.tuition.new_tuition.entity.AppUser;
import com.tuition.new_tuition.entity.TimetableSession;
import com.tuition.new_tuition.repository.AbsenceNotificationRepository;
import com.tuition.new_tuition.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AbsenceNotificationService {

    private final AbsenceNotificationRepository absenceNotificationRepository;
    private final UserRepository userRepository;
    private final TimetableService timetableService;

    public AbsenceNotificationService(AbsenceNotificationRepository absenceNotificationRepository,
            UserRepository userRepository,
            TimetableService timetableService) {
        this.absenceNotificationRepository = absenceNotificationRepository;
        this.userRepository = userRepository;
        this.timetableService = timetableService;
    }

    public void createAbsenceNotification(Long studentId, Long sessionId, LocalDate attendanceDate) {
        if (studentId == null || sessionId == null || attendanceDate == null) {
            return;
        }

        boolean alreadyExists = absenceNotificationRepository
                .findByStudentIdAndSessionIdAndAttendanceDate(studentId, sessionId, attendanceDate)
                .isPresent();

        if (alreadyExists) {
            return;
        }

        AppUser student = userRepository.findById(studentId).orElse(null);
        TimetableSession session;
        try {
            session = timetableService.getById(sessionId);
        } catch (RuntimeException ex) {
            return;
        }

        if (student == null || session == null) {
            return;
        }

        String studentName = student.getName() != null && !student.getName().isBlank() 
                ? student.getName() 
                : "Unknown Student";

        String subjectName = getSubjectName(session.getSubjectId());

        String message = "Absent Notification: " + studentName
                + ", you were marked absent for "
                + subjectName
                + " class on "
                + attendanceDate
                + ".";

        AbsenceNotification notification = new AbsenceNotification();
        notification.setStudentId(studentId);
        notification.setSessionId(sessionId);
        notification.setAttendanceDate(attendanceDate);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        absenceNotificationRepository.save(notification);
    }

    @Transactional
    public void removeAbsenceNotification(Long studentId, Long sessionId, LocalDate attendanceDate) {
        if (studentId == null || sessionId == null || attendanceDate == null) {
            return;
        }

        absenceNotificationRepository.deleteByStudentIdAndSessionIdAndAttendanceDate(
                studentId,
                sessionId,
                attendanceDate);
    }

    public List<AbsenceNotification> getNotificationsForStudent(Long studentId) {
        return absenceNotificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public long getUnreadNotificationCount(Long studentId) {
        if (studentId == null) {
            return 0;
        }
        return absenceNotificationRepository.countByStudentIdAndReadFalse(studentId);
    }

    public long getUnreadNotificationCountForSessionAndDate(Long studentId, Long sessionId, LocalDate date) {
        if (studentId == null || sessionId == null || date == null) {
            return 0;
        }
        return absenceNotificationRepository.countByStudentIdAndSessionIdAndAttendanceDateAndReadFalse(studentId, sessionId, date);
    }

    public List<AbsenceNotification> getNotificationsForStudentAndSessionAndDate(Long studentId, Long sessionId, LocalDate date) {
        return absenceNotificationRepository.findByStudentIdAndSessionIdAndAttendanceDateOrderByCreatedAtDesc(studentId, sessionId, date);
    }

    public void markAllAsRead(Long studentId) {
        List<AbsenceNotification> notifications = absenceNotificationRepository
                .findByStudentIdAndReadFalseOrderByCreatedAtDesc(studentId);

        for (AbsenceNotification notification : notifications) {
            notification.setRead(true);
        }

        absenceNotificationRepository.saveAll(notifications);
    }

    public void markAsReadForSessionAndDate(Long studentId, Long sessionId, LocalDate date) {
        List<AbsenceNotification> notifications = absenceNotificationRepository
                .findByStudentIdAndSessionIdAndAttendanceDateAndReadFalse(studentId, sessionId, date);

        for (AbsenceNotification notification : notifications) {
            notification.setRead(true);
        }

        absenceNotificationRepository.saveAll(notifications);
    }

    private String getSubjectName(Long subjectId) {
        if (subjectId == null) {
            return "Unknown Subject";
        }

        if (subjectId.equals(1L)) {
            return "Mathematics";
        }

        if (subjectId.equals(2L)) {
            return "Science";
        }

        return "Unknown Subject";
    }
}
