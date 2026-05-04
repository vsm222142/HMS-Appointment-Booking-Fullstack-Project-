package com.hms.service;

import com.hms.dto.auth.AuthResponse;
import com.hms.dto.auth.LoginRequest;
import com.hms.dto.auth.RegisterRequest;
import com.hms.dto.user.UserResponse;
import com.hms.entity.Patient;
import com.hms.entity.User;
import com.hms.entity.enums.NotificationType;
import com.hms.entity.enums.Role;
import com.hms.exception.ApiException;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.UserRepository;
import com.hms.util.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    @Transactional
    public AuthResult register(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        Role role = req.getRole();
        if (role == Role.USER) role = Role.PATIENT;

        // Restriction: Only PATIENT can register publicly
        if (role != Role.PATIENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only Patients can register themselves");
        }

        User userToSave = User.builder()
                .name(req.getName())
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();
        User user = userRepository.save(java.util.Objects.requireNonNull(userToSave));

        Patient patientToSave = Patient.builder().user(user).build();
        patientRepository.save(java.util.Objects.requireNonNull(patientToSave));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResult(token, AuthResponse.builder().user(toUserResponse(user)).build());
    }

    @Transactional
    public AuthResult login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // Restriction: If doctor, check if added by admin
        if (user.getRole() == Role.DOCTOR) {
            boolean exists = doctorRepository.findByUser(user).isPresent();
            if (!exists) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Admin has not granted access");
            }
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        // Notify other admins if an admin logs in
        if (user.getRole() == Role.ADMIN) {
            userRepository.findByRole(Role.ADMIN).stream()
                .filter(admin -> !admin.getId().equals(user.getId()))
                .forEach(admin -> notificationService.sendNotification(
                    admin,
                    "Admin Alert: " + user.getName() + " (" + user.getEmail() + ") has just logged into the system.",
                    NotificationType.LOGIN_ALERT,
                    user.getId()
                ));
        }

        return new AuthResult(token, AuthResponse.builder().user(toUserResponse(user)).build());
    }

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found with this email"));

        if (user.getRole() == Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin password reset is restricted for security. Please contact system owner.");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10)); // 10 mins valid
        userRepository.save(user);

        return otp;
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Email not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin password cannot be reset via OTP for security reasons.");
        }

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP Expired");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    public static UserResponse toUserResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .imageUrl(u.getImageUrl())
                .gender(u.getGender())
                .phone(u.getPhone())
                .build();
    }

    public record AuthResult(String token, AuthResponse body) {}
}
