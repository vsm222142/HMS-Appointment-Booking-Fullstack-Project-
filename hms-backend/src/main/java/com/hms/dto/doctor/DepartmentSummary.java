package com.hms.dto.doctor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentSummary {
    private Long id;
    private String name;
}

