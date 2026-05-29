package com.tuition.new_tuition.dto;

public class StudentReportItemDTO {

    private String examName;
    private String subject;
    private String examDate;
    private int score;
    private int totalMarks;
    private double percentage;
    private String status;

    public StudentReportItemDTO(String examName,
                                String subject,
                                String examDate,
                                int score,
                                int totalMarks,
                                double percentage,
                                String status) {
        this.examName = examName;
        this.subject = subject;
        this.examDate = examDate;
        this.score = score;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.status = status;
    }

    public String getExamName() {
        return examName;
    }

    public String getSubject() {
        return subject;
    }

    public String getExamDate() {
        return examDate;
    }

    public int getScore() {
        return score;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getStatus() {
        return status;
    }
}
