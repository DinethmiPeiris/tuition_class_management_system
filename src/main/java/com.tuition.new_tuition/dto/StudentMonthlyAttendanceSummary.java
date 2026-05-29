package com.tuition.new_tuition.dto;

import com.tuition.new_tuition.entity.AttendanceRecord;
import com.tuition.new_tuition.entity.AppUser;

import java.util.ArrayList;
import java.util.List;

public class StudentMonthlyAttendanceSummary {

    private AppUser student;
    private String studentName;
    private int totalClasses;
    private int presentCount;
    private int absentCount;
    private double attendancePercentage;
    private List<AttendanceRecord> records = new ArrayList<>();

    public AppUser getStudent() {
        return student;
    }

    public void setStudent(AppUser student) {
        this.student = student;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public List<AttendanceRecord> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecord> records) {
        this.records = records;
    }
}
