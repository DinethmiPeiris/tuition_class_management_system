package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    boolean existsByBatchName(String batchName);
    java.util.Optional<Batch> findByBatchName(String batchName);
    java.util.Optional<Batch> findFirstByBatchName(String batchName);

    Optional<Batch> findFirstByCourse_CourseIdAndStatusOrderByBatchIdAsc(String courseId, String status);

    List<Batch> findAllByOrderByBatchIdAsc();

    List<Batch> findAllByOrderByBatchIdDesc();

    boolean existsByCourse_CourseIdAndBatchNameIgnoreCaseAndYear(String courseId, String batchName, int year);

    boolean existsByCourse_CourseIdAndBatchNameIgnoreCaseAndYearAndBatchIdNot(String courseId, String batchName, int year, Long batchId);

    List<Batch> findByCourse_CourseIdAndStatusOrderByBatchNameAsc(String courseId, String status);
}
