package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findBySessionIdAndDate(Long sessionId, LocalDate date);

    Optional<AttendanceRecord> findBySessionIdAndStudentIdAndDate(Long sessionId, Long studentId, LocalDate date);

    List<AttendanceRecord> findByStudentIdOrderByDateDesc(Long studentId);

    List<AttendanceRecord> findByStudentIdAndSessionIdOrderByDateDesc(Long studentId, Long sessionId);

    List<AttendanceRecord> findByStudentIdAndSessionIdAndDateOrderByDateDesc(Long studentId, Long sessionId,
            LocalDate date);

    List<AttendanceRecord> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

    List<AttendanceRecord> findByTeacherIdAndDateBetweenOrderByDateAsc(Long teacherId,
            LocalDate startDate,
            LocalDate endDate);

    List<AttendanceRecord> findByStudentIdAndDateBetweenOrderByDateAsc(Long studentId,
            LocalDate startDate,
            LocalDate endDate);

    @Transactional
    void deleteByStudentId(Long studentId);
}
