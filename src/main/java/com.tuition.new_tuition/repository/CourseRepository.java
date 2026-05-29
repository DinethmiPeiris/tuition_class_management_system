package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
    @Query("SELECT DISTINCT c.grade FROM Course c")
    List<String> findDistinctGrades();

    @Query("SELECT DISTINCT c.subject FROM Course c")
    List<String> findDistinctSubjects();

    java.util.Optional<Course> findByGradeIgnoreCaseAndSubjectIgnoreCase(String grade, String subject);
}
