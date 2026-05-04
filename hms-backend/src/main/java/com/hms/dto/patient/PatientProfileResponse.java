package com.hms.dto.patient;

import com.hms.dto.user.UserResponse;
import com.hms.entity.enums.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientProfileResponse {
    private UserResponse user;
    private Integer age;
    private Gender gender;
    private String phone;
    private String address;
}

