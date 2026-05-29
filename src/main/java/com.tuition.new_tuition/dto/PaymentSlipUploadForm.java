package com.tuition.new_tuition.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class PaymentSlipUploadForm {

    @NotNull(message = "Please select an enrollment.")
    private Long enrollmentId;

    @NotNull(message = "Please enter month.")
    @Min(value = 1, message = "Month must be between 1 and 12.")
    @Max(value = 12, message = "Month must be between 1 and 12.")
    private Integer month;

    @NotNull(message = "Please enter year.")
    @Min(value = 2020, message = "Enter a valid year.")
    private Integer year;

    private MultipartFile slipFile;

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public MultipartFile getSlipFile() {
        return slipFile;
    }

    public void setSlipFile(MultipartFile slipFile) {
        this.slipFile = slipFile;
    }
}
