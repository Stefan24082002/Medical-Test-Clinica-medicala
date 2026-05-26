package com.example.clinic.repository;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatient(Patient patient);

    List<MedicalRecord> findByDoctor(Doctor doctor);

    Optional<MedicalRecord> findByAppointment(Appointment appointment);

    boolean existsByAppointment(Appointment appointment);
}