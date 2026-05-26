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
        log.info("Creating appointment");

        validateAppointment(appointment);

        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.SCHEDULED);
        }

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
                .orElseThrow(() -> new ResourceNotFoundException("Programarea nu a fost găsită cu id-ul: " + id));
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

        if (existingAppointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppointmentConflictException("Nu poți edita o programare finalizată.");
        }

        if (existingAppointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentConflictException("Nu poți edita o programare anulată.");
        }

        if (existingAppointment.getStatus() == AppointmentStatus.REJECTED) {
            throw new AppointmentConflictException("Nu poți edita o programare refuzată.");
        }

        existingAppointment.setPatient(updatedAppointment.getPatient());
        existingAppointment.setDoctor(updatedAppointment.getDoctor());
        existingAppointment.setAppointmentDate(updatedAppointment.getAppointmentDate());
        existingAppointment.setReason(updatedAppointment.getReason());

        if (updatedAppointment.getStatus() != null) {
            existingAppointment.setStatus(updatedAppointment.getStatus());
        }

        validateAppointmentForUpdate(id, existingAppointment);

        return appointmentRepository.save(existingAppointment);
    }

    public void acceptAppointment(Long id) {
        log.info("Accepting appointment with id: {}", id);

        Appointment appointment = getAppointmentById(id);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AppointmentConflictException("Doar programările în așteptare pot fi acceptate.");
        }

        appointment.setStatus(AppointmentStatus.ACCEPTED);
        appointmentRepository.save(appointment);
    }

    public void rejectAppointment(Long id) {
        log.info("Rejecting appointment with id: {}", id);

        Appointment appointment = getAppointmentById(id);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AppointmentConflictException("Doar programările în așteptare pot fi refuzate.");
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointmentRepository.save(appointment);
    }

    public Appointment cancelAppointment(Long id) {
        log.info("Cancelling appointment with id: {}", id);

        Appointment appointment = getAppointmentById(id);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppointmentConflictException("Nu poți anula o programare finalizată.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentConflictException("Programarea este deja anulată.");
        }

        if (appointment.getStatus() == AppointmentStatus.REJECTED) {
            throw new AppointmentConflictException("Nu poți anula o programare refuzată.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        return appointmentRepository.save(appointment);
    }
    public void completeAppointment(Long id) {
        log.info("Completing appointment with id: {}", id);

        Appointment appointment = getAppointmentById(id);

        if (appointment.getStatus() != AppointmentStatus.ACCEPTED) {
            throw new AppointmentConflictException("Doar programările acceptate pot fi finalizate.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        log.info("Deleting appointment with id: {}", id);

        Appointment appointment = getAppointmentById(id);
        appointmentRepository.delete(appointment);
    }

    private void validateAppointment(Appointment appointment) {
        if (appointment.getPatient() == null) {
            throw new AppointmentConflictException("Pacientul este obligatoriu.");
        }

        if (appointment.getDoctor() == null) {
            throw new AppointmentConflictException("Doctorul este obligatoriu.");
        }

        validateAppointmentDate(appointment.getAppointmentDate());

        if (appointmentRepository.existsByDoctorAndAppointmentDate(
                appointment.getDoctor(),
                appointment.getAppointmentDate())) {

            throw new AppointmentConflictException(
                    "Doctorul are deja o programare la această dată și oră."
            );
        }
    }

    private void validateAppointmentForUpdate(Long appointmentId, Appointment appointment) {
        if (appointment.getPatient() == null) {
            throw new AppointmentConflictException("Pacientul este obligatoriu.");
        }

        if (appointment.getDoctor() == null) {
            throw new AppointmentConflictException("Doctorul este obligatoriu.");
        }

        validateAppointmentDate(appointment.getAppointmentDate());

        List<Appointment> doctorAppointments =
                appointmentRepository.findByDoctor(appointment.getDoctor());

        boolean conflictExists = doctorAppointments.stream()
                .anyMatch(existingAppointment ->
                        !existingAppointment.getId().equals(appointmentId)
                                && existingAppointment.getAppointmentDate().equals(appointment.getAppointmentDate())
                );

        if (conflictExists) {
            throw new AppointmentConflictException(
                    "Doctorul are deja o altă programare la această dată și oră."
            );
        }
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
}