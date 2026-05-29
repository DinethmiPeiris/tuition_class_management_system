package com.tuition.new_tuition.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class UpdateTimetableRequest {

    @NotNull(message = "Please select subject")
    private Long subjectId;

    @NotNull(message = "Please select grade")
    private Integer grade;

    @NotNull(message = "Please select batch")
    private Long batchId;

    @NotNull(message = "Please select date")
    private LocalDate date;

    @NotNull(message = "Please select start time")
    private LocalTime startTime;

    @NotNull(message = "Please select end time")
    private LocalTime endTime;

    private String location;

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
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
}
