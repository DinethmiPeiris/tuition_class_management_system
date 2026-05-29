package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.PaymentSlip;
import com.tuition.new_tuition.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentSlipRepository extends JpaRepository<PaymentSlip, Long> {

    boolean existsByEnrollment_EnrollmentIdAndMonthAndYear(Long enrollmentId, int month, int year);

    boolean existsByEnrollment_EnrollmentIdAndMonthAndYearAndPaymentStatus(
            Long enrollmentId, int month, int year, PaymentStatus paymentStatus);

    List<PaymentSlip> findByEnrollment_Student_IdOrderByUploadedDateDesc(Long studentId);

    List<PaymentSlip> findAllByOrderByUploadedDateDesc();

    List<PaymentSlip> findByMonthAndYearAndPaymentStatus(int month, int year, PaymentStatus paymentStatus);

    @Transactional
    void deleteByEnrollment_EnrollmentId(Long enrollmentId);
}
