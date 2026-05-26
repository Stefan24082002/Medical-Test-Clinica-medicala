package com.example.clinic.service;

import com.example.clinic.entity.*;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        log.info("Creating medical record");

        if (medicalRecord.getAppointment() != null
                && medicalRecordRepository.existsByAppointment(medicalRecord.getAppointment())) {
            throw new IllegalArgumentException("Această programare are deja o fișă medicală.");
        }

        if (medicalRecord.getRecordDate() == null) {
            medicalRecord.setRecordDate(LocalDate.now());
        }

        calculateTotalServicesPrice(medicalRecord);

        return medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    public Page<MedicalRecord> getAllMedicalRecords(Pageable pageable) {
        return medicalRecordRepository.findAll(pageable);
    }

    public MedicalRecord getMedicalRecordById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fișa medicală nu a fost găsită cu id-ul: " + id));
    }

    public List<MedicalRecord> getMedicalRecordsByPatient(Patient patient) {
        return medicalRecordRepository.findByPatient(patient);
    }

    public List<MedicalRecord> getMedicalRecordsByDoctor(Doctor doctor) {
        return medicalRecordRepository.findByDoctor(doctor);
    }

    public Optional<MedicalRecord> getMedicalRecordByAppointment(Appointment appointment) {
        return medicalRecordRepository.findByAppointment(appointment);
    }

    public boolean existsByAppointment(Appointment appointment) {
        return medicalRecordRepository.existsByAppointment(appointment);
    }

    public MedicalRecord updateMedicalRecord(Long id, MedicalRecord updatedMedicalRecord) {
        MedicalRecord existingRecord = getMedicalRecordById(id);

        if (updatedMedicalRecord.getAppointment() != null) {
            Optional<MedicalRecord> recordForAppointment =
                    medicalRecordRepository.findByAppointment(updatedMedicalRecord.getAppointment());

            if (recordForAppointment.isPresent()
                    && !recordForAppointment.get().getId().equals(existingRecord.getId())) {
                throw new IllegalArgumentException("Această programare are deja o fișă medicală.");
            }
        }

        existingRecord.setRecordDate(updatedMedicalRecord.getRecordDate());
        existingRecord.setPatient(updatedMedicalRecord.getPatient());
        existingRecord.setDoctor(updatedMedicalRecord.getDoctor());
        existingRecord.setAppointment(updatedMedicalRecord.getAppointment());
        existingRecord.setDiagnosis(updatedMedicalRecord.getDiagnosis());
        existingRecord.setPrescription(updatedMedicalRecord.getPrescription());
        existingRecord.setNotes(updatedMedicalRecord.getNotes());

        if (updatedMedicalRecord.getRecommendedTreatments() != null) {
            existingRecord.setRecommendedTreatments(updatedMedicalRecord.getRecommendedTreatments());
        } else {
            existingRecord.setRecommendedTreatments(new HashSet<>());
        }

        calculateTotalServicesPrice(existingRecord);

        return medicalRecordRepository.save(existingRecord);
    }

    public void deleteMedicalRecord(Long id) {
        MedicalRecord record = getMedicalRecordById(id);
        medicalRecordRepository.delete(record);
    }

    private void calculateTotalServicesPrice(MedicalRecord medicalRecord) {
        BigDecimal total = BigDecimal.ZERO;

        Set<Treatment> treatments = medicalRecord.getRecommendedTreatments();

        if (treatments != null) {
            for (Treatment treatment : treatments) {
                if (treatment.getPrice() != null) {
                    total = total.add(treatment.getPrice());
                }
            }
        }

        medicalRecord.setTotalServicesPrice(total);
    }
}