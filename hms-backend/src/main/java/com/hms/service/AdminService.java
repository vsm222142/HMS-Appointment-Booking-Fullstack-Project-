package com.hms.service;

import com.hms.dto.admin.AdminCreateDoctorRequest;
import com.hms.dto.admin.AdminCreateRequest;
import com.hms.dto.admin.DepartmentCreateRequest;
import com.hms.dto.user.UserResponse;
import com.hms.entity.Department;
import com.hms.entity.Doctor;
import com.hms.entity.User;
import com.hms.entity.enums.Role;
import com.hms.exception.ApiException;
import com.hms.repository.DepartmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createDoctor(AdminCreateDoctorRequest req) {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");

        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required for new registration");
        }
        if (req.getPassword().length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        if (req.getImageUrl() == null || req.getImageUrl().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Doctor image URL is required");
        }

        Department dept = null;
        if (req.getDepartmentId() != null) {
            dept = departmentRepository.findById(java.util.Objects.requireNonNull(req.getDepartmentId()))
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Department not found"));
        }

        User doctorUserToSave = User.builder()
                .name(req.getName())
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .imageUrl(req.getImageUrl())
                .role(Role.DOCTOR)
                .build();
        User doctorUser = userRepository.save(java.util.Objects.requireNonNull(doctorUserToSave));

        Doctor doctorToSave = Doctor.builder()
                .user(doctorUser)
                .specialization(req.getSpecialization())
                .experience(req.getExperience())
                .available(req.getAvailable())
                .department(dept)
                .imageUrl(req.getImageUrl())
                .build();
        doctorRepository.save(java.util.Objects.requireNonNull(doctorToSave));

        return AuthService.toUserResponse(doctorUser);
    }

    @Transactional
    public void deleteDoctor(Long doctorUserId) {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");

        User u = userRepository.findById(java.util.Objects.requireNonNull(doctorUserId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found"));
        if (u.getRole() != Role.DOCTOR) throw new ApiException(HttpStatus.BAD_REQUEST, "Not a doctor user");

        doctorRepository.findByUser(u).ifPresent(doctorRepository::delete);
        userRepository.delete(u);
    }

    @Transactional
    public UserResponse updateDoctor(Long doctorUserId, AdminCreateDoctorRequest req) {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");

        User u = userRepository.findById(java.util.Objects.requireNonNull(doctorUserId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found"));
        
        u.setName(req.getName());
        u.setImageUrl(req.getImageUrl());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(u);

        Doctor doctor = doctorRepository.findByUser(u)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
        
        doctor.setSpecialization(req.getSpecialization());
        doctor.setExperience(req.getExperience());
        doctor.setAvailable(req.getAvailable());
        doctor.setImageUrl(req.getImageUrl());
        
        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(java.util.Objects.requireNonNull(req.getDepartmentId()))
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Department not found"));
            doctor.setDepartment(dept);
        }

        doctorRepository.save(doctor);
        return AuthService.toUserResponse(u);
    }

    @Transactional
    public UserResponse createAdmin(AdminCreateRequest req) {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");

        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User newAdminToSave = User.builder()
                .name(req.getName())
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.ADMIN)
                .build();
        User newAdmin = userRepository.save(java.util.Objects.requireNonNull(newAdminToSave));

        return AuthService.toUserResponse(newAdmin);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");

        long doctors = doctorRepository.count();
        long departments = departmentRepository.count();
        long users = userRepository.count();

        return Map.of(
                "doctors", doctors,
                "departments", departments,
                "users", users
        );
    }

    @Transactional
    public Map<String, Object> createDepartment(DepartmentCreateRequest req) {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");

        String name = req.getName().trim();
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Department already exists");
        }
        Department deptToSave = Department.builder().name(name).build();
        Department d = departmentRepository.save(java.util.Objects.requireNonNull(deptToSave));
        return Map.of("id", d.getId(), "name", d.getName());
    }

    @Transactional
    public void deleteDepartment(Long id) {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        Department d = departmentRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Department not found"));
        departmentRepository.delete(java.util.Objects.requireNonNull(d));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDepartments() {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        return departmentRepository.findAll().stream()
                .map(d -> Map.<String, Object>of("id", d.getId(), "name", d.getName()))
                .toList();
    }

    @Transactional
    public void deleteAdmin(Long adminUserId) {
        User admin = userService.requireCurrentUser();
        if (admin.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");

        if (admin.getId().equals(adminUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot delete your own admin account");
        }

        User u = userRepository.findById(java.util.Objects.requireNonNull(adminUserId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Admin not found"));
        
        if (u.getRole() != Role.ADMIN) throw new ApiException(HttpStatus.BAD_REQUEST, "Not an admin user");

        userRepository.delete(u);
    }
}

