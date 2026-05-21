package com.example.clinic.repository;

import com.example.clinic.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    Optional<Treatment> findByName(String name);

    boolean existsByName(String name);
}