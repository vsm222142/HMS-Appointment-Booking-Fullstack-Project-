package com.hms.controller;

import com.hms.dto.auth.LoginRequest;
import com.hms.dto.common.ApiResponse;
import com.hms.dto.doctor.DoctorProfileUpdateRequest;
import com.hms.entity.enums.Role;
import com.hms.exception.ApiException;
import com.hms.service.AppointmentService;
import com.hms.service.AuthService;
import com.hms.service.DoctorService;
import com.hms.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorCompatController {

    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
        var result = authService.login(req);
        if (result.body().getUser().getRole() != Role.DOCTOR) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only doctor can login here");
        }
        int maxAge = (int) (expirationMinutes * 60);
        res.addCookie(CookieUtil.httpOnlyAuthCookie(result.token(), sslEnabled, maxAge));
        return ApiResponse.ok(result.body());
    }

    @GetMapping("/profile")
    public ApiResponse<?> profile() {
        return ApiResponse.ok(doctorService.getProfile());
    }

    @GetMapping("/appointments")
    public ApiResponse<?> appointments() {
        return ApiResponse.ok(appointmentService.listForCurrentUser());
    }

    @PutMapping("/appointments/{id}/approve")
    public ApiResponse<?> approve(@PathVariable Long id) {
        return ApiResponse.ok(appointmentService.approve(id));
    }

    @PutMapping("/appointments/{id}/reject")
    public ApiResponse<?> reject(@PathVariable Long id, @RequestBody(required = false) com.hms.dto.appointment.RejectRequest req) {
        String reason = (req != null) ? req.getReason() : null;
        return ApiResponse.ok(appointmentService.reject(id, reason));
    }

    @PutMapping("/update")
    public ApiResponse<?> update(@Valid @RequestBody DoctorProfileUpdateRequest req) {
        return ApiResponse.ok(doctorService.updateProfile(req));
    }

    @Data
    public static class UpdateStatusRequest {
        private Long appointmentId;
        private String status;
    }
}

