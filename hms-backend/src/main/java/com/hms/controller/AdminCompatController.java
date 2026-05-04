package com.hms.controller;

import com.hms.dto.admin.AdminCreateDoctorRequest;
import com.hms.dto.common.ApiResponse;
import com.hms.service.AdminQueryService;
import com.hms.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCompatController {

    private final AdminService adminService;
    private final AdminQueryService adminQueryService;

    @PostMapping("/add-doctor")
    public ApiResponse<?> addDoctor(@Valid @RequestBody AdminCreateDoctorRequest req) {
        return ApiResponse.ok(adminService.createDoctor(req));
    }

    @GetMapping("/doctors")
    public ApiResponse<?> doctors() {
        return ApiResponse.ok(adminQueryService.listDoctors());
    }

    @DeleteMapping("/delete-doctor")
    public ApiResponse<?> deleteDoctor(@RequestParam("id") Long id) {
        adminService.deleteDoctor(id);
        return ApiResponse.ok(true);
    }

    @GetMapping("/all-users")
    public ApiResponse<?> allUsers() {
        return ApiResponse.ok(adminQueryService.listUsers());
    }

    @GetMapping("/all-appointments")
    public ApiResponse<?> allAppointments() {
        return ApiResponse.ok(adminQueryService.listAppointments());
    }
}

