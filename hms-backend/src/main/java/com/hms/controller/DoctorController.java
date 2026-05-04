package com.hms.controller;

import com.hms.dto.doctor.DoctorProfileUpdateRequest;
import com.hms.service.AppointmentService;
import com.hms.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        return Map.of("success", true, "data", doctorService.getProfile());
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@Valid @RequestBody DoctorProfileUpdateRequest req) {
        return Map.of("success", true, "data", doctorService.updateProfile(req));
    }

    @GetMapping("/appointments")
    public Map<String, Object> appointments() {
        return Map.of("success", true, "data", appointmentService.listForCurrentUser());
    }

    @PutMapping("/appointments/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id) {
        return Map.of("success", true, "data", appointmentService.approve(id));
    }
}

