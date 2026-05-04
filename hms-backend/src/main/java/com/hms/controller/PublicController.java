package com.hms.controller;

import com.hms.entity.Doctor;
import com.hms.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final DoctorRepository doctorRepository;

    @GetMapping("/doctors")
    public Map<String, Object> doctors() {
        List<Map<String, Object>> data = doctorRepository.findAllWithUser().stream()
                .map(this::toPublicDoctor)
                .toList();
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> toPublicDoctor(Doctor d) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", d.getId());
        map.put("userId", d.getUser().getId());
        map.put("name", d.getUser().getName());
        map.put("email", d.getUser().getEmail());
        map.put("specialization", d.getSpecialization());
        map.put("experience", d.getExperience());
        map.put("available", d.getAvailable());
        map.put("imageUrl", d.getUser().getImageUrl());
        if (d.getDepartment() != null) {
            map.put("department", Map.of(
                    "id", d.getDepartment().getId(),
                    "name", d.getDepartment().getName()
            ));
        } else {
            map.put("department", null);
        }
        return map;
    }
}

