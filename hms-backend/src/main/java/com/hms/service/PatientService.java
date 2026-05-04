package com.hms.service;

import com.hms.dto.patient.PatientProfileResponse;
import com.hms.dto.patient.PatientProfileUpdateRequest;
import com.hms.entity.Patient;
import com.hms.entity.User;
import com.hms.entity.enums.NotificationType;
import com.hms.entity.enums.Role;
import com.hms.exception.ApiException;
import com.hms.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserService userService;
    private final PatientRepository patientRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PatientProfileResponse getProfile() {
        User user = userService.requireCurrentUser();
        requireRole(user, Role.PATIENT);
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        return PatientProfileResponse.builder()
                .user(AuthService.toUserResponse(user))
                .age(patient.getAge())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .address(patient.getAddress())
                .build();
    }

    @Transactional
    public PatientProfileResponse updateProfile(PatientProfileUpdateRequest req) {
        User user = userService.requireCurrentUser();
        requireRole(user, Role.PATIENT);
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));

        patient.setAge(req.getAge());
        patient.setGender(req.getGender());
        patient.setPhone(req.getPhone());
        patient.setAddress(req.getAddress());
        patientRepository.save(patient);

        // Notify Patient
        notificationService.sendNotification(
            user,
            "Your profile has been updated successfully.",
            NotificationType.PROFILE_UPDATE,
            user.getId()
        );

        return getProfile();
    }

    public Patient requirePatient(User user) {
        requireRole(user, Role.PATIENT);
        return patientRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));
    }

    private static void requireRole(User u, Role role) {
        // Accept legacy USER as PATIENT alias
        if (role == Role.PATIENT && u.getRole() == Role.USER) return;
        if (u.getRole() != role) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
    }
}

