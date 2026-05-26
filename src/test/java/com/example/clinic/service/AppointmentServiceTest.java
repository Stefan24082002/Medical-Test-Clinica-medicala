package com.example.clinic.service;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.AppointmentStatus;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Patient;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void createAppointment_shouldSaveAppointmentWithScheduledStatus() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("Ștefan");
        patient.setLastName("Săndulache");

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFirstName("Ion");
        doctor.setLastName("Bancu");

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(LocalDateTime.of(2026, 5, 27, 10, 30));
        appointment.setReason("Consultație");
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.createAppointment(appointment);

        assertNotNull(result);
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
        assertEquals("Consultație", result.getReason());
        assertEquals(patient, result.getPatient());
        assertEquals(doctor, result.getDoctor());

        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    void getAppointmentById_shouldReturnAppointment_whenAppointmentExists() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setReason("Control");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        Appointment result = appointmentService.getAppointmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Control", result.getReason());
    }

    @Test
    void getAppointmentById_shouldThrowException_whenAppointmentDoesNotExist() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.getAppointmentById(99L)
        );

        assertEquals("Programarea nu a fost găsită cu id-ul: 99", exception.getMessage());
    }

    @Test
    void getAppointmentsByPatient_shouldReturnPatientAppointments() {
        Patient patient = new Patient();
        patient.setId(1L);

        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        appointment1.setPatient(patient);

        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        appointment2.setPatient(patient);

        when(appointmentRepository.findByPatient(patient))
                .thenReturn(List.of(appointment1, appointment2));

        List<Appointment> result = appointmentService.getAppointmentsByPatient(patient);

        assertEquals(2, result.size());
        assertEquals(patient, result.get(0).getPatient());
        assertEquals(patient, result.get(1).getPatient());

        verify(appointmentRepository, times(1)).findByPatient(patient);
    }

    @Test
    void getAppointmentsByDoctor_shouldReturnDoctorAppointments() {
        Doctor doctor = new Doctor();
        doctor.setId(1L);

        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        appointment1.setDoctor(doctor);

        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        appointment2.setDoctor(doctor);

        when(appointmentRepository.findByDoctor(doctor))
                .thenReturn(List.of(appointment1, appointment2));

        List<Appointment> result = appointmentService.getAppointmentsByDoctor(doctor);

        assertEquals(2, result.size());
        assertEquals(doctor, result.get(0).getDoctor());
        assertEquals(doctor, result.get(1).getDoctor());

        verify(appointmentRepository, times(1)).findByDoctor(doctor);
    }

    @Test
    void updateAppointment_shouldUpdateAppointmentData() {
        Patient patient = new Patient();
        patient.setId(1L);

        Doctor doctor = new Doctor();
        doctor.setId(1L);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setId(1L);
        existingAppointment.setReason("Motiv vechi");
        existingAppointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment updatedAppointment = new Appointment();
        updatedAppointment.setPatient(patient);
        updatedAppointment.setDoctor(doctor);
        updatedAppointment.setAppointmentDate(LocalDateTime.of(2026, 5, 28, 12, 0));
        updatedAppointment.setReason("Motiv nou");
        updatedAppointment.setStatus(AppointmentStatus.ACCEPTED);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(existingAppointment));

        when(appointmentRepository.save(existingAppointment))
                .thenReturn(existingAppointment);

        Appointment result = appointmentService.updateAppointment(1L, updatedAppointment);

        assertEquals("Motiv nou", result.getReason());
        assertEquals(AppointmentStatus.ACCEPTED, result.getStatus());
        assertEquals(patient, result.getPatient());
        assertEquals(doctor, result.getDoctor());
        assertEquals(LocalDateTime.of(2026, 5, 28, 12, 0), result.getAppointmentDate());

        verify(appointmentRepository, times(1)).save(existingAppointment);
    }

    @Test
    void cancelAppointment_shouldSetStatusToCancelled() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(appointment))
                .thenReturn(appointment);

        Appointment result = appointmentService.cancelAppointment(1L);

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());

        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    void deleteAppointment_shouldDeleteAppointment_whenAppointmentExists() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        appointmentService.deleteAppointment(1L);

        verify(appointmentRepository, times(1)).delete(appointment);
    }
}