package com.example.clinic.service;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.AppointmentStatus;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Patient;
import com.example.clinic.exception.AppointmentConflictException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public Appointment createAppointment(Appointment appointment) {
        log.info("Creating appointment for patient id: {} and doctor id: {}",
                appointment.getPatient() != null ? appointment.getPatient().getId() : null,
                appointment.getDoctor() != null ? appointment.getDoctor().getId() : null);

        validateAppointmentDate(appointment.getAppointmentDate());
        validateAppointmentConflicts(appointment);

        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        log.info("Fetching all appointments");
        return appointmentRepository.findAll();
    }

    public Page<Appointment> getAllAppointments(Pageable pageable) {
        log.info("Fetching appointments with pagination");
        return appointmentRepository.findAll(pageable);
    }

    public Appointment getAppointmentById(Long id) {
        log.info("Fetching appointment with id: {}", id);

        return appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Appointment not found with id: {}", id);
                    return new ResourceNotFoundException("Programarea nu a fost găsită cu id-ul: " + id);
                });
    }

    public List<Appointment> getAppointmentsByPatient(Patient patient) {
        log.info("Fetching appointments for patient id: {}", patient.getId());
        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> getAppointmentsByDoctor(Doctor doctor) {
        log.info("Fetching appointments for doctor id: {}", doctor.getId());
        return appointmentRepository.findByDoctor(doctor);
    }

    public Appointment updateAppointment(Long id, Appointment updatedAppointment) {
        log.info("Updating appointment with id: {}", id);

        Appointment existingAppointment = getAppointmentById(id);

        validateAppointmentDate(updatedAppointment.getAppointmentDate());

        boolean doctorChanged = !existingAppointment.getDoctor().getId()
                .equals(updatedAppointment.getDoctor().getId());

        boolean patientChanged = !existingAppointment.getPatient().getId()
                .equals(updatedAppointment.getPatient().getId());

        boolean dateChanged = !existingAppointment.getAppointmentDate()
                .equals(updatedAppointment.getAppointmentDate());

        if (doctorChanged || patientChanged || dateChanged) {
            validateAppointmentConflicts(updatedAppointment);
        }

        existingAppointment.setAppointmentDate(updatedAppointment.getAppointmentDate());
        existingAppointment.setPatient(updatedAppointment.getPatient());
        existingAppointment.setDoctor(updatedAppointment.getDoctor());
        existingAppointment.setReason(updatedAppointment.getReason());
        existingAppointment.setStatus(updatedAppointment.getStatus());

        return appointmentRepository.save(existingAppointment);
    }

    public Appointment cancelAppointment(Long id) {
        log.info("Cancelling appointment with id: {}", id);

        Appointment appointment = getAppointmentById(id);
        appointment.setStatus(AppointmentStatus.CANCELLED);

        return appointmentRepository.save(appointment);
    }

    public Appointment completeAppointment(Long id) {
        log.info("Completing appointment with id: {}", id);

        Appointment appointment = getAppointmentById(id);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        log.info("Deleting appointment with id: {}", id);

        Appointment appointment = getAppointmentById(id);
        appointmentRepository.delete(appointment);
    }

    private void validateAppointmentDate(LocalDateTime appointmentDate) {
        if (appointmentDate == null) {
            throw new AppointmentConflictException("Data programării este obligatorie.");
        }

        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new AppointmentConflictException("Nu poți crea o programare în trecut.");
        }

        LocalTime time = appointmentDate.toLocalTime();

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);

        if (time.isBefore(start) || !time.isBefore(end)) {
            throw new AppointmentConflictException("Programările se pot face doar între 08:00 și 18:00.");
        }

        if (!(time.getMinute() == 0 || time.getMinute() == 30)) {
            throw new AppointmentConflictException("Programările trebuie să fie din 30 în 30 de minute.");
        }
    }

    private void validateAppointmentConflicts(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        Patient patient = appointment.getPatient();
        LocalDateTime appointmentDate = appointment.getAppointmentDate();

        if (doctor == null) {
            throw new AppointmentConflictException("Doctorul este obligatoriu pentru programare.");
        }

        if (patient == null) {
            throw new AppointmentConflictException("Pacientul este obligatoriu pentru programare.");
        }

        if (appointmentRepository.existsByDoctorAndAppointmentDate(doctor, appointmentDate)) {
            log.error("Doctor already has appointment at: {}", appointmentDate);
            throw new AppointmentConflictException("Doctorul are deja o programare la această oră.");
        }

        if (appointmentRepository.existsByPatientAndAppointmentDate(patient, appointmentDate)) {
            log.error("Patient already has appointment at: {}", appointmentDate);
            throw new AppointmentConflictException("Pacientul are deja o programare la această oră.");
        }
    }
}