package com.example.clinic.service;

import com.example.clinic.entity.Department;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Treatment;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.DepartmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.MedicalRecordRepository;
import com.example.clinic.repository.TreatmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final TreatmentRepository treatmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public Department createDepartment(Department department) {
        log.info("Creating department with name: {}", department.getName());

        if (departmentRepository.existsByName(department.getName())) {
            throw new DuplicateResourceException("Există deja un departament cu numele: " + department.getName());
        }

        return departmentRepository.save(department);
    }

    public List<Department> getAllDepartments() {
        log.info("Fetching all departments");
        return departmentRepository.findAll();
    }

    public Page<Department> getAllDepartments(Pageable pageable) {
        log.info("Fetching departments with pagination");
        return departmentRepository.findAll(pageable);
    }

    public Department getDepartmentById(Long id) {
        log.info("Fetching department with id: {}", id);

        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamentul nu a fost găsit cu id-ul: " + id));
    }

    public Department updateDepartment(Long id, Department updatedDepartment) {
        log.info("Updating department with id: {}", id);

        Department existingDepartment = getDepartmentById(id);

        if (!existingDepartment.getName().equals(updatedDepartment.getName())
                && departmentRepository.existsByName(updatedDepartment.getName())) {
            throw new DuplicateResourceException("Există deja un departament cu numele: " + updatedDepartment.getName());
        }

        existingDepartment.setName(updatedDepartment.getName());
        existingDepartment.setDescription(updatedDepartment.getDescription());

        return departmentRepository.save(existingDepartment);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department with id: {}", id);

        Department department = getDepartmentById(id);

        /*
         * 1. Scoatem departamentul de la doctori.
         * Doctorii NU se șterg, doar rămân fără departament.
         */
        List<Doctor> doctorsInDepartment = doctorRepository.findByDepartment(department);

        for (Doctor doctor : doctorsInDepartment) {
            doctor.setDepartment(null);
        }

        doctorRepository.saveAll(doctorsInDepartment);

        /*
         * 2. Luăm serviciile medicale care aparțin departamentului.
         * Acestea VOR FI ȘTERSE.
         */
        List<Treatment> treatmentsInDepartment = treatmentRepository.findByDepartment(department);

        /*
         * 3. Scoatem serviciile din legăturile cu doctorii.
         * Asta curăță tabela doctor_treatments.
         */
        List<Doctor> allDoctors = doctorRepository.findAll();

        for (Doctor doctor : allDoctors) {
            if (doctor.getTreatments() != null) {
                doctor.getTreatments().removeAll(treatmentsInDepartment);
            }
        }

        doctorRepository.saveAll(allDoctors);

        /*
         * 4. Scoatem serviciile din fișele medicale.
         * Asta curăță tabela medical_record_treatments.
         * După aceea recalculăm totalul fiecărei fișe.
         */
        List<MedicalRecord> allRecords = medicalRecordRepository.findAll();

        for (MedicalRecord record : allRecords) {
            if (record.getRecommendedTreatments() != null) {
                boolean changed = record.getRecommendedTreatments().removeAll(treatmentsInDepartment);

                if (changed) {
                    BigDecimal total = BigDecimal.ZERO;

                    for (Treatment treatment : record.getRecommendedTreatments()) {
                        if (treatment.getPrice() != null) {
                            total = total.add(treatment.getPrice());
                        }
                    }

                    record.setTotalServicesPrice(total);
                }
            }
        }

        medicalRecordRepository.saveAll(allRecords);

        /*
         * 5. Ștergem serviciile medicale ale departamentului.
         */
        treatmentRepository.deleteAll(treatmentsInDepartment);

        /*
         * 6. Ștergem departamentul.
         */
        departmentRepository.delete(department);
    }
}