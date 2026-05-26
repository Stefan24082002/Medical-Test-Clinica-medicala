package com.example.clinic.repository;

import com.example.clinic.entity.Patient;
import com.example.clinic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByEmail(String email);

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByUser(User user);
}