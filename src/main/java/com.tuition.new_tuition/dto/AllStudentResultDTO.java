package com.tuition.new_tuition.dto;

public class AllStudentResultDTO {

    private String studentName;
    private String studentEmail;
    private String examName;
    private String subject;
    private String batchName;
    private String examDate;
    private int score;
    private int totalMarks;
    private double percentage;
    private String status;

    public AllStudentResultDTO(String studentName, String studentEmail,
                               String examName, String subject, String batchName,
                               String examDate, int score, int totalMarks,
                               double percentage, String status) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.examName = examName;
        this.subject = subject;
        this.batchName = batchName;
        this.examDate = examDate;
        this.score = score;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.status = status;
    }

    public String getStudentName()  { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getExamName()     { return examName; }
    public String getSubject()      { return subject; }
    public String getBatchName()    { return batchName; }
    public String getExamDate()     { return examDate; }
    public int getScore()           { return score; }
    public int getTotalMarks()      { return totalMarks; }
    public double getPercentage()   { return percentage; }
    public String getStatus()       { return status; }
}
