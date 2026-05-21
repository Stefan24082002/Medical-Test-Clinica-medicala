package com.example.clinic.service;

import com.example.clinic.entity.Patient;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;

    public Patient createPatient(Patient patient) {
        log.info("Creating patient with email: {}", patient.getEmail());

        if (patient.getEmail() != null && patientRepository.existsByEmail(patient.getEmail())) {
            log.error("Patient already exists with email: {}", patient.getEmail());
            throw new DuplicateResourceException("Există deja un pacient cu email-ul: " + patient.getEmail());
        }

        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        log.info("Fetching all patients");
        return patientRepository.findAll();
    }

    public Page<Patient> getAllPatients(Pageable pageable) {
        log.info("Fetching patients with pagination");
        return patientRepository.findAll(pageable);
    }

    public Patient getPatientById(Long id) {
        log.info("Fetching patient with id: {}", id);

        return patientRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Patient not found with id: {}", id);
                    return new ResourceNotFoundException("Pacientul nu a fost găsit cu id-ul: " + id);
                });
    }

    public Patient updatePatient(Long id, Patient updatedPatient) {
        log.info("Updating patient with id: {}", id);

        Patient existingPatient = getPatientById(id);

        if (updatedPatient.getEmail() != null
                && !updatedPatient.getEmail().equals(existingPatient.getEmail())
                && patientRepository.existsByEmail(updatedPatient.getEmail())) {
            log.error("Patient already exists with email: {}", updatedPatient.getEmail());
            throw new DuplicateResourceException("Există deja un pacient cu email-ul: " + updatedPatient.getEmail());
        }

        existingPatient.setFirstName(updatedPatient.getFirstName());
        existingPatient.setLastName(updatedPatient.getLastName());
        existingPatient.setBirthDate(updatedPatient.getBirthDate());
        existingPatient.setEmail(updatedPatient.getEmail());
        existingPatient.setPhone(updatedPatient.getPhone());
        existingPatient.setAddress(updatedPatient.getAddress());
        existingPatient.setUser(updatedPatient.getUser());

        return patientRepository.save(existingPatient);
    }

    public void deletePatient(Long id) {
        log.info("Deleting patient with id: {}", id);

        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }
}