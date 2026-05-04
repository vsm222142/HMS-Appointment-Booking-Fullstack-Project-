package com.hms.controller;

import com.hms.dto.patient.PatientProfileUpdateRequest;
import com.hms.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        return Map.of("success", true, "data", patientService.getProfile());
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@Valid @RequestBody PatientProfileUpdateRequest req) {
        return Map.of("success", true, "data", patientService.updateProfile(req));
    }
}

