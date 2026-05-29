package com.tuition.new_tuition.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_requests")
public class RegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // login credentials
    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false, unique=true)
    private String username;

    @Column(nullable=false)
    private String passwordHash;

    // student details
    @Column(nullable=false)
    private String fullName;

    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    private String rejectionReason;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @OneToOne
    @JoinColumn(name = "student_id")
    private Student createdStudent;

    public RegistrationRequest() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public Student getCreatedStudent() { return createdStudent; }
    public void setCreatedStudent(Student createdStudent) { this.createdStudent = createdStudent; }
}