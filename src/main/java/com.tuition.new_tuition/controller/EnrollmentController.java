package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.entity.Batch;
import com.tuition.new_tuition.entity.Enrollment;
import com.tuition.new_tuition.entity.EnrollmentStatus;
import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.repository.BatchRepository;
import com.tuition.new_tuition.repository.EnrollmentRepository;
import com.tuition.new_tuition.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;

    public EnrollmentController(EnrollmentRepository enrollmentRepository,
                                StudentRepository studentRepository,
                                BatchRepository batchRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.batchRepository = batchRepository;
    }

    // Student sends enrollment request
    @PostMapping("/request")
    public Enrollment requestEnrollment(@RequestParam Long studentId,
                                        @RequestParam Long batchId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setBatch(batch);
        enrollment.setRequestDate(LocalDate.now());
        enrollment.setEnrollmentStatus(EnrollmentStatus.PENDING);

        return enrollmentRepository.save(enrollment);
    }

    // Teacher views all pending enrollments
    @GetMapping("/pending")
    public List<Enrollment> getPendingEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .filter(e -> e.getEnrollmentStatus() == EnrollmentStatus.PENDING)
                .collect(Collectors.toList());
    }

    // Teacher approves or rejects
    @PutMapping("/{enrollmentId}/status")
    public Enrollment updateEnrollmentStatus(@PathVariable Long enrollmentId,
                                             @RequestParam EnrollmentStatus status) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setEnrollmentStatus(status);
        return enrollmentRepository.save(enrollment);
    }

    // Get all approved students for a batch
    @GetMapping("/batch/{batchId}/students")
    public List<Map<String, String>> getBatchStudents(@PathVariable Long batchId) {
        return enrollmentRepository.findByBatch_BatchIdAndEnrollmentStatus(batchId, EnrollmentStatus.APPROVED)
                .stream()
                .map(e -> {
                    Map<String, String> studentInfo = new HashMap<>();
                    studentInfo.put("fullName", e.getStudent().getFullName());
                    studentInfo.put("studentCode", e.getStudent().getStudentCode());
                    return studentInfo;
                })
                .collect(Collectors.toList());
    }
}
