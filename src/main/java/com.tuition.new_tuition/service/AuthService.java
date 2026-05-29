package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.RegistrationRequest;
import com.tuition.new_tuition.entity.Student;
import com.tuition.new_tuition.entity.Teacher;
import com.tuition.new_tuition.repository.RegistrationRequestRepository;
import com.tuition.new_tuition.repository.StudentRepository;
import com.tuition.new_tuition.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final TeacherRepository teacherRepository;
    private final RegistrationRequestRepository requestRepository;
    private final StudentRepository studentRepository;
    private final PasswordService passwordService;

    public AuthService(TeacherRepository teacherRepository,
                       RegistrationRequestRepository requestRepository,
                       StudentRepository studentRepository,
                       PasswordService passwordService) {
        this.teacherRepository = teacherRepository;
        this.requestRepository = requestRepository;
        this.studentRepository = studentRepository;
        this.passwordService = passwordService;
    }

    public Optional<Teacher> loginTeacher(String username, String password) {
        return teacherRepository.findByUsernameIgnoreCase(username)
                .filter(t -> passwordService.matches(password, t.getPasswordHash()));
    }

    /**
     * Student can login even when PENDING/REJECTED (to see status).
     * If APPROVED, they may also exist in Student table.
     */
    public Optional<RegistrationRequest> loginStudentRequest(String username, String password) {
        return requestRepository.findByUsernameIgnoreCase(username)
                .filter(r -> passwordService.matches(password, r.getPasswordHash()));
    }

    public Optional<Student> findApprovedStudentByUsername(String username) {
        return studentRepository.findByUsername(username);
    }
}