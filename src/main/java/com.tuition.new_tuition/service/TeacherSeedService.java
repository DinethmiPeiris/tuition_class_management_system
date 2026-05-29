package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.*;
import com.tuition.new_tuition.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TeacherSeedService {

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentSlipRepository paymentSlipRepository;
    private final MaterialRepository materialRepository;
    private final PasswordService passwordService;

    public TeacherSeedService(TeacherRepository teacherRepository,
                             CourseRepository courseRepository,
                             BatchRepository batchRepository,
                             EnrollmentRepository enrollmentRepository,
                             PaymentSlipRepository paymentSlipRepository,
                             MaterialRepository materialRepository,
                             PasswordService passwordService) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.batchRepository = batchRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.paymentSlipRepository = paymentSlipRepository;
        this.materialRepository = materialRepository;
        this.passwordService = passwordService;
    }

    @PostConstruct
    @Transactional
    public void seedInitialData() {
        if (teacherRepository.count() == 0) {
            Teacher teacher = new Teacher();
            teacher.setFullName("Default Teacher");
            teacher.setUsername("teacher");
            teacher.setPasswordHash(passwordService.hash("teacher123"));
            teacherRepository.save(teacher);
            System.out.println("SEED: Base teacher created.");
        }
    }
}