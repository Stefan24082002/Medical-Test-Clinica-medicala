package com.example.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "treatments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele tratamentului este obligatoriu")
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @NotNull(message = "Prețul este obligatoriu")
    @PositiveOrZero(message = "Prețul nu poate fi negativ")
    private BigDecimal price;

    @ManyToMany(mappedBy = "treatments")
    private Set<Doctor> doctors = new HashSet<>();

    @ManyToMany(mappedBy = "recommendedTreatments")
    private Set<MedicalRecord> medicalRecords = new HashSet<>();
}