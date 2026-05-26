package com.example.clinic.controller;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.AppointmentStatus;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Patient;
import com.example.clinic.entity.Treatment;
import com.example.clinic.entity.User;
import com.example.clinic.service.AppointmentService;
import com.example.clinic.service.DoctorService;
import com.example.clinic.service.MedicalRecordService;
import com.example.clinic.service.PatientService;
import com.example.clinic.service.TreatmentService;
import com.example.clinic.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/doctor")
public class DoctorPortalController {

    private final UserService userService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final TreatmentService treatmentService;

    @GetMapping("/appointments")
    public String appointments(Model model, Principal principal) {
        Doctor doctor = getLoggedDoctor(principal);

        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctor);
        List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByDoctor(doctor);

        Map<Long, MedicalRecord> recordsByAppointmentId = new HashMap<>();

        for (MedicalRecord record : records) {
            if (record.getAppointment() != null) {
                recordsByAppointmentId.put(record.getAppointment().getId(), record);
            }
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("recordsByAppointmentId", recordsByAppointmentId);

        return "doctor/appointments";
    }

    @GetMapping("/appointments/accept/{id}")
    public String acceptAppointment(@PathVariable Long id, Principal principal) {
        Doctor doctor = getLoggedDoctor(principal);
        Appointment appointment = appointmentService.getAppointmentById(id);

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            return "redirect:/doctor/appointments";
        }

        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointment.setStatus(AppointmentStatus.ACCEPTED);
            appointmentService.updateAppointment(id, appointment);
        }

        return "redirect:/doctor/appointments";
    }

    @GetMapping("/appointments/reject/{id}")
    public String rejectAppointment(@PathVariable Long id, Principal principal) {
        Doctor doctor = getLoggedDoctor(principal);
        Appointment appointment = appointmentService.getAppointmentById(id);

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            return "redirect:/doctor/appointments";
        }

        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointment.setStatus(AppointmentStatus.REJECTED);
            appointmentService.updateAppointment(id, appointment);
        }

        return "redirect:/doctor/appointments";
    }

    @GetMapping("/appointments/complete/{id}")
    public String completeAppointment(@PathVariable Long id, Principal principal) {
        Doctor doctor = getLoggedDoctor(principal);
        Appointment appointment = appointmentService.getAppointmentById(id);

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            return "redirect:/doctor/appointments";
        }

        if (appointment.getStatus() == AppointmentStatus.ACCEPTED) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentService.updateAppointment(id, appointment);
        }

        return "redirect:/doctor/appointments";
    }

    @GetMapping("/medical-records")
    public String medicalRecords(Model model, Principal principal) {
        Doctor doctor = getLoggedDoctor(principal);

        List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByDoctor(doctor);

        model.addAttribute("records", records);

        return "doctor/medical-records";
    }

    @GetMapping("/medical-records/new")
    public String showCreateMedicalRecordForm(
            @RequestParam(required = false) Long appointmentId,
            Model model,
            Principal principal) {

        Doctor doctor = getLoggedDoctor(principal);

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setDoctor(doctor);

        if (appointmentId != null) {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                return "redirect:/doctor/appointments";
            }

            if (medicalRecordService.existsByAppointment(appointment)) {
                return "redirect:/doctor/appointments";
            }

            medicalRecord.setAppointment(appointment);
            medicalRecord.setPatient(appointment.getPatient());

            if (appointment.getAppointmentDate() != null) {
                medicalRecord.setRecordDate(appointment.getAppointmentDate().toLocalDate());
            }
        }

        model.addAttribute("medicalRecord", medicalRecord);
        prepareMedicalRecordFormModel(model, doctor, "Adaugă fișă medicală");

        return "doctor/medical-record-form";
    }

    @PostMapping("/medical-records")
    public String createMedicalRecord(
            @Valid @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
            BindingResult bindingResult,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) List<Long> treatmentIds,
            Model model,
            Principal principal) {

        Doctor doctor = getLoggedDoctor(principal);

        if (bindingResult.hasErrors()) {
            prepareMedicalRecordFormModel(model, doctor, "Adaugă fișă medicală");
            return "doctor/medical-record-form";
        }

        try {
            setDoctorMedicalRecordRelations(medicalRecord, doctor, appointmentId, patientId, treatmentIds);

            medicalRecordService.createMedicalRecord(medicalRecord);

            return "redirect:/doctor/medical-records";

        } catch (Exception e) {
            prepareMedicalRecordFormModel(model, doctor, "Adaugă fișă medicală");
            model.addAttribute("errorMessage", e.getMessage());

            return "doctor/medical-record-form";
        }
    }

    @GetMapping("/medical-records/{id}")
    public String medicalRecordDetails(
            @PathVariable Long id,
            Model model,
            Principal principal) {

        Doctor doctor = getLoggedDoctor(principal);
        MedicalRecord record = medicalRecordService.getMedicalRecordById(id);

        if (record.getDoctor() == null || !record.getDoctor().getId().equals(doctor.getId())) {
            return "redirect:/doctor/medical-records";
        }

        model.addAttribute("record", record);

        return "doctor/medical-record-details";
    }

    @GetMapping("/medical-records/edit/{id}")
    public String showEditMedicalRecordForm(
            @PathVariable Long id,
            Model model,
            Principal principal) {

        Doctor doctor = getLoggedDoctor(principal);
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordById(id);

        if (medicalRecord.getDoctor() == null || !medicalRecord.getDoctor().getId().equals(doctor.getId())) {
            return "redirect:/doctor/medical-records";
        }

        model.addAttribute("medicalRecord", medicalRecord);
        prepareMedicalRecordEditFormModel(model, doctor, medicalRecord, "Editează fișă medicală");

        return "doctor/medical-record-form";
    }
    @PostMapping("/medical-records/edit/{id}")
    public String updateMedicalRecord(
            @PathVariable Long id,
            @Valid @ModelAttribute("medicalRecord") MedicalRecord medicalRecord,
            BindingResult bindingResult,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) List<Long> treatmentIds,
            Model model,
            Principal principal) {

        Doctor doctor = getLoggedDoctor(principal);

        if (bindingResult.hasErrors()) {
            prepareMedicalRecordFormModel(model, doctor, "Editează fișă medicală");
            return "doctor/medical-record-form";
        }

        try {
            setDoctorMedicalRecordRelations(medicalRecord, doctor, appointmentId, patientId, treatmentIds);

            medicalRecordService.updateMedicalRecord(id, medicalRecord);

            return "redirect:/doctor/medical-records";

        } catch (Exception e) {
            prepareMedicalRecordFormModel(model, doctor, "Editează fișă medicală");
            model.addAttribute("errorMessage", e.getMessage());

            return "doctor/medical-record-form";
        }
    }

    private void prepareMedicalRecordFormModel(Model model, Doctor doctor, String pageTitle) {
        List<Appointment> allAppointments = appointmentService.getAppointmentsByDoctor(doctor);

        List<Appointment> availableAppointments = allAppointments.stream()
                .filter(appointment ->
                        appointment.getStatus() == AppointmentStatus.ACCEPTED
                                || appointment.getStatus() == AppointmentStatus.COMPLETED
                )
                .filter(appointment -> !medicalRecordService.existsByAppointment(appointment))
                .toList();

        List<Patient> patients = patientService.getAllPatients();
        List<Treatment> treatments = treatmentService.getAllTreatments();

        model.addAttribute("appointments", availableAppointments);
        model.addAttribute("patients", patients);
        model.addAttribute("treatments", treatments);
        model.addAttribute("pageTitle", pageTitle);
      
    }
    private void prepareMedicalRecordEditFormModel(Model model, Doctor doctor, MedicalRecord currentRecord, String pageTitle) {
        List<Appointment> allAppointments = appointmentService.getAppointmentsByDoctor(doctor);

        List<Appointment> availableAppointments = allAppointments.stream()
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

        List<Patient> patients = patientService.getAllPatients();
        List<Treatment> treatments = treatmentService.getAllTreatments();

        model.addAttribute("appointments", availableAppointments);
        model.addAttribute("patients", patients);
        model.addAttribute("treatments", treatments);
        model.addAttribute("pageTitle", pageTitle);
    }
    private void setDoctorMedicalRecordRelations(
            MedicalRecord medicalRecord,
            Doctor doctor,
            Long appointmentId,
            Long patientId,
            List<Long> treatmentIds) {

        medicalRecord.setDoctor(doctor);

        if (appointmentId != null) {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);

            if (!appointment.getDoctor().getId().equals(doctor.getId())) {
                throw new IllegalArgumentException("Nu poți crea fișă pentru programarea altui doctor.");
            }

            medicalRecord.setAppointment(appointment);
            medicalRecord.setPatient(appointment.getPatient());
        } else {
            if (patientId == null) {
                throw new IllegalArgumentException("Trebuie selectat un pacient.");
            }

            Patient patient = patientService.getPatientById(patientId);
            medicalRecord.setPatient(patient);
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

    private Doctor getLoggedDoctor(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());

        return doctorService.getDoctorByUser(user);
    }
}