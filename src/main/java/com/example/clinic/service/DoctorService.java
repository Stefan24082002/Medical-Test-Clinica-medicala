package com.example.clinic.service;

import com.example.clinic.entity.Doctor;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Doctor createDoctor(Doctor doctor) {
        log.info("Creating doctor with email: {}", doctor.getEmail());

        if (doctor.getEmail() != null && doctorRepository.existsByEmail(doctor.getEmail())) {
            log.error("Doctor already exists with email: {}", doctor.getEmail());
            throw new DuplicateResourceException("Există deja un doctor cu email-ul: " + doctor.getEmail());
        }

        return doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        log.info("Fetching all doctors");
        return doctorRepository.findAll();
    }

    public Page<Doctor> getAllDoctors(Pageable pageable) {
        log.info("Fetching doctors with pagination");
        return doctorRepository.findAll(pageable);
    }

    public Doctor getDoctorById(Long id) {
        log.info("Fetching doctor with id: {}", id);

        return doctorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Doctor not found with id: {}", id);
                    return new ResourceNotFoundException("Doctorul nu a fost găsit cu id-ul: " + id);
                });
    }

    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        log.info("Updating doctor with id: {}", id);

        Doctor existingDoctor = getDoctorById(id);

        if (updatedDoctor.getEmail() != null
                && !updatedDoctor.getEmail().equals(existingDoctor.getEmail())
                && doctorRepository.existsByEmail(updatedDoctor.getEmail())) {
            log.error("Doctor already exists with email: {}", updatedDoctor.getEmail());
            throw new DuplicateResourceException("Există deja un doctor cu email-ul: " + updatedDoctor.getEmail());
        }

        existingDoctor.setFirstName(updatedDoctor.getFirstName());
        existingDoctor.setLastName(updatedDoctor.getLastName());
        existingDoctor.setSpecialization(updatedDoctor.getSpecialization());
        existingDoctor.setEmail(updatedDoctor.getEmail());
        existingDoctor.setPhone(updatedDoctor.getPhone());
        existingDoctor.setDepartment(updatedDoctor.getDepartment());
        existingDoctor.setUser(updatedDoctor.getUser());
        existingDoctor.setTreatments(updatedDoctor.getTreatments());

        return doctorRepository.save(existingDoctor);
    }

    public void deleteDoctor(Long id) {
        log.info("Deleting doctor with id: {}", id);

        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }
}