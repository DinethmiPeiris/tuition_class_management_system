package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.RegistrationRequest;
import com.tuition.new_tuition.entity.RegistrationStatus;
import com.tuition.new_tuition.entity.Teacher;
import com.tuition.new_tuition.repository.RegistrationRequestRepository;
import com.tuition.new_tuition.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegistrationService {

    private final RegistrationRequestRepository requestRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordService passwordService;

    public RegistrationService(RegistrationRequestRepository requestRepository,
                               TeacherRepository teacherRepository,
                               PasswordService passwordService) {
        this.requestRepository = requestRepository;
        this.teacherRepository = teacherRepository;
        this.passwordService = passwordService;
    }

    public Optional<String> registerStudent(String fullName,
                                            String email,
                                            String username,
                                            String rawPassword,
                                            String phone,
                                            String address) {

        // basic duplicate checks
        if (requestRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return Optional.of("Username already exists. Please use another username.");
        }
        if (requestRepository.findByEmail(email).isPresent()) {
            return Optional.of("Email already exists. Please use another email.");
        }

        // single teacher (seeded) - username "teacher"
        Teacher teacher = teacherRepository.findByUsername("teacher")
                .orElseThrow(() -> new RuntimeException("Teacher account not found. Seed failed."));

        RegistrationRequest req = new RegistrationRequest();
        req.setFullName(fullName);
        req.setEmail(email);
        req.setUsername(username);
        req.setPasswordHash(passwordService.hash(rawPassword));
        req.setPhone(phone);
        req.setAddress(address);
        req.setStatus(RegistrationStatus.PENDING);
        req.setTeacher(teacher);

        requestRepository.save(req);
        return Optional.empty(); // no error
    }

    public RegistrationRequest getRequestByUsername(String username) {
        return requestRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));
    }
}