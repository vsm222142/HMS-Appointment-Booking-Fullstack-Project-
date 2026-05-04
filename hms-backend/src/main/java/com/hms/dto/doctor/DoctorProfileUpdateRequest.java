package com.hms.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorProfileUpdateRequest {

    @NotBlank
    @Size(max = 120)
    private String specialization;

    @Size(max = 80)
    private String experience;

    private Boolean available;

    private Long departmentId;

    private String imageUrl;
}

