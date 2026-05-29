package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByBatch_BatchIdOrderByUploadedDateDesc(Long batchId);

    List<Material> findByBatch_BatchIdAndMonthAndYearOrderByUploadedDateDesc(Long batchId, int month, int year);

    List<Material> findByBatch_BatchIdAndMonthAndYear(Long batchId, int month, int year);

    List<Material> findByBatch_Course_CourseIdAndMonthAndYear(String courseId, int month, int year);

    @Transactional
    void deleteByBatch_BatchId(Long batchId);
}
