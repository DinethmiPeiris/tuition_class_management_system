package com.tuition.new_tuition.dto;

import com.tuition.new_tuition.entity.AttendanceStatus;

public class AttendanceMarkRow {

    private Long studentId;
    private String studentName;
    private String studentEmail;
    private AttendanceStatus status;

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
