package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.AbsenceNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AbsenceNotificationRepository extends JpaRepository<AbsenceNotification, Long> {

    List<AbsenceNotification> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<AbsenceNotification> findByStudentIdAndReadFalseOrderByCreatedAtDesc(Long studentId);

    Optional<AbsenceNotification> findByStudentIdAndSessionIdAndAttendanceDate(Long studentId,
            Long sessionId,
            LocalDate attendanceDate);

    long countByStudentIdAndReadFalse(Long studentId);

    long countByStudentIdAndSessionIdAndAttendanceDateAndReadFalse(Long studentId, Long sessionId, LocalDate attendanceDate);

    List<AbsenceNotification> findByStudentIdAndSessionIdAndAttendanceDateOrderByCreatedAtDesc(Long studentId, Long sessionId, LocalDate attendanceDate);

    List<AbsenceNotification> findByStudentIdAndSessionIdAndAttendanceDateAndReadFalse(Long studentId, Long sessionId, LocalDate attendanceDate);

    void deleteByStudentIdAndSessionIdAndAttendanceDate(Long studentId,
            Long sessionId,
            LocalDate attendanceDate);
}
