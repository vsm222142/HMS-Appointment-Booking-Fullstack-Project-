package com.hms.controller;

import com.hms.dto.admin.AdminCreateDoctorRequest;
import com.hms.dto.admin.AdminCreateRequest;
import com.hms.dto.admin.DepartmentCreateRequest;
import com.hms.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final com.hms.service.AdminQueryService adminQueryService;

    @GetMapping("/users")
    public Map<String, Object> listUsers() {
        return Map.of("success", true, "data", adminQueryService.listUsers());
    }

    @PostMapping("/doctors")
    public Map<String, Object> createDoctor(@Valid @RequestBody AdminCreateDoctorRequest req) {
        return Map.of("success", true, "data", adminService.createDoctor(req));
    }

    @DeleteMapping("/doctors/{id}")
    public Map<String, Object> deleteDoctor(@PathVariable Long id) {
        adminService.deleteDoctor(id);
        return Map.of("success", true);
    }

    @PutMapping("/doctors/{id}")
    public Map<String, Object> updateDoctor(@PathVariable Long id, @Valid @RequestBody AdminCreateDoctorRequest req) {
        return Map.of("success", true, "data", adminService.updateDoctor(id, req));
    }

    @PostMapping("/create-admin")
    public Map<String, Object> createAdmin(@Valid @RequestBody AdminCreateRequest req) {
        return Map.of("success", true, "data", adminService.createAdmin(req));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of("success", true, "data", adminService.dashboard());
    }

    @PostMapping("/departments")
    public Map<String, Object> createDepartment(@Valid @RequestBody DepartmentCreateRequest req) {
        return Map.of("success", true, "data", adminService.createDepartment(req));
    }

    @GetMapping("/departments")
    public Map<String, Object> listDepartments() {
        return Map.of("success", true, "data", adminService.listDepartments());
    }

    @DeleteMapping("/departments/{id}")
    public Map<String, Object> deleteDepartment(@PathVariable Long id) {
        adminService.deleteDepartment(id);
        return Map.of("success", true);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return Map.of("success", true);
    }
}

