package com.example.clinic.controller;

import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.AppointmentStatus;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.MedicalRecord;
import com.example.clinic.entity.Patient;
import com.example.clinic.service.AppointmentService;
import com.example.clinic.service.DoctorService;
import com.example.clinic.service.MedicalRecordService;
import com.example.clinic.service.PatientService;
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

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping
    public String listAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "appointmentDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Appointment> appointmentPage = appointmentService.getAllAppointments(pageable);

        Map<Long, MedicalRecord> recordsByAppointmentId = new HashMap<>();

        for (Appointment appointment : appointmentPage.getContent()) {
            medicalRecordService.getMedicalRecordByAppointment(appointment)
                    .ifPresent(record -> recordsByAppointmentId.put(appointment.getId(), record));
        }

        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("recordsByAppointmentId", recordsByAppointmentId);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "appointments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("appointment", new Appointment());
        prepareAppointmentForm(model, "Adaugă programare");

        return "appointments/form";
    }

    @PostMapping
    public String createAppointment(
            @Valid @ModelAttribute("appointment") Appointment appointment,
            BindingResult bindingResult,
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            Model model) {

        if (bindingResult.hasErrors()) {
            prepareAppointmentForm(model, "Adaugă programare");
            return "appointments/form";
        }

        try {
            Patient patient = patientService.getPatientById(patientId);
            Doctor doctor = doctorService.getDoctorById(doctorId);

            appointment.setPatient(patient);
            appointment.setDoctor(doctor);

            if (appointment.getStatus() == null) {
                appointment.setStatus(AppointmentStatus.SCHEDULED);
            }

            appointmentService.createAppointment(appointment);

            return "redirect:/admin/appointments";

        } catch (Exception e) {
            model.addAttribute("appointment", appointment);
            prepareAppointmentForm(model, "Adaugă programare");
            model.addAttribute("errorMessage", e.getMessage());

            return "appointments/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id);

        model.addAttribute("appointment", appointment);
        prepareAppointmentForm(model, "Editează programare");

        return "appointments/form";
    }

    @PostMapping("/edit/{id}")
    public String updateAppointment(
            @PathVariable Long id,
            @Valid @ModelAttribute("appointment") Appointment appointment,
            BindingResult bindingResult,
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            Model model) {

        if (bindingResult.hasErrors()) {
            prepareAppointmentForm(model, "Editează programare");
            return "appointments/form";
        }

        try {
            Patient patient = patientService.getPatientById(patientId);
            Doctor doctor = doctorService.getDoctorById(doctorId);

            appointment.setPatient(patient);
            appointment.setDoctor(doctor);

            appointmentService.updateAppointment(id, appointment);

            return "redirect:/admin/appointments";

        } catch (Exception e) {
            Appointment existingAppointment = appointmentService.getAppointmentById(id);

            model.addAttribute("appointment", existingAppointment);
            prepareAppointmentForm(model, "Editează programare");
            model.addAttribute("errorMessage", e.getMessage());

            return "appointments/form";
        }
    }

    @GetMapping("/accept/{id}")
    public String acceptAppointment(@PathVariable Long id) {
        appointmentService.acceptAppointment(id);
        return "redirect:/admin/appointments";
    }

    @GetMapping("/reject/{id}")
    public String rejectAppointment(@PathVariable Long id) {
        appointmentService.rejectAppointment(id);
        return "redirect:/admin/appointments";
    }

    @GetMapping("/complete/{id}")
    public String completeAppointment(@PathVariable Long id) {
        appointmentService.completeAppointment(id);
        return "redirect:/admin/appointments";
    }

    @GetMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return "redirect:/admin/appointments";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return "redirect:/admin/appointments";
    }

    private void prepareAppointmentForm(Model model, String pageTitle) {
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("statuses", AppointmentStatus.values());
        model.addAttribute("pageTitle", pageTitle);
    }
}