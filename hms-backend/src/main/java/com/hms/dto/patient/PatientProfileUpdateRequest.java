package com.hms.dto.patient;

import com.hms.entity.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientProfileUpdateRequest {

    @Min(0)
    @Max(150)
    private Integer age;

    private Gender gender;

    @jakarta.validation.constraints.Pattern(regexp = "^[6-9]\\d{9}$", message = "Please enter a valid 10-digit Indian phone number starting with 6-9")
    private String phone;

    @Size(max = 255)
    private String address;
}

