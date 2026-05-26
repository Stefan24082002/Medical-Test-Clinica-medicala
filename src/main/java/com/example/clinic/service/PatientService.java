package com.example.clinic.service;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Patient;
import com.example.clinic.entity.User;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.MedicalRecordRepository;
import com.example.clinic.repository.PatientRepository;
import com.example.clinic.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public Patient createPatient(Patient patient) {
        log.info("Creating patient with email: {}", patient.getEmail());

        if (patient.getEmail() != null
                && !patient.getEmail().isBlank()
                && patientRepository.existsByEmail(patient.getEmail())) {
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
                .orElseThrow(() -> new ResourceNotFoundException("Pacientul nu a fost găsit cu id-ul: " + id));
    }

    public Patient getPatientByUser(User user) {
        log.info("Fetching patient by user: {}", user.getUsername());

        return patientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Pacientul nu a fost găsit pentru utilizatorul logat."));
    }

    public Patient getPatientByUsername(String username) {
        log.info("Fetching patient by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit: " + username));

        return getPatientByUser(user);
    }

    public Patient updatePatient(Long id, Patient updatedPatient) {
        log.info("Updating patient with id: {}", id);

        Patient existingPatient = getPatientById(id);

        if (updatedPatient.getEmail() != null
                && !updatedPatient.getEmail().isBlank()
                && !updatedPatient.getEmail().equals(existingPatient.getEmail())
                && patientRepository.existsByEmail(updatedPatient.getEmail())) {
            throw new DuplicateResourceException("Există deja un pacient cu email-ul: " + updatedPatient.getEmail());
        }

        existingPatient.setFirstName(updatedPatient.getFirstName());
        existingPatient.setLastName(updatedPatient.getLastName());
        existingPatient.setEmail(updatedPatient.getEmail());
        existingPatient.setPhone(updatedPatient.getPhone());

        /*
         * Dacă entitatea ta Patient are și address / birthDate,
         * liniile de mai jos merg doar dacă ai aceste câmpuri în Patient.java.
         */
        existingPatient.setAddress(updatedPatient.getAddress());
        existingPatient.setBirthDate(updatedPatient.getBirthDate());

        return patientRepository.save(existingPatient);
    }

    @Transactional
    public void deletePatient(Long id) {
        log.info("Deleting patient with id: {}", id);

        Patient patient = getPatientById(id);
        User user = patient.getUser();

        /*
         * 1. Ștergem fișele medicale ale pacientului.
         * Curățăm întâi serviciile recomandate ca să se șteargă legăturile
         * din medical_record_treatments.
         */
        List<MedicalRecord> records = medicalRecordRepository.findByPatient(patient);

        for (MedicalRecord record : records) {
            if (record.getRecommendedTreatments() != null) {
                record.getRecommendedTreatments().clear();
            }

            record.setAppointment(null);
            record.setPatient(null);
            record.setDoctor(null);
        }

        medicalRecordRepository.deleteAll(records);

        /*
         * 2. Ștergem programările pacientului.
         */
        List<Appointment> appointments = appointmentRepository.findByPatient(patient);
        appointmentRepository.deleteAll(appointments);

        /*
         * 3. Curățăm relațiile din patient.
         * Important: NU facem patientRepository.save(patient) după asta.
         */
        if (patient.getAppointments() != null) {
            patient.getAppointments().clear();
        }

        if (patient.getMedicalRecords() != null) {
            patient.getMedicalRecords().clear();
        }

        patient.setUser(null);

        /*
         * 4. Ștergem pacientul.
         */
        patientRepository.delete(patient);

        /*
         * 5. Ștergem și contul userului, dacă pacientul avea cont.
         */
        if (user != null) {
            if (user.getRoles() != null) {
                user.getRoles().clear();
            }

            userRepository.delete(user);
        }
    }
}