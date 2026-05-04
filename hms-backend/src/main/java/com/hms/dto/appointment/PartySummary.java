package com.hms.dto.appointment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartySummary {
    private Long id;
    private String name;
    private String email;
}

