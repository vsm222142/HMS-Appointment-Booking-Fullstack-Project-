package com.hms.controller;

import com.hms.dto.appointment.AppointmentCreateRequest;
import com.hms.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody AppointmentCreateRequest req) {
        return Map.of("success", true, "data", appointmentService.create(req));
    }

    @GetMapping
    public Map<String, Object> listMine() {
        return Map.of("success", true, "data", appointmentService.listForCurrentUser());
    }

    @GetMapping("/booked-slots")
    public Map<String, Object> getBookedSlots(@RequestParam Long doctorId, @RequestParam java.time.LocalDate date) {
        return Map.of("success", true, "data", appointmentService.getBookedSlots(doctorId, date));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return Map.of("success", true);
    }

    @PostMapping("/{id}/pay")
    public Map<String, Object> pay(@PathVariable Long id) {
        appointmentService.pay(id);
        return Map.of("success", true);
    }
}

