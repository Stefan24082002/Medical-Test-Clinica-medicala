package com.example.clinic.repository;

import com.example.clinic.entity.Department;
import com.example.clinic.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    boolean existsByName(String name);

    List<Treatment> findByDepartment(Department department);
}