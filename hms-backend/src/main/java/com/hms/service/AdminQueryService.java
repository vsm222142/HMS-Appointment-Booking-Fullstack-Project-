package com.hms.service;

import com.hms.entity.Appointment;
import com.hms.entity.Doctor;
import com.hms.entity.User;
import com.hms.entity.enums.Role;
import com.hms.exception.ApiException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminQueryService {

        private final UserService userService;
        private final UserRepository userRepository;
        private final DoctorRepository doctorRepository;
        private final AppointmentRepository appointmentRepository;

        private void requireAdmin() {
                User u = userService.requireCurrentUser();
                if (u.getRole() != Role.ADMIN)
                        throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        @Transactional(readOnly = true)
        public List<Map<String, Object>> listUsers() {
                requireAdmin();
                return userRepository.findAll().stream()
                                .map(u -> Map.<String, Object>of(
                                                "id", u.getId(),
                                                "name", u.getName(),
                                                "email", u.getEmail(),
                                                "role", u.getRole().name()))
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<Map<String, Object>> listDoctors() {
                requireAdmin();
                return doctorRepository.findAll().stream()
                                .map(this::doctorRow)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<Map<String, Object>> listAppointments() {
                requireAdmin();
                return appointmentRepository.findAll().stream()
                                .map(this::appointmentRow)
                                .toList();
        }

        private Map<String, Object> doctorRow(Doctor d) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", d.getId());
                map.put("userId", d.getUser().getId());
                map.put("name", d.getUser().getName());
                map.put("email", d.getUser().getEmail());
                map.put("specialization", d.getSpecialization());
                map.put("experience", d.getExperience());
                map.put("available", d.getAvailable());
                map.put("imageUrl", d.getUser().getImageUrl());
                return map;
        }

        private Map<String, Object> appointmentRow(Appointment a) {
                return Map.<String, Object>of(
                                "id", a.getId(),
                                "date", a.getAppointmentDate(),
                                "time", a.getAppointmentTime(),
                                "status", a.getStatus().name(),
                                "patient", Map.<String, Object>of(
                                                "id", a.getPatient().getUser().getId(),
                                                "name", a.getPatient().getUser().getName(),
                                                "email", a.getPatient().getUser().getEmail()),
                                "doctor", Map.<String, Object>of(
                                                "id", a.getDoctor().getUser().getId(),
                                                "name", a.getDoctor().getUser().getName(),
                                                "email", a.getDoctor().getUser().getEmail()));
        }
}
