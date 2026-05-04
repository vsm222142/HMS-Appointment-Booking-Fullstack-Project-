package com.hms.controller;

import com.hms.dto.common.ApiResponse;
import com.hms.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verifyStripe")
    public ApiResponse<?> verifyStripe(@RequestBody VerifyStripeRequest req) {
        var payment = paymentService.verifyStripe(req.getSuccess(), req.getAppointmentId());
        return ApiResponse.ok(Map.of(
                "paymentId", payment.getId(),
                "status", payment.getStatus().name()
        ));
    }

    @Data
    public static class VerifyStripeRequest {
        private Boolean success;
        private Long appointmentId;
    }
}

