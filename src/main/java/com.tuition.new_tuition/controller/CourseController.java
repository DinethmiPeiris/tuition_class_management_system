package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Course;
import com.tuition.new_tuition.repository.CourseRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // Create Course
    @PostMapping("/create")
    public Course createCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }

    // View All Courses
    @GetMapping("/all")
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
}
