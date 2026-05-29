package com.tuition.new_tuition.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimetableForm {

    private Long id;
    private Integer grade;
    private Long subjectId;
    private Long batchId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String classType;
    private Integer maxMembers;
    private String onlineLink;

    // Return-to filter params (used to redirect back to the filtered list after edit)
    private String returnGrade;
    private String returnSubject;
    private String returnBatchId;



    public TimetableForm() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public String getOnlineLink() {
        return onlineLink;
    }

    public void setOnlineLink(String onlineLink) {
        this.onlineLink = onlineLink;
    }

    public String getReturnGrade() {
        return returnGrade;
    }

    public void setReturnGrade(String returnGrade) {
        this.returnGrade = returnGrade;
    }

    public String getReturnSubject() {
        return returnSubject;
    }

    public void setReturnSubject(String returnSubject) {
        this.returnSubject = returnSubject;
    }

    public String getReturnBatchId() {
        return returnBatchId;
    }

    public void setReturnBatchId(String returnBatchId) {
        this.returnBatchId = returnBatchId;
    }
}


