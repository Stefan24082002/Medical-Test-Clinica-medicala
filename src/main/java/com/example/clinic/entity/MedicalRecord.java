package com.example.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Data fișei medicale este obligatorie")
    @Column(nullable = false)
    private LocalDate recordDate;

    @NotBlank(message = "Diagnosticul este obligatoriu")
    @Column(nullable = false, length = 1000)
    private String diagnosis;

    @Column(length = 1000)
    private String prescription;

    @Column(length = 1000)
    private String notes;

    @NotNull(message = "Pacientul este obligatoriu")
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @ManyToMany
    @JoinTable(
            name = "medical_record_treatments",
            joinColumns = @JoinColumn(name = "medical_record_id"),
            inverseJoinColumns = @JoinColumn(name = "treatment_id")
    )
    private Set<Treatment> recommendedTreatments = new HashSet<>();

    @Transient
    public BigDecimal getTotalServicesPrice() {
        if (recommendedTreatments == null || recommendedTreatments.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return recommendedTreatments.stream()
                .map(Treatment::getPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}