package com.example.clinic.service;

import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Patient;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        log.info("Creating medical record for patient id: {}",
                medicalRecord.getPatient() != null ? medicalRecord.getPatient().getId() : null);

        if (medicalRecord.getRecordDate() == null) {
            medicalRecord.setRecordDate(LocalDate.now());
        }

        if (medicalRecord.getAppointment() != null
                && medicalRecord.getAppointment().getMedicalRecord() != null) {
            throw new DuplicateResourceException("Această programare are deja o fișă medicală.");
        }

        return medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getAllMedicalRecords() {
        log.info("Fetching all medical records");
        return medicalRecordRepository.findAll();
    }

    public Page<MedicalRecord> getAllMedicalRecords(Pageable pageable) {
        log.info("Fetching medical records with pagination");
        return medicalRecordRepository.findAll(pageable);
    }

    public MedicalRecord getMedicalRecordById(Long id) {
        log.info("Fetching medical record with id: {}", id);

        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Medical record not found with id: {}", id);
                    return new ResourceNotFoundException("Fișa medicală nu a fost găsită cu id-ul: " + id);
                });
    }

    public List<MedicalRecord> getMedicalRecordsByPatient(Patient patient) {
        log.info("Fetching medical records for patient id: {}", patient.getId());
        return medicalRecordRepository.findByPatient(patient);
    }

    public List<MedicalRecord> getMedicalRecordsByDoctor(Doctor doctor) {
        log.info("Fetching medical records for doctor id: {}", doctor.getId());
        return medicalRecordRepository.findByDoctor(doctor);
    }

    public MedicalRecord updateMedicalRecord(Long id, MedicalRecord updatedMedicalRecord) {
        log.info("Updating medical record with id: {}", id);

        MedicalRecord existingMedicalRecord = getMedicalRecordById(id);

        existingMedicalRecord.setRecordDate(updatedMedicalRecord.getRecordDate());
        existingMedicalRecord.setDiagnosis(updatedMedicalRecord.getDiagnosis());
        existingMedicalRecord.setPrescription(updatedMedicalRecord.getPrescription());
        existingMedicalRecord.setNotes(updatedMedicalRecord.getNotes());
        existingMedicalRecord.setPatient(updatedMedicalRecord.getPatient());
        existingMedicalRecord.setDoctor(updatedMedicalRecord.getDoctor());
        existingMedicalRecord.setAppointment(updatedMedicalRecord.getAppointment());
        existingMedicalRecord.setRecommendedTreatments(updatedMedicalRecord.getRecommendedTreatments());

        return medicalRecordRepository.save(existingMedicalRecord);
    }

    public void deleteMedicalRecord(Long id) {
        log.info("Deleting medical record with id: {}", id);

        MedicalRecord medicalRecord = getMedicalRecordById(id);
        medicalRecordRepository.delete(medicalRecord);
    }
}