package com.example.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Numele serviciului este obligatoriu")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Prețul este obligatoriu")
    @DecimalMin(value = "0.0", inclusive = true, message = "Prețul trebuie să fie pozitiv")
    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany(mappedBy = "treatments")
    private Set<Doctor> doctors = new HashSet<>();

    @ManyToMany(mappedBy = "recommendedTreatments")
    private Set<MedicalRecord> medicalRecords = new HashSet<>();
}