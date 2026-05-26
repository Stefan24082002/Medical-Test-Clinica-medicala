package com.example.clinic.service;

import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Role;
import com.example.clinic.entity.User;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.MedicalRecordRepository;
import com.example.clinic.repository.RoleRepository;
import com.example.clinic.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void createDoctorWithAccount_shouldCreateDoctorAndUserAccount() {
        Doctor doctor = new Doctor();
        doctor.setFirstName("Ion");
        doctor.setLastName("Bancu");
        doctor.setEmail("ion.bancu@gmail.com");
        doctor.setSpecialization("Ortopedie");

        Role doctorRole = new Role();
        doctorRole.setId(1L);
        doctorRole.setName("ROLE_DOCTOR");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("doctor.ion");
        savedUser.setEmail("ion.bancu@gmail.com");
        savedUser.setPassword("encoded-password");
        savedUser.setEnabled(true);

        when(doctorRepository.existsByEmail("ion.bancu@gmail.com")).thenReturn(false);
        when(userRepository.existsByUsername("doctor.ion")).thenReturn(false);
        when(userRepository.existsByEmail("ion.bancu@gmail.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_DOCTOR")).thenReturn(Optional.of(doctorRole));
        when(passwordEncoder.encode("parola123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Doctor result = doctorService.createDoctorWithAccount(doctor, "doctor.ion", "parola123");

        assertNotNull(result);
        assertNotNull(result.getUser());
        assertEquals("doctor.ion", result.getUser().getUsername());
        assertEquals("ion.bancu@gmail.com", result.getUser().getEmail());

        verify(userRepository, times(1)).save(any(User.class));
        verify(doctorRepository, times(1)).save(doctor);
    }

    @Test
    void createDoctorWithAccount_shouldThrowException_whenDoctorEmailAlreadyExists() {
        Doctor doctor = new Doctor();
        doctor.setEmail("doctor@gmail.com");

        when(doctorRepository.existsByEmail("doctor@gmail.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> doctorService.createDoctorWithAccount(doctor, "doctor.test", "parola123")
        );

        assertEquals("Există deja un doctor cu email-ul: doctor@gmail.com", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void createDoctorWithAccount_shouldThrowException_whenUsernameAlreadyExists() {
        Doctor doctor = new Doctor();
        doctor.setEmail("doctor@gmail.com");

        when(doctorRepository.existsByEmail("doctor@gmail.com")).thenReturn(false);
        when(userRepository.existsByUsername("doctor.test")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> doctorService.createDoctorWithAccount(doctor, "doctor.test", "parola123")
        );

        assertEquals("Există deja un utilizator cu username-ul: doctor.test", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void getDoctorById_shouldReturnDoctor_whenDoctorExists() {
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFirstName("Ștefan");
        doctor.setLastName("Alexandru");

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        Doctor result = doctorService.getDoctorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ștefan", result.getFirstName());
        assertEquals("Alexandru", result.getLastName());
    }

    @Test
    void getDoctorById_shouldThrowException_whenDoctorDoesNotExist() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> doctorService.getDoctorById(99L)
        );

        assertEquals("Doctorul nu a fost găsit cu id-ul: 99", exception.getMessage());
    }

    @Test
    void deleteDoctor_shouldDeleteDoctorAndAssociatedUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("doctor.ion");
        user.setRoles(new HashSet<>());

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFirstName("Ion");
        doctor.setLastName("Bancu");
        doctor.setUser(user);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(medicalRecordRepository.findByDoctor(doctor)).thenReturn(List.of());
        when(appointmentRepository.findByDoctor(doctor)).thenReturn(List.of());

        doctorService.deleteDoctor(1L);

        verify(medicalRecordRepository, times(1)).deleteAll(List.of());
        verify(appointmentRepository, times(1)).deleteAll(List.of());
        verify(doctorRepository, times(1)).delete(doctor);
        verify(userRepository, times(1)).delete(user);
    }
}