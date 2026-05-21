package com.example.clinic.controller;

import com.example.clinic.entity.Patient;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/patients")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public String listPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Patient> patientPage = patientService.getAllPatients(pageable);

        model.addAttribute("patientPage", patientPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "patients/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("patient", new Patient());
        model.addAttribute("pageTitle", "Adaugă pacient");
        return "patients/form";
    }

    @PostMapping
    public String createPatient(
            @Valid @ModelAttribute("patient") Patient patient,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Adaugă pacient");
            return "patients/form";
        }

        patientService.createPatient(patient);
        return "redirect:/admin/patients";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Patient patient = patientService.getPatientById(id);

        model.addAttribute("patient", patient);
        model.addAttribute("pageTitle", "Editează pacient");

        return "patients/form";
    }

    @PostMapping("/edit/{id}")
    public String updatePatient(
            @PathVariable Long id,
            @Valid @ModelAttribute("patient") Patient patient,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Editează pacient");
            return "patients/form";
        }

        patientService.updatePatient(id, patient);
        return "redirect:/admin/patients";
    }

    @GetMapping("/delete/{id}")
    public String deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return "redirect:/admin/patients";
    }
}