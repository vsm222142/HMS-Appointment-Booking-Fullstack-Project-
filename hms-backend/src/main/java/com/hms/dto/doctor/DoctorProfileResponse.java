package com.hms.dto.doctor;

import com.hms.dto.user.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorProfileResponse {
    private UserResponse user;
    private String specialization;
    private String experience;
    private Boolean available;
    private DepartmentSummary department;
    private String imageUrl;
}

