package com.example.clinic.service;

import com.example.clinic.entity.Treatment;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;

    public Treatment createTreatment(Treatment treatment) {
        log.info("Creating treatment with name: {}", treatment.getName());

        if (treatmentRepository.existsByName(treatment.getName())) {
            throw new DuplicateResourceException("Există deja un serviciu medical cu numele: " + treatment.getName());
        }

        return treatmentRepository.save(treatment);
    }

    public List<Treatment> getAllTreatments() {
        log.info("Fetching all treatments");
        return treatmentRepository.findAll();
    }

    public Page<Treatment> getAllTreatments(Pageable pageable) {
        log.info("Fetching treatments with pagination");
        return treatmentRepository.findAll(pageable);
    }

    public Treatment getTreatmentById(Long id) {
        log.info("Fetching treatment with id: {}", id);

        return treatmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviciul medical nu a fost găsit cu id-ul: " + id));
    }

    public Treatment updateTreatment(Long id, Treatment updatedTreatment) {
        log.info("Updating treatment with id: {}", id);

        Treatment existingTreatment = getTreatmentById(id);

        if (!existingTreatment.getName().equals(updatedTreatment.getName())
                && treatmentRepository.existsByName(updatedTreatment.getName())) {
            throw new DuplicateResourceException("Există deja un serviciu medical cu numele: " + updatedTreatment.getName());
        }

        existingTreatment.setName(updatedTreatment.getName());
        existingTreatment.setDescription(updatedTreatment.getDescription());
        existingTreatment.setPrice(updatedTreatment.getPrice());
        existingTreatment.setDepartment(updatedTreatment.getDepartment());

        return treatmentRepository.save(existingTreatment);
    }

    public void deleteTreatment(Long id) {
        log.info("Deleting treatment with id: {}", id);

        Treatment treatment = getTreatmentById(id);
        treatmentRepository.delete(treatment);
    }
}