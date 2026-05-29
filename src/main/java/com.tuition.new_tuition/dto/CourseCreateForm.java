package com.tuition.new_tuition.dto;

import jakarta.validation.constraints.*;

public class CourseCreateForm {

    @NotBlank(message = "Course ID is required.")
    @Pattern(regexp = "^[A-Za-z]{2,6}\\d{1,3}$", message = "Course ID must be like SC10 / MA11 / ENG12.")
    private String courseId;

    @NotBlank(message = "Subject is required.")
    @Size(min = 3, max = 40, message = "Subject must be 3 to 40 characters.")
    private String subject;

    @NotBlank(message = "Grade is required.")
    @Size(max = 20, message = "Grade can be max 20 characters.")
    private String grade;

    @NotNull(message = "Batch Year is required.")
    @Min(value = 2000, message = "Batch Year must be >= 2000.")
    @Max(value = 2100, message = "Batch Year must be <= 2100.")
    private Integer year;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}
