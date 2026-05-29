package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudent_IdAndBatch_BatchId(Long studentId, Long batchId);
    
    java.util.Optional<Enrollment> findByStudent_IdAndBatch_BatchId(Long studentId, Long batchId);

    List<Enrollment> findByStudent_Id(Long studentId);

    List<Enrollment> findByStudent_Username(String username);
    List<Enrollment> findByStudent_UsernameIgnoreCase(String username);

    List<Enrollment> findByStudent_IdAndEnrollmentStatus(Long studentId, EnrollmentStatus enrollmentStatus);
    
    List<Enrollment> findByStudent_IdAndEnrollmentStatusIn(Long studentId, java.util.Collection<com.tuition.new_tuition.entity.EnrollmentStatus> statuses);

    List<Enrollment> findByEnrollmentStatusOrderByEnrollmentIdDesc(EnrollmentStatus enrollmentStatus);

    List<Enrollment> findByBatch_BatchIdAndEnrollmentStatus(Long batchId, EnrollmentStatus enrollmentStatus);

    List<Enrollment> findByBatch_BatchId(Long batchId);

    List<Enrollment> findByBatch_BatchIdAndEnrollmentStatusIn(
            Long batchId, java.util.Collection<com.tuition.new_tuition.entity.EnrollmentStatus> statuses);

    List<Enrollment> findByBatch_Course_GradeAndBatch_Course_SubjectAndEnrollmentStatusIn(
            String grade, String subject, java.util.Collection<com.tuition.new_tuition.entity.EnrollmentStatus> statuses);
}
