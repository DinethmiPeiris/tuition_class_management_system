package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.*;
import com.tuition.new_tuition.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherApprovalService {

    private final RegistrationRequestRepository requestRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public TeacherApprovalService(RegistrationRequestRepository requestRepository,
                                  StudentRepository studentRepository,
                                  UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    public List<RegistrationRequest> getPendingRequests() {
        return requestRepository.findByStatus(RegistrationStatus.PENDING);
    }

    public void approveRequest(Long requestId) {
        RegistrationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != RegistrationStatus.PENDING) {
            throw new RuntimeException("Request already reviewed");
        }

        // Create Student
        Student student = new Student();
        student.setFullName(req.getFullName());
        student.setEmail(req.getEmail());
        student.setUsername(req.getUsername());
        student.setPasswordHash(req.getPasswordHash());
        student.setPhone(req.getPhone());
        student.setAddress(req.getAddress());
        student.setTeacher(req.getTeacher());
        student.setStatus(StudentStatus.ACTIVE);

        studentRepository.save(student);

        // Generate Student Code
        String studentCode = "STU" + String.format("%04d", student.getId());
        student.setStudentCode(studentCode);
        studentRepository.save(student);

        // Create matching AppUser if not already present (required for attendance roster)
        userRepository.findByEmailIgnoreCase(req.getEmail()).orElseGet(() -> {
            AppUser appUser = new AppUser();
            appUser.setEmail(req.getEmail());
            appUser.setName(req.getFullName());
            appUser.setPassword(req.getPasswordHash());
            appUser.setRole(Role.STUDENT);
            return userRepository.save(appUser);
        });

        // Update request
        req.setStatus(RegistrationStatus.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        req.setCreatedStudent(student);

        requestRepository.save(req);
    }

    public void rejectRequest(Long requestId, String reason) {
        RegistrationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != RegistrationStatus.PENDING) {
            throw new RuntimeException("Request already reviewed");
        }

        req.setStatus(RegistrationStatus.REJECTED);
        req.setRejectionReason(reason);
        req.setReviewedAt(LocalDateTime.now());

        requestRepository.save(req);
    }
}