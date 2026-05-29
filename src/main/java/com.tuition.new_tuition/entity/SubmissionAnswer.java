package com.tuition.new_tuition.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_answers")
public class SubmissionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private ExamSubmission submission;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // stores MCQ answer (A/B/C/D) or essay text
    @Column(name = "answer_text", columnDefinition = "nvarchar(max)")
    private String answerText;

    // for MCQ auto marking or essay marking later
    private Integer awardedMarks;

    @Column(length = 1000)
    private String teacherFeedback;

    public Long getId() {
        return id;
    }

    public ExamSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(ExamSubmission submission) {
        this.submission = submission;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Integer getAwardedMarks() {
        return awardedMarks;
    }

    public void setAwardedMarks(Integer awardedMarks) {
        this.awardedMarks = awardedMarks;
    }

    public String getTeacherFeedback() {
        return teacherFeedback;
    }

    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
    }
}
