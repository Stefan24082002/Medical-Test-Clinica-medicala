package com.example.clinic.repository;

import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatient(Patient patient);

    List<MedicalRecord> findByDoctor(Doctor doctor);
}