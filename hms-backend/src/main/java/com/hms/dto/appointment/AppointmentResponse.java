package com.hms.dto.appointment;

import com.hms.entity.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private AppointmentStatus status;
    private LocalDate date;
    private LocalTime time;

    private PartySummary patient;
    private PartySummary doctor;

    private Double amount;
    private com.hms.entity.enums.PaymentStatus paymentStatus;
}

