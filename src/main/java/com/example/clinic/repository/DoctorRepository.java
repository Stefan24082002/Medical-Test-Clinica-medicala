package com.example.clinic.repository;

import com.example.clinic.entity.Department;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByEmail(String email);

    Optional<Doctor> findByUser(User user);

    List<Doctor> findByDepartment(Department department);
}