package com.hms.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateDoctorRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Email
    @Size(max = 190)
    private String email;

    @Size(max = 72)
    private String password;

    @NotBlank
    @Size(max = 120)
    private String specialization;

    @Size(max = 80)
    private String experience;

    @NotNull
    private Boolean available;

    private Long departmentId;

    private String imageUrl;
}

