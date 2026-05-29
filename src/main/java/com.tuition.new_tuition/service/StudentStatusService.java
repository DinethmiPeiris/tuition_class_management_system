package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.*;
import com.tuition.new_tuition.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentStatusService {

    private final StudentRepository studentRepository;
    private final StudentStatusHistoryRepository historyRepository;

    public StudentStatusService(StudentRepository studentRepository,
                                StudentStatusHistoryRepository historyRepository) {
        this.studentRepository = studentRepository;
        this.historyRepository = historyRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> getStudentsByStatus(StudentStatus status) {
        return studentRepository.findByStatus(status);
    }

    public void changeStatus(Long studentId, StudentStatus newStatus, String note) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentStatus oldStatus = student.getStatus();

        if (oldStatus == newStatus) return;

        // Update student
        student.setStatus(newStatus);
        studentRepository.save(student);

        // Save history
        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudent(student);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);
        history.setChangedAt(LocalDateTime.now());

        historyRepository.save(history);
    }

    public List<StudentStatusHistory> getHistory(Long studentId) {
        return historyRepository.findByStudentIdOrderByChangedAtDesc(studentId);
    }
}