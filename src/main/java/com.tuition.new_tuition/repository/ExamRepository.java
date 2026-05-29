package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {
}
