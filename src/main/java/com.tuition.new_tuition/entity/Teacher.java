package com.tuition.new_tuition.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String username;

    @Column(nullable=false)
    private String passwordHash;

    @Column(nullable=false)
    private String fullName;

    @OneToMany(mappedBy = "teacher")
    private List<RegistrationRequest> requests = new ArrayList<>();

    @OneToMany(mappedBy = "teacher")
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "teacher")
    private List<TeacherFeedback> feedbacks = new ArrayList<>();

    public Teacher() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<RegistrationRequest> getRequests() { return requests; }
    public void setRequests(List<RegistrationRequest> requests) { this.requests = requests; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }

    public List<TeacherFeedback> getFeedbacks() { return feedbacks; }
    public void setFeedbacks(List<TeacherFeedback> feedbacks) { this.feedbacks = feedbacks; }
}