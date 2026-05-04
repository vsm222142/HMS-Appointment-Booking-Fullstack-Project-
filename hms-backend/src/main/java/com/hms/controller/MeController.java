package com.hms.controller;

import com.hms.dto.common.ApiResponse;
import com.hms.service.AuthService;
import com.hms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<?> me() {
        return ApiResponse.ok(AuthService.toUserResponse(userService.requireCurrentUser()));
    }

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.springframework.web.bind.annotation.PutMapping("/me/profile")
    public ApiResponse<?> updateProfile(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> req) {
        var user = userService.requireCurrentUser();
        if (req.containsKey("name")) user.setName(req.get("name"));
        if (req.containsKey("imageUrl")) user.setImageUrl(req.get("imageUrl"));
        
        if (req.containsKey("gender")) {
            try {
                user.setGender(com.hms.entity.enums.Gender.valueOf(req.get("gender").toUpperCase()));
            } catch (Exception e) {
                user.setGender(null);
            }
        }
        
        if (req.containsKey("phone")) user.setPhone(req.get("phone"));
        
        if (req.containsKey("password") && req.get("password") != null && !req.get("password").isBlank()) {
            if (req.get("password").length() < 6) {
                return ApiResponse.fail("Password must be at least 6 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(req.get("password")));
        }

        return ApiResponse.ok(AuthService.toUserResponse(userService.save(user)));
    }

}

