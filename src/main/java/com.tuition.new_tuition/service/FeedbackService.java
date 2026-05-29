package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.*;
import com.tuition.new_tuition.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    private final TeacherFeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public FeedbackService(TeacherFeedbackRepository feedbackRepository,
                           StudentRepository studentRepository,
                           TeacherRepository teacherRepository) {
        this.feedbackRepository = feedbackRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public void submitFeedback(String username, int rating, String comment, boolean anonymous) {

        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Teacher teacher = student.getTeacher();

        TeacherFeedback feedback = new TeacherFeedback();
        feedback.setStudent(student);
        feedback.setTeacher(teacher);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setAnonymous(anonymous);

        feedbackRepository.save(feedback);
    }

    public List<TeacherFeedback> getFeedbackForTeacher(Long teacherId) {
        return feedbackRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    public List<TeacherFeedback> getFeedbackByStudent(Long studentId) {
        return feedbackRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }
}