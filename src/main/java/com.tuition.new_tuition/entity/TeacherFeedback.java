package com.tuition.new_tuition.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="teacher_feedback")
public class TeacherFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="teacher_id", nullable=false)
    private Teacher teacher;

    @ManyToOne(optional=false)
    @JoinColumn(name="student_id", nullable=false)
    private Student student;

    @Column(nullable=false)
    private int rating; // 1..5

    @Column(length=1000)
    private String comment;

    @Column(nullable=false)
    private boolean anonymous = false;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TeacherFeedback() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}