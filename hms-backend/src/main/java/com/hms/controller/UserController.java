package com.hms.controller;

import com.hms.dto.common.ApiResponse;
import com.hms.dto.patient.PatientProfileUpdateRequest;
import com.hms.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final PatientService patientService;

    @GetMapping("/profile")
    public ApiResponse<?> profile() {
        return ApiResponse.ok(patientService.getProfile());
    }

    @PutMapping("/update")
    public ApiResponse<?> update(@RequestBody PatientProfileUpdateRequest req) {
        return ApiResponse.ok(patientService.updateProfile(req));
    }
}

