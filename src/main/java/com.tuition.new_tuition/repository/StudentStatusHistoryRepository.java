package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    List<StudentStatusHistory> findByStudentIdOrderByChangedAtDesc(Long studentId);

    @Transactional
    void deleteByStudentId(Long studentId);
}