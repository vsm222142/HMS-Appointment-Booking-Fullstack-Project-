package com.hms.dto.user;

import com.hms.entity.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String imageUrl;
    private com.hms.entity.enums.Gender gender;
    private String phone;
}

