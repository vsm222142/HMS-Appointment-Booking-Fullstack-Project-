package com.hms.controller;

import com.hms.dto.appointment.AppointmentCreateRequest;
import com.hms.dto.common.ApiResponse;
import com.hms.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentCompatController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ApiResponse<?> book(@RequestBody AppointmentCreateRequest req) {
        return ApiResponse.ok(appointmentService.create(req));
    }

    @GetMapping("/user")
    public ApiResponse<?> userAppointments() {
        return ApiResponse.ok(appointmentService.listForCurrentUser());
    }

    @GetMapping("/doctor")
    public ApiResponse<?> doctorAppointments() {
        return ApiResponse.ok(appointmentService.listForCurrentUser());
    }

    @DeleteMapping("/cancel")
    public ApiResponse<?> cancel(@RequestParam("id") Long id) {
        appointmentService.cancel(id);
        return ApiResponse.ok(true);
    }
}

