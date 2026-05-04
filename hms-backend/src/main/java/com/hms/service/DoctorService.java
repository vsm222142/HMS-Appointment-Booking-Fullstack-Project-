package com.hms.service;

import com.hms.dto.doctor.DepartmentSummary;
import com.hms.dto.doctor.DoctorProfileResponse;
import com.hms.dto.doctor.DoctorProfileUpdateRequest;
import com.hms.entity.Department;
import com.hms.entity.Doctor;
import com.hms.entity.User;
import com.hms.entity.enums.NotificationType;
import com.hms.entity.enums.Role;
import com.hms.exception.ApiException;
import com.hms.repository.DepartmentRepository;
import com.hms.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final UserService userService;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public DoctorProfileResponse getProfile() {
        User user = userService.requireCurrentUser();
        requireRole(user, Role.DOCTOR);
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        return toResponse(user, doctor);
    }

    @Transactional
    public DoctorProfileResponse updateProfile(DoctorProfileUpdateRequest req) {
        User user = userService.requireCurrentUser();
        requireRole(user, Role.DOCTOR);
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(java.util.Objects.requireNonNull(req.getDepartmentId()))
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Department not found"));
        }

        doctor.setSpecialization(req.getSpecialization());
        doctor.setExperience(req.getExperience());
        if (req.getAvailable() != null) doctor.setAvailable(req.getAvailable());
        doctor.setDepartment(department);
        if (req.getImageUrl() != null) doctor.setImageUrl(req.getImageUrl());
        doctorRepository.save(doctor);

        // Notify Doctor
        notificationService.sendNotification(
            user,
            "Your professional profile has been updated successfully.",
            NotificationType.PROFILE_UPDATE,
            user.getId()
        );

        return toResponse(user, doctor);
    }

    public Doctor requireDoctor(User user) {
        requireRole(user, Role.DOCTOR);
        return doctorRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    public static void requireRole(User u, Role role) {
        if (u.getRole() != role) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
    }

    private DoctorProfileResponse toResponse(User user, Doctor doctor) {
        DepartmentSummary dept = null;
        if (doctor.getDepartment() != null) {
            dept = DepartmentSummary.builder()
                    .id(doctor.getDepartment().getId())
                    .name(doctor.getDepartment().getName())
                    .build();
        }
        return DoctorProfileResponse.builder()
                .user(AuthService.toUserResponse(user))
                .specialization(doctor.getSpecialization())
                .experience(doctor.getExperience())
                .available(doctor.getAvailable())
                .department(dept)
                .imageUrl(doctor.getImageUrl())
                .build();
    }
}

