package com.example.clinic.service;

import com.example.clinic.entity.Patient;
import com.example.clinic.entity.User;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.MedicalRecordRepository;
import com.example.clinic.repository.PatientRepository;
import com.example.clinic.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void createPatient_shouldCreatePatient_whenEmailIsUnique() {
        Patient patient = new Patient();
        patient.setFirstName("Alex");
        patient.setLastName("Dan");
        patient.setEmail("alex@gmail.com");
        patient.setPhone("0766777888");

        when(patientRepository.existsByEmail("alex@gmail.com")).thenReturn(false);
        when(patientRepository.save(patient)).thenReturn(patient);

        Patient result = patientService.createPatient(patient);

        assertNotNull(result);
        assertEquals("Alex", result.getFirstName());
        assertEquals("Dan", result.getLastName());
        assertEquals("alex@gmail.com", result.getEmail());

        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void createPatient_shouldThrowException_whenEmailAlreadyExists() {
        Patient patient = new Patient();
        patient.setEmail("alex@gmail.com");

        when(patientRepository.existsByEmail("alex@gmail.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> patientService.createPatient(patient)
        );

        assertEquals("Există deja un pacient cu email-ul: alex@gmail.com", exception.getMessage());

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void getPatientById_shouldReturnPatient_whenPatientExists() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("Ștefan");
        patient.setLastName("Săndulache");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ștefan", result.getFirstName());
        assertEquals("Săndulache", result.getLastName());
    }

    @Test
    void getPatientById_shouldThrowException_whenPatientDoesNotExist() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.getPatientById(99L)
        );

        assertEquals("Pacientul nu a fost găsit cu id-ul: 99", exception.getMessage());
    }

    @Test
    void getPatientByUsername_shouldReturnPatient_whenUserAndPatientExist() {
        User user = new User();
        user.setId(1L);
        user.setUsername("stefan");

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("Ștefan");
        patient.setLastName("Săndulache");
        patient.setUser(user);

        when(userRepository.findByUsername("stefan")).thenReturn(Optional.of(user));
        when(patientRepository.findByUser(user)).thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientByUsername("stefan");

        assertNotNull(result);
        assertEquals("Ștefan", result.getFirstName());
        assertEquals("Săndulache", result.getLastName());

        verify(userRepository, times(1)).findByUsername("stefan");
        verify(patientRepository, times(1)).findByUser(user);
    }

    @Test
    void getPatientByUsername_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.getPatientByUsername("unknown")
        );

        assertEquals("Utilizatorul nu a fost găsit: unknown", exception.getMessage());

        verify(patientRepository, never()).findByUser(any(User.class));
    }

    @Test
    void updatePatient_shouldUpdatePatientData() {
        Patient existingPatient = new Patient();
        existingPatient.setId(1L);
        existingPatient.setFirstName("Alex");
        existingPatient.setLastName("Dan");
        existingPatient.setEmail("alex@gmail.com");
        existingPatient.setPhone("0700000000");

        Patient updatedPatient = new Patient();
        updatedPatient.setFirstName("Alexandru");
        updatedPatient.setLastName("Dan");
        updatedPatient.setEmail("alexandru@gmail.com");
        updatedPatient.setPhone("0711111111");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.existsByEmail("alexandru@gmail.com")).thenReturn(false);
        when(patientRepository.save(existingPatient)).thenReturn(existingPatient);

        Patient result = patientService.updatePatient(1L, updatedPatient);

        assertEquals("Alexandru", result.getFirstName());
        assertEquals("Dan", result.getLastName());
        assertEquals("alexandru@gmail.com", result.getEmail());
        assertEquals("0711111111", result.getPhone());

        verify(patientRepository, times(1)).save(existingPatient);
    }

    @Test
    void deletePatient_shouldDeletePatientAndAssociatedUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("patient.alex");
        user.setRoles(new HashSet<>());

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("Alex");
        patient.setLastName("Dan");
        patient.setUser(user);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(medicalRecordRepository.findByPatient(patient)).thenReturn(List.of());
        when(appointmentRepository.findByPatient(patient)).thenReturn(List.of());

        patientService.deletePatient(1L);

        verify(medicalRecordRepository, times(1)).deleteAll(List.of());
        verify(appointmentRepository, times(1)).deleteAll(List.of());
        verify(patientRepository, times(1)).delete(patient);
        verify(userRepository, times(1)).delete(user);
    }
}