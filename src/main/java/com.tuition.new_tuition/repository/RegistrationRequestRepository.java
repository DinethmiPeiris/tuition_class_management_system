package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.RegistrationRequest;
import com.tuition.new_tuition.entity.RegistrationStatus;
import com.tuition.new_tuition.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    Optional<RegistrationRequest> findByUsername(String username);
    Optional<RegistrationRequest> findByUsernameIgnoreCase(String username);
    Optional<RegistrationRequest> findByEmail(String email);
    List<RegistrationRequest> findByStatus(RegistrationStatus status);
    
    @Transactional
    void deleteByCreatedStudent(Student student);
}