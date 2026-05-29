package com.tuition.new_tuition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CourseUpdateForm {

    @NotBlank(message = "Subject is required.")
    @Size(min = 3, max = 40, message = "Subject must be 3 to 40 characters.")
    private String subject;

    @NotBlank(message = "Grade is required.")
    @Size(max = 20, message = "Grade can be max 20 characters.")
    private String grade;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
