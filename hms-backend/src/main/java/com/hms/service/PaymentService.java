package com.hms.service;

import com.hms.entity.Appointment;
import com.hms.entity.Payment;
import com.hms.entity.enums.PaymentStatus;
import com.hms.exception.ApiException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment verifyStripe(Boolean success, Long appointmentId) {
        if (appointmentId == null) throw new ApiException(HttpStatus.BAD_REQUEST, "appointmentId required");
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));

        Payment payment = paymentRepository.findByAppointment(appt)
                .orElseGet(() -> Payment.builder()
                        .appointment(appt)
                        .amount(BigDecimal.ZERO)
                        .status(PaymentStatus.PENDING)
                        .build());

        if (Boolean.TRUE.equals(success)) {
            payment.setStatus(PaymentStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        return paymentRepository.save(payment);
    }
}

