package com.tuition.new_tuition.dto;

public class ProgressReportDTO {

    private String studentName;
    private String studentEmail;
    private String subject;
    private int totalExams;
    private double averagePercentage;
    private long passCount;
    private long failCount;
    private long pendingCount;

    public ProgressReportDTO(String studentName,
                             String studentEmail,
                             String subject,
                             int totalExams,
                             double averagePercentage,
                             long passCount,
                             long failCount,
                             long pendingCount) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.subject = subject;
        this.totalExams = totalExams;
        this.averagePercentage = averagePercentage;
        this.passCount = passCount;
        this.failCount = failCount;
        this.pendingCount = pendingCount;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getSubject() {
        return subject;
    }

    public int getTotalExams() {
        return totalExams;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public long getPassCount() {
        return passCount;
    }

    public long getFailCount() {
        return failCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }
}
