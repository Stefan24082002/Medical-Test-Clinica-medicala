package com.example.clinic.service;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Role;
import com.example.clinic.entity.User;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.MedicalRecordRepository;
import com.example.clinic.repository.RoleRepository;
import com.example.clinic.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public Doctor createDoctor(Doctor doctor) {
        log.info("Creating doctor with email: {}", doctor.getEmail());

        if (doctor.getEmail() != null
                && !doctor.getEmail().isBlank()
                && doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new DuplicateResourceException("Există deja un doctor cu email-ul: " + doctor.getEmail());
        }

        return doctorRepository.save(doctor);
    }

    public Doctor createDoctorWithAccount(Doctor doctor, String username, String password) {
        log.info("Creating doctor with account: {}", username);

        if (doctor.getEmail() != null
                && !doctor.getEmail().isBlank()
                && doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new DuplicateResourceException("Există deja un doctor cu email-ul: " + doctor.getEmail());
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username-ul este obligatoriu pentru contul doctorului.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Parola este obligatorie pentru contul doctorului.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Există deja un utilizator cu username-ul: " + username);
        }

        if (doctor.getEmail() != null
                && !doctor.getEmail().isBlank()
                && userRepository.existsByEmail(doctor.getEmail())) {
            throw new DuplicateResourceException("Există deja un utilizator cu email-ul: " + doctor.getEmail());
        }

        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseThrow(() -> new ResourceNotFoundException("Rolul ROLE_DOCTOR nu există."));

        Set<Role> roles = new HashSet<>();
        roles.add(doctorRole);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(doctor.getEmail());
        user.setEnabled(true);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        doctor.setUser(savedUser);

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
                .orElseThrow(() -> new ResourceNotFoundException("Doctorul nu a fost găsit cu id-ul: " + id));
    }

    public Doctor getDoctorByUser(User user) {
        log.info("Fetching doctor by user: {}", user.getUsername());

        return doctorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Doctorul nu a fost găsit pentru utilizatorul logat."));
    }

    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        log.info("Updating doctor with id: {}", id);

        Doctor existingDoctor = getDoctorById(id);

        if (updatedDoctor.getEmail() != null
                && !updatedDoctor.getEmail().isBlank()
                && !updatedDoctor.getEmail().equals(existingDoctor.getEmail())
                && doctorRepository.existsByEmail(updatedDoctor.getEmail())) {
            throw new DuplicateResourceException("Există deja un doctor cu email-ul: " + updatedDoctor.getEmail());
        }

        existingDoctor.setFirstName(updatedDoctor.getFirstName());
        existingDoctor.setLastName(updatedDoctor.getLastName());
        existingDoctor.setSpecialization(updatedDoctor.getSpecialization());
        existingDoctor.setEmail(updatedDoctor.getEmail());
        existingDoctor.setPhone(updatedDoctor.getPhone());
        existingDoctor.setDepartment(updatedDoctor.getDepartment());

        return doctorRepository.save(existingDoctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        log.info("Deleting doctor with id: {}", id);

        Doctor doctor = getDoctorById(id);
        User user = doctor.getUser();

        /*
         * 1. Ștergem fișele medicale ale doctorului.
         * Curățăm întâi serviciile recomandate ca să se șteargă legăturile din medical_record_treatments.
         */
        List<MedicalRecord> records = medicalRecordRepository.findByDoctor(doctor);

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
         * 2. Ștergem programările doctorului.
         */
        List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);
        appointmentRepository.deleteAll(appointments);

        /*
         * 3. Curățăm relațiile din doctor.
         * Important: NU mai facem doctorRepository.save(doctor) după asta.
         */
        if (doctor.getAppointments() != null) {
            doctor.getAppointments().clear();
        }

        if (doctor.getMedicalRecords() != null) {
            doctor.getMedicalRecords().clear();
        }

        if (doctor.getTreatments() != null) {
            doctor.getTreatments().clear();
        }

        doctor.setDepartment(null);
        doctor.setUser(null);

        /*
         * 4. Ștergem doctorul.
         */
        doctorRepository.delete(doctor);

        /*
         * 5. Ștergem și contul userului, dacă doctorul avea cont.
         */
        if (user != null) {
            if (user.getRoles() != null) {
                user.getRoles().clear();
            }

            userRepository.delete(user);
        }
    }
}