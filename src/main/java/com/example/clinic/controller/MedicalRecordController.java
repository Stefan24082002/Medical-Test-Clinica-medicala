package com.example.clinic.controller;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Treatment;
import com.example.clinic.service.AppointmentService;
import com.example.clinic.service.DoctorService;
import com.example.clinic.service.MedicalRecordService;
import com.example.clinic.service.PatientService;
import com.example.clinic.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final TreatmentService treatmentService;

    @GetMapping
    public String listMedicalRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "recordDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MedicalRecord> medicalRecordPage = medicalRecordService.getAllMedicalRecords(pageable);

        model.addAttribute("medicalRecordPage", medicalRecordPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "medical-records/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setRecordDate(LocalDate.now());

        model.addAttribute("medicalRecord", medicalRecord);
        model.addAttribute("pageTitle", "Adaugă fișă medicală");
        addFormData(model);

        return "medical-records/form";
    }

    @PostMapping
    public String createMedicalRecord(
            @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) List<Long> treatmentIds,
            Model model) {

        try {
            if (medicalRecord.getDiagnosis() == null || medicalRecord.getDiagnosis().isBlank()) {
                throw new RuntimeException("Diagnosticul este obligatoriu.");
            }

            if (appointmentId != null) {
                Appointment appointment = appointmentService.getAppointmentById(appointmentId);

                medicalRecord.setAppointment(appointment);
                medicalRecord.setRecordDate(appointment.getAppointmentDate().toLocalDate());
                medicalRecord.setPatient(appointment.getPatient());
                medicalRecord.setDoctor(appointment.getDoctor());
            } else {
                if (medicalRecord.getRecordDate() == null) {
                    medicalRecord.setRecordDate(LocalDate.now());
                }

                if (patientId == null) {
                    throw new RuntimeException("Pacientul este obligatoriu.");
                }

                medicalRecord.setPatient(patientService.getPatientById(patientId));

                if (doctorId != null) {
                    medicalRecord.setDoctor(doctorService.getDoctorById(doctorId));
                }
            }

            medicalRecord.setRecommendedTreatments(getSelectedTreatments(treatmentIds));

            medicalRecordService.createMedicalRecord(medicalRecord);

            return "redirect:/admin/medical-records";

        } catch (Exception e) {
            model.addAttribute("medicalRecord", medicalRecord);
            model.addAttribute("pageTitle", "Adaugă fișă medicală");
            model.addAttribute("errorMessage", e.getMessage());
            addFormData(model);
            return "medical-records/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordById(id);

        model.addAttribute("medicalRecord", medicalRecord);
        model.addAttribute("pageTitle", "Editează fișă medicală");
        addFormData(model);

        return "medical-records/form";
    }

    @PostMapping("/edit/{id}")
    public String updateMedicalRecord(
            @PathVariable Long id,
            @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) List<Long> treatmentIds,
            Model model) {

        try {
            if (medicalRecord.getDiagnosis() == null || medicalRecord.getDiagnosis().isBlank()) {
                throw new RuntimeException("Diagnosticul este obligatoriu.");
            }

            if (appointmentId != null) {
                Appointment appointment = appointmentService.getAppointmentById(appointmentId);

                medicalRecord.setAppointment(appointment);
                medicalRecord.setRecordDate(appointment.getAppointmentDate().toLocalDate());
                medicalRecord.setPatient(appointment.getPatient());
                medicalRecord.setDoctor(appointment.getDoctor());
            } else {
                if (medicalRecord.getRecordDate() == null) {
                    medicalRecord.setRecordDate(LocalDate.now());
                }

                if (patientId == null) {
                    throw new RuntimeException("Pacientul este obligatoriu.");
                }

                medicalRecord.setPatient(patientService.getPatientById(patientId));

                if (doctorId != null) {
                    medicalRecord.setDoctor(doctorService.getDoctorById(doctorId));
                }
            }

            medicalRecord.setRecommendedTreatments(getSelectedTreatments(treatmentIds));

            medicalRecordService.updateMedicalRecord(id, medicalRecord);

            return "redirect:/admin/medical-records";

        } catch (Exception e) {
            model.addAttribute("medicalRecord", medicalRecord);
            model.addAttribute("pageTitle", "Editează fișă medicală");
            model.addAttribute("errorMessage", e.getMessage());
            addFormData(model);
            return "medical-records/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicalRecord(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);
        return "redirect:/admin/medical-records";
    }

    private void addFormData(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        model.addAttribute("treatments", treatmentService.getAllTreatments());
    }

    private Set<Treatment> getSelectedTreatments(List<Long> treatmentIds) {
        Set<Treatment> selectedTreatments = new HashSet<>();

        if (treatmentIds != null) {
            for (Long treatmentId : treatmentIds) {
                selectedTreatments.add(treatmentService.getTreatmentById(treatmentId));
            }
        }

        return selectedTreatments;
    }
}