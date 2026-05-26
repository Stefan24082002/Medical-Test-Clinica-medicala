package com.example.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    private LocalDate recordDate;

    @NotBlank(message = "Diagnosticul este obligatoriu")
    @Column(nullable = false, length = 1000)
    private String diagnosis;

    @Column(length = 1000)
    private String prescription;

    @Column(length = 1500)
    private String notes;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalServicesPrice = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToMany
    @JoinTable(
            name = "medical_record_treatments",
            joinColumns = @JoinColumn(name = "medical_record_id"),
            inverseJoinColumns = @JoinColumn(name = "treatment_id")
    )
    private Set<Treatment> recommendedTreatments = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }

        if (totalServicesPrice == null) {
            totalServicesPrice = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (totalServicesPrice == null) {
            totalServicesPrice = BigDecimal.ZERO;
        }
    }
}