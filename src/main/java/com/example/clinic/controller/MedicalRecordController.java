package com.example.clinic.controller;

import com.example.clinic.entity.*;
import com.example.clinic.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
        Page<MedicalRecord> recordPage = medicalRecordService.getAllMedicalRecords(pageable);

        model.addAttribute("recordPage", recordPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "medical-records/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("medicalRecord", new MedicalRecord());
        prepareFormModel(model, "Adaugă fișă medicală");

        return "medical-records/form";
    }

    @PostMapping
    public String createMedicalRecord(
            @Valid @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
            BindingResult bindingResult,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) List<Long> treatmentIds,
            Model model) {

        if (bindingResult.hasErrors()) {
            prepareFormModel(model, "Adaugă fișă medicală");
            return "medical-records/form";
        }

        try {
            setRelations(medicalRecord, appointmentId, patientId, doctorId, treatmentIds);

            medicalRecordService.createMedicalRecord(medicalRecord);

            return "redirect:/admin/medical-records";

        } catch (Exception e) {
            prepareFormModel(model, "Adaugă fișă medicală");
            model.addAttribute("errorMessage", e.getMessage());

            return "medical-records/form";
        }
    }

    @GetMapping("/{id}")
    public String showDetails(@PathVariable Long id, Model model) {
        MedicalRecord record = medicalRecordService.getMedicalRecordById(id);

        model.addAttribute("record", record);

        return "medical-records/details";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        MedicalRecord record = medicalRecordService.getMedicalRecordById(id);

        model.addAttribute("medicalRecord", record);
        prepareEditFormModel(model, record, "Editează fișă medicală");

        return "medical-records/form";
    }

    @PostMapping("/edit/{id}")
    public String updateMedicalRecord(
            @PathVariable Long id,
            @Valid @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
            BindingResult bindingResult,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) List<Long> treatmentIds,
            Model model) {

        if (bindingResult.hasErrors()) {
            prepareFormModel(model, "Editează fișă medicală");
            return "medical-records/form";
        }

        try {
            setRelations(medicalRecord, appointmentId, patientId, doctorId, treatmentIds);

            medicalRecordService.updateMedicalRecord(id, medicalRecord);

            return "redirect:/admin/medical-records";

        } catch (Exception e) {
            prepareFormModel(model, "Editează fișă medicală");
            model.addAttribute("errorMessage", e.getMessage());

            return "medical-records/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicalRecord(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);

        return "redirect:/admin/medical-records";
    }

    private void prepareFormModel(Model model, String pageTitle) {
        List<Appointment> availableAppointments = appointmentService.getAllAppointments().stream()
                .filter(appointment ->
                        appointment.getStatus() == AppointmentStatus.ACCEPTED
                                || appointment.getStatus() == AppointmentStatus.COMPLETED
                )
                .filter(appointment -> !medicalRecordService.existsByAppointment(appointment))
                .toList();

        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("appointments", availableAppointments);
        model.addAttribute("treatments", treatmentService.getAllTreatments());
        model.addAttribute("pageTitle", pageTitle);
    }
    private void prepareEditFormModel(Model model, MedicalRecord currentRecord, String pageTitle) {
        List<Appointment> availableAppointments = appointmentService.getAllAppointments().stream()
                .filter(appointment ->
                        appointment.getStatus() == AppointmentStatus.ACCEPTED
                                || appointment.getStatus() == AppointmentStatus.COMPLETED
                )
                .filter(appointment -> {
                    if (currentRecord.getAppointment() != null
                            && currentRecord.getAppointment().getId().equals(appointment.getId())) {
                        return true;
                    }

                    return !medicalRecordService.existsByAppointment(appointment);
                })
                .toList();

        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("appointments", availableAppointments);
        model.addAttribute("treatments", treatmentService.getAllTreatments());
        model.addAttribute("pageTitle", pageTitle);
    }
    private void setRelations(
            MedicalRecord medicalRecord,
            Long appointmentId,
            Long patientId,
            Long doctorId,
            List<Long> treatmentIds) {

        if (appointmentId != null) {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);

            medicalRecord.setAppointment(appointment);
            medicalRecord.setPatient(appointment.getPatient());
            medicalRecord.setDoctor(appointment.getDoctor());
        } else {
            if (patientId != null) {
                Patient patient = patientService.getPatientById(patientId);
                medicalRecord.setPatient(patient);
            }

            if (doctorId != null) {
                Doctor doctor = doctorService.getDoctorById(doctorId);
                medicalRecord.setDoctor(doctor);
            }
        }

        Set<Treatment> selectedTreatments = new HashSet<>();

        if (treatmentIds != null) {
            for (Long treatmentId : treatmentIds) {
                Treatment treatment = treatmentService.getTreatmentById(treatmentId);
                selectedTreatments.add(treatment);
            }
        }

        medicalRecord.setRecommendedTreatments(selectedTreatments);
    }
}