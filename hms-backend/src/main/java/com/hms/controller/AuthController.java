package com.hms.controller;

import com.hms.dto.auth.LoginRequest;
import com.hms.dto.auth.RegisterRequest;
import com.hms.service.AuthService;
import com.hms.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req, HttpServletResponse res) {
        var result = authService.register(req);
        int maxAge = (int) (expirationMinutes * 60);
        res.addCookie(CookieUtil.httpOnlyAuthCookie(result.token(), sslEnabled, maxAge));
        return Map.of("success", true, "token", result.token(), "data", result.body());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
        var result = authService.login(req);
        int maxAge = (int) (expirationMinutes * 60);
        res.addCookie(CookieUtil.httpOnlyAuthCookie(result.token(), sslEnabled, maxAge));
        return Map.of("success", true, "token", result.token(), "data", result.body());
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletResponse res) {
        res.addCookie(CookieUtil.clearAuthCookie(sslEnabled));
        return Map.of("success", true);
    }

    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(@RequestBody Map<String, String> body) {
        String otp = authService.forgotPassword(body.get("email"));
        return Map.of("success", true, "otp", otp);
    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> body) {
        authService.resetPassword(body.get("email"), body.get("otp"), body.get("newPassword"));
        return Map.of("success", true);
    }
}

