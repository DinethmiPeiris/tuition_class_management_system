package com.tuition.new_tuition.dto;

import java.util.List;

public class StudentProgressReportDTO {

    private String studentName;
    private String studentEmail;
    private int totalExams;
    private double averagePercentage;
    private long passCount;
    private long failCount;
    private long pendingCount;
    private List<StudentReportItemDTO> examItems;

    public StudentProgressReportDTO(String studentName,
                                    String studentEmail,
                                    int totalExams,
                                    double averagePercentage,
                                    long passCount,
                                    long failCount,
                                    long pendingCount,
                                    List<StudentReportItemDTO> examItems) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.totalExams = totalExams;
        this.averagePercentage = averagePercentage;
        this.passCount = passCount;
        this.failCount = failCount;
        this.pendingCount = pendingCount;
        this.examItems = examItems;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
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

    public List<StudentReportItemDTO> getExamItems() {
        return examItems;
    }
}
