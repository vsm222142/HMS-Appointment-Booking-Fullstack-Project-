package com.hms.service;

import com.hms.dto.appointment.AppointmentCreateRequest;
import com.hms.dto.appointment.AppointmentResponse;
import com.hms.dto.appointment.PartySummary;
import com.hms.entity.Appointment;
import com.hms.entity.Doctor;
import com.hms.entity.Patient;
import com.hms.entity.User;
import com.hms.entity.enums.AppointmentStatus;
import com.hms.entity.enums.NotificationType;
import com.hms.entity.enums.Role;
import com.hms.exception.ApiException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final com.hms.repository.UserRepository userRepository;
    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final com.hms.repository.PatientRepository patientRepository;
    private final NotificationService notificationService;

    /**
     * Runs every 30 minutes to mark past appointments as COMPLETED.
     * Logic: If status is APPROVED and date < today -> COMPLETED.
     */
    @Scheduled(fixedRate = 1800000) // 1,800,000 ms = 30 minutes
    @Transactional
    public void autoUpdateAppointmentStatus() {
        LocalDate today = LocalDate.now();
        List<Appointment> pastAppointments = appointmentRepository.findByStatusAndAppointmentDateBefore(
                AppointmentStatus.APPROVED, today
        );

        if (!pastAppointments.isEmpty()) {
            pastAppointments.forEach(a -> a.setStatus(AppointmentStatus.COMPLETED));
            appointmentRepository.saveAll(pastAppointments);
            System.out.println("Auto-completed " + pastAppointments.size() + " past appointments.");
        }
    }

    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest req) {
        User user = userService.requireCurrentUser();
        if (user.getRole() != Role.PATIENT && user.getRole() != Role.USER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only users can book");
        }

        Patient patient = patientService.requirePatient(user);
        
        // Update patient profile automatically
        user.setPhone(req.getPhone());
        user.setGender(req.getGender());
        userRepository.save(user); 
        
        patient.setAge(req.getAge());
        patient.setGender(req.getGender());
        patient.setPhone(req.getPhone());
        patientRepository.save(patient);
        
        Doctor doctor = doctorRepository.findById(java.util.Objects.requireNonNull(req.getDoctorId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found"));

        boolean exists = appointmentRepository.existsByDoctorAndAppointmentDateAndAppointmentTime(
                doctor, req.getDate(), req.getTime()
        );
        if (exists) throw new ApiException(HttpStatus.CONFLICT, "Slot already booked");

        Appointment apptToSave = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(req.getDate())
                .appointmentTime(req.getTime())
                .status(AppointmentStatus.PENDING)
                .amount(200.0)
                .paymentStatus(com.hms.entity.enums.PaymentStatus.PENDING)
                .build();
        Appointment appt = appointmentRepository.save(java.util.Objects.requireNonNull(apptToSave));
        
        // Notify Doctor
        notificationService.sendNotification(
            doctor.getUser(),
            "New appointment booked by " + user.getName() + " for " + req.getDate(),
            NotificationType.APPOINTMENT_BOOKED,
            appt.getId()
        );

        return toResponse(appt);
    }

    @Transactional(readOnly = true)
    public List<String> getBookedSlots(Long doctorId, LocalDate date) {
        if (doctorId == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Doctor ID is required");
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found"));
        return appointmentRepository.findByDoctorAndAppointmentDate(doctor, date)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.REJECTED)
                .map(a -> a.getAppointmentTime().toString().substring(0, 5)) // HH:mm
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listForCurrentUser() {
        User user = userService.requireCurrentUser();
        if (user.getRole() == Role.PATIENT || user.getRole() == Role.USER) {
            Patient patient = patientService.requirePatient(user);
            return appointmentRepository.findByPatientOrderByCreatedAtDesc(patient)
                    .stream().map(this::toResponse).toList();
        }
        if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorService.requireDoctor(user);
            return appointmentRepository.findByDoctorOrderByCreatedAtDesc(doctor)
                    .stream().map(this::toResponse).toList();
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
    }

    @Transactional
    public void cancel(Long id) {
        User user = userService.requireCurrentUser();
        if (user.getRole() != Role.PATIENT && user.getRole() != Role.USER) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        Patient patient = patientService.requirePatient(user);

        Appointment appt = appointmentRepository.findByIdAndPatient(id, patient)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
        
        appt.setStatus(AppointmentStatus.CANCELLED);
        
        // Refund if already paid
        if (appt.getPaymentStatus() == com.hms.entity.enums.PaymentStatus.PAID) {
            appt.setPaymentStatus(com.hms.entity.enums.PaymentStatus.REFUNDED);
        }
        
        appointmentRepository.save(appt);
    }

    @Transactional
    public AppointmentResponse approve(Long id) {
        User user = userService.requireCurrentUser();
        if (user.getRole() != Role.DOCTOR) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        Doctor doctor = doctorService.requireDoctor(user);

        Appointment appt = appointmentRepository.findByIdAndDoctor(id, doctor)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cancelled appointment can't be approved");
        }
        appt.setStatus(AppointmentStatus.APPROVED);
        appointmentRepository.save(appt);

        // Notify Patient
        notificationService.sendNotification(
            appt.getPatient().getUser(),
            "Your appointment with Dr. " + doctor.getUser().getName() + " on " + appt.getAppointmentDate() + " has been APPROVED.",
            NotificationType.APPOINTMENT_APPROVED,
            appt.getId()
        );

        return toResponse(appt);
    }

    @Transactional
    public AppointmentResponse reject(Long id, String reason) {
        User user = userService.requireCurrentUser();
        if (user.getRole() != Role.DOCTOR) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        Doctor doctor = doctorService.requireDoctor(user);

        Appointment appt = appointmentRepository.findByIdAndDoctor(id, doctor)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
        
        appt.setStatus(AppointmentStatus.REJECTED);
        
        // Refund if already paid
        if (appt.getPaymentStatus() == com.hms.entity.enums.PaymentStatus.PAID) {
            appt.setPaymentStatus(com.hms.entity.enums.PaymentStatus.REFUNDED);
        }
        
        appointmentRepository.save(appt);

        // Notify Patient
        String msg = "Your appointment with Dr. " + doctor.getUser().getName() + " on " + appt.getAppointmentDate() + " has been REJECTED.";
        if (reason != null && !reason.isBlank()) {
            msg += " Reason: " + reason;
        }
        notificationService.sendNotification(
            appt.getPatient().getUser(),
            msg,
            NotificationType.APPOINTMENT_REJECTED,
            appt.getId()
        );

        return toResponse(appt);
    }

    @Transactional
    public void pay(Long id) {
        User user = userService.requireCurrentUser();
        Appointment a = appointmentRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!a.getPatient().getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        if (a.getPaymentStatus() == com.hms.entity.enums.PaymentStatus.PAID) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Already paid");
        }

        // Simulate successful payment
        a.setPaymentStatus(com.hms.entity.enums.PaymentStatus.PAID);
        appointmentRepository.save(a);
    }

    private AppointmentResponse toResponse(Appointment appt) {
        return AppointmentResponse.builder()
                .id(appt.getId())
                .status(appt.getStatus())
                .date(appt.getAppointmentDate())
                .time(appt.getAppointmentTime())
                .patient(PartySummary.builder()
                        .id(appt.getPatient().getUser().getId())
                        .name(appt.getPatient().getUser().getName())
                        .email(appt.getPatient().getUser().getEmail())
                        .build())
                .doctor(PartySummary.builder()
                        .id(appt.getDoctor().getUser().getId())
                        .name(appt.getDoctor().getUser().getName())
                        .email(appt.getDoctor().getUser().getEmail())
                        .build())
                .amount(appt.getAmount())
                .paymentStatus(appt.getPaymentStatus())
                .build();
    }
}

