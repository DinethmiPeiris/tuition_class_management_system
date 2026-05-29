package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.Material;
import com.tuition.new_tuition.entity.PaymentStatus;
import com.tuition.new_tuition.repository.MaterialRepository;
import com.tuition.new_tuition.repository.PaymentSlipRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialAccessService {

    private final PaymentSlipRepository paymentSlipRepository;
    private final MaterialRepository materialRepository;

    public MaterialAccessService(PaymentSlipRepository paymentSlipRepository,
                                 MaterialRepository materialRepository) {
        this.paymentSlipRepository = paymentSlipRepository;
        this.materialRepository = materialRepository;
    }

    public List<Material> getMaterialsIfPaymentApproved(
            Long enrollmentId,
            Long batchId,
            int month,
            int year
    ) {

        boolean isApproved = paymentSlipRepository
                .existsByEnrollment_EnrollmentIdAndMonthAndYearAndPaymentStatus(
                        enrollmentId,
                        month,
                        year,
                        PaymentStatus.APPROVED
                );

        if (!isApproved) {
            throw new RuntimeException("Payment not approved for this month.");
        }

        return materialRepository
                .findByBatch_BatchIdAndMonthAndYearOrderByUploadedDateDesc(batchId, month, year);
    }
}
