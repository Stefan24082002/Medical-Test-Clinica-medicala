package com.example.clinic.service;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Treatment;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.MedicalRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @Test
    void createMedicalRecord_shouldCalculateTotalServicesPrice() {
        Treatment treatment1 = new Treatment();
        treatment1.setId(1L);
        treatment1.setName("Consultație cardiologică");
        treatment1.setPrice(new BigDecimal("300.00"));

        Treatment treatment2 = new Treatment();
        treatment2.setId(2L);
        treatment2.setName("EKG");
        treatment2.setPrice(new BigDecimal("200.00"));

        Set<Treatment> treatments = new HashSet<>();
        treatments.add(treatment1);
        treatments.add(treatment2);

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setDiagnosis("Durere în piept");
        medicalRecord.setRecommendedTreatments(treatments);

        when(medicalRecordRepository.save(any(MedicalRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicalRecord savedRecord = medicalRecordService.createMedicalRecord(medicalRecord);

        assertNotNull(savedRecord);
        assertEquals(new BigDecimal("500.00"), savedRecord.getTotalServicesPrice());
        verify(medicalRecordRepository, times(1)).save(medicalRecord);
    }

    @Test
    void createMedicalRecord_shouldThrowException_whenAppointmentAlreadyHasMedicalRecord() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setDiagnosis("Entorsă");
        medicalRecord.setAppointment(appointment);

        when(medicalRecordRepository.existsByAppointment(appointment))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> medicalRecordService.createMedicalRecord(medicalRecord)
        );

        assertEquals("Această programare are deja o fișă medicală.", exception.getMessage());
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }

    @Test
    void getMedicalRecordById_shouldReturnRecord_whenRecordExists() {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);
        medicalRecord.setDiagnosis("Migrenă");

        when(medicalRecordRepository.findById(1L))
                .thenReturn(Optional.of(medicalRecord));

        MedicalRecord result = medicalRecordService.getMedicalRecordById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Migrenă", result.getDiagnosis());
    }

    @Test
    void getMedicalRecordById_shouldThrowException_whenRecordDoesNotExist() {
        when(medicalRecordRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> medicalRecordService.getMedicalRecordById(99L)
        );

        assertEquals("Fișa medicală nu a fost găsită cu id-ul: 99", exception.getMessage());
    }

    @Test
    void updateMedicalRecord_shouldUpdateDiagnosisPrescriptionAndTotal() {
        Treatment treatment = new Treatment();
        treatment.setId(1L);
        treatment.setName("Consultație ortopedică");
        treatment.setPrice(new BigDecimal("250.00"));

        Set<Treatment> treatments = new HashSet<>();
        treatments.add(treatment);

        MedicalRecord existingRecord = new MedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setDiagnosis("Diagnostic vechi");
        existingRecord.setPrescription("Prescripție veche");

        MedicalRecord updatedRecord = new MedicalRecord();
        updatedRecord.setDiagnosis("Entorsă gleznă");
        updatedRecord.setPrescription("Repaus 7 zile");
        updatedRecord.setRecommendedTreatments(treatments);

        when(medicalRecordRepository.findById(1L))
                .thenReturn(Optional.of(existingRecord));

        when(medicalRecordRepository.save(any(MedicalRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicalRecord result = medicalRecordService.updateMedicalRecord(1L, updatedRecord);

        assertEquals("Entorsă gleznă", result.getDiagnosis());
        assertEquals("Repaus 7 zile", result.getPrescription());
        assertEquals(new BigDecimal("250.00"), result.getTotalServicesPrice());
        verify(medicalRecordRepository, times(1)).save(existingRecord);
    }
}