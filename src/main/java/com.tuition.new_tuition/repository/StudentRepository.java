package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUsername(String username);
    Optional<Student> findByUsernameIgnoreCase(String username);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByStudentCode(String studentCode);
    List<Student> findByStatus(StudentStatus status);
    List<Student> findByTeacher_Id(Long teacherId);
}