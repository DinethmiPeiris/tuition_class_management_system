package com.tuition.new_tuition.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BatchForm {

    private Long batchId;

    @NotBlank(message = "Batch name is required.")
    private String batchName;

    @NotNull(message = "Year is required.")
    @Min(value = 2000, message = "Enter a valid year.")
    private Integer year;

    @NotBlank(message = "Please select a status.")
    private String status;

    @NotBlank(message = "Please select a course.")
    private String courseId;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
}
