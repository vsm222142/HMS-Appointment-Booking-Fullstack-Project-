package com.hms.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "doctors", indexes = {
    @Index(name = "idx_doctor_specialization", columnList = "specialization")
})
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_doctor_user"))
    private User user;

    @Column(nullable = false, length = 120)
    private String specialization;

    @Column(length = 80)
    private String experience;

    @Column(nullable = false)
    private Boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id",
            foreignKey = @ForeignKey(name = "fk_doctor_department"))
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}

