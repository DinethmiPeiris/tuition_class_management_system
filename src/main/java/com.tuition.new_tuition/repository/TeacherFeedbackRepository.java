package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.TeacherFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TeacherFeedbackRepository extends JpaRepository<TeacherFeedback, Long> {
    List<TeacherFeedback> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    List<TeacherFeedback> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Transactional
    void deleteByStudentId(Long studentId);
}