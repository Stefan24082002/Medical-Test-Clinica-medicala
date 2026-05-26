package com.example.clinic.controller;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.AppointmentStatus;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Patient;
import com.example.clinic.service.AppointmentService;
import com.example.clinic.service.DoctorService;
import com.example.clinic.service.MedicalRecordService;
import com.example.clinic.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/patient")
public class PatientPortalController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/appointments")
    public String myAppointments(Authentication authentication, Model model) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());

        model.addAttribute("appointments", appointmentService.getAppointmentsByPatient(patient));

        return "patients/appointments";
    }

    @GetMapping("/appointments/new")
    public String showAppointmentForm(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("timeSlots", generateTimeSlots());

        return "patients/appointment-form";
    }

    @PostMapping("/appointments")
    public String createAppointment(
            Authentication authentication,
            @RequestParam Long doctorId,
            @RequestParam String appointmentDateInput,
            @RequestParam String appointmentTimeInput,
            @RequestParam(required = false) String reason,
            Model model) {

        try {
            Patient patient = patientService.getPatientByUsername(authentication.getName());
            Doctor doctor = doctorService.getDoctorById(doctorId);

            LocalDate date = LocalDate.parse(appointmentDateInput);
            LocalTime time = LocalTime.parse(appointmentTimeInput);
            LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(appointmentDateTime);
            appointment.setReason(reason);
            appointment.setStatus(AppointmentStatus.SCHEDULED);

            appointmentService.createAppointment(appointment);

            return "redirect:/patient/appointments";

        } catch (Exception e) {
            model.addAttribute("doctors", doctorService.getAllDoctors());
            model.addAttribute("timeSlots", generateTimeSlots());
            model.addAttribute("errorMessage", e.getMessage());

            return "patients/appointment-form";
        }
    }

    @GetMapping("/appointments/cancel/{id}")
    public String cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {

        Patient patient = patientService.getPatientByUsername(authentication.getName());
        Appointment appointment = appointmentService.getAppointmentById(id);

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Nu poți anula programarea altui pacient.");
        }

        appointmentService.cancelAppointment(id);

        return "redirect:/patient/appointments";
    }

    @GetMapping("/medical-records")
    public String myMedicalRecords(Authentication authentication, Model model) {
        Patient patient = patientService.getPatientByUsername(authentication.getName());

        model.addAttribute("records", medicalRecordService.getMedicalRecordsByPatient(patient));

        return "patients/medical-records";
    }

    @GetMapping("/medical-records/{id}")
    public String viewMedicalRecord(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Patient patient = patientService.getPatientByUsername(authentication.getName());
        MedicalRecord record = medicalRecordService.getMedicalRecordById(id);

        if (record.getPatient() == null || !record.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Nu poți vedea fișa medicală a altui pacient.");
        }

        model.addAttribute("record", record);

        return "patients/medical-record-details";
    }

    private List<String> generateTimeSlots() {
        List<String> timeSlots = new ArrayList<>();

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);

        while (start.isBefore(end)) {
            timeSlots.add(start.toString());
            start = start.plusMinutes(30);
        }

        return timeSlots;
    }
}