package com.tuition.new_tuition.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "payment_slip")
public class PaymentSlip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentSlipId;

    private String slipImage;

    private LocalDate uploadedDate;

    private int month;
    private int year;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    public Long getPaymentSlipId() {
        return paymentSlipId;
    }

    public void setPaymentSlipId(Long paymentSlipId) {
        this.paymentSlipId = paymentSlipId;
    }

    public String getSlipImage() {
        return slipImage;
    }

    public void setSlipImage(String slipImage) {
        this.slipImage = slipImage;
    }

    public LocalDate getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(LocalDate uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }
}
