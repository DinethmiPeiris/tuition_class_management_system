package com.tuition.new_tuition.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // generated only after approval
    @Column(unique=true)
    private String studentCode;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(unique=true)
    private String username;

    private String passwordHash;

    private String fullName;

    // Legacy column added to satisfy database NOT NULL constraint without dropping db columns
    private String name;

    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.ACTIVE;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name="teacher_id")
    private Teacher teacher;

    @OneToMany(mappedBy="student", cascade=CascadeType.ALL)
    private List<StudentStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy="student", cascade=CascadeType.ALL)
    private List<TeacherFeedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy="student", cascade=CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();

    public Student() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { 
        this.fullName = fullName; 
        this.name = fullName; 
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public List<StudentStatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StudentStatusHistory> statusHistory) { this.statusHistory = statusHistory; }

    public List<TeacherFeedback> getFeedbacks() { return feedbacks; }
    public void setFeedbacks(List<TeacherFeedback> feedbacks) { this.feedbacks = feedbacks; }

    public List<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }
}