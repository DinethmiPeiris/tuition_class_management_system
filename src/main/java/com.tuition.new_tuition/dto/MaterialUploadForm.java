package com.tuition.new_tuition.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class MaterialUploadForm {

    private Long materialId;

    @NotBlank(message = "Please select a grade.")
    private String grade;

    @NotBlank(message = "Please select a subject.")
    private String subject;

    @NotBlank(message = "Please enter a title.")
    private String title;

    @NotNull(message = "Please enter month.")
    @Min(value = 1, message = "Month must be between 1 and 12.")
    @Max(value = 12, message = "Month must be between 1 and 12.")
    private Integer month;

    @NotNull(message = "Please enter year.")
    @Min(value = 2020, message = "Enter a valid year.")
    private Integer year;

    private MultipartFile materialFile;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public MultipartFile getMaterialFile() { return materialFile; }
    public void setMaterialFile(MultipartFile materialFile) { this.materialFile = materialFile; }
}
