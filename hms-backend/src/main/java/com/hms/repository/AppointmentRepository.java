package com.hms.repository;

import com.hms.entity.Appointment;
import com.hms.entity.Doctor;
import com.hms.entity.Patient;
import com.hms.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByDoctorAndAppointmentDateAndAppointmentTime(Doctor doctor, LocalDate date, LocalTime time);
    List<Appointment> findByPatientOrderByCreatedAtDesc(Patient patient);
    List<Appointment> findByDoctorOrderByCreatedAtDesc(Doctor doctor);
    Optional<Appointment> findByIdAndDoctor(Long id, Doctor doctor);
    Optional<Appointment> findByIdAndPatient(Long id, Patient patient);
    List<Appointment> findByStatusAndAppointmentDateBefore(AppointmentStatus status, LocalDate date);
    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, LocalDate date);
}

