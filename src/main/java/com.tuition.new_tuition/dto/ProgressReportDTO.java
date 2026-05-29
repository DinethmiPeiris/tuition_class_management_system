package com.tuition.new_tuition.dto;

public class ProgressReportDTO {

    private String subject;
    private String batchName;
    private int studentCount;
    private int totalExams;
    private double averagePercentage;
    private long passCount;
    private long failCount;
    private long pendingCount;

    public ProgressReportDTO(String subject,
                             String batchName,
                             int studentCount,
                             int totalExams,
                             double averagePercentage,
                             long passCount,
                             long failCount,
                             long pendingCount) {
        this.subject = subject;
        this.batchName = batchName;
        this.studentCount = studentCount;
        this.totalExams = totalExams;
        this.averagePercentage = averagePercentage;
        this.passCount = passCount;
        this.failCount = failCount;
        this.pendingCount = pendingCount;
    }

    public String getSubject()           { return subject; }
    public String getBatchName()         { return batchName; }
    public int getStudentCount()         { return studentCount; }
    public int getTotalExams()           { return totalExams; }
    public double getAveragePercentage() { return averagePercentage; }
    public long getPassCount()           { return passCount; }
    public long getFailCount()           { return failCount; }
    public long getPendingCount()        { return pendingCount; }
}
