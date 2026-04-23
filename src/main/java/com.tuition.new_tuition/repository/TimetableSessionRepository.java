package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.TimetableSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface TimetableSessionRepository extends JpaRepository<TimetableSession, Long> {

    List<TimetableSession> findByTeacherId(Long teacherId);

    List<TimetableSession> findByTeacherIdAndDate(Long teacherId, LocalDate date);

    List<TimetableSession> findByDate(LocalDate date);

    // Used by student timetable: sessions for a set of batchIds
    List<TimetableSession> findByBatchIdIn(Collection<Long> batchIds);

    // Used by student timetable: sessions for a set of batchIds AND a specific grade
    List<TimetableSession> findByBatchIdInAndGrade(Collection<Long> batchIds, Integer grade);
}
