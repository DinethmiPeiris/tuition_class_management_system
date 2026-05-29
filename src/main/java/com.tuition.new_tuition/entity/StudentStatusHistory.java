package com.tuition.new_tuition.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="student_status_history")
public class StudentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="student_id", nullable=false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StudentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StudentStatus newStatus;

    @Column(nullable=false)
    private LocalDateTime changedAt = LocalDateTime.now();

    private String note;

    public StudentStatusHistory() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public StudentStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(StudentStatus oldStatus) { this.oldStatus = oldStatus; }

    public StudentStatus getNewStatus() { return newStatus; }
    public void setNewStatus(StudentStatus newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}