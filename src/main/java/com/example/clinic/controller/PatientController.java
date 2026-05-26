package com.example.clinic.controller;

import com.example.clinic.entity.Patient;
import com.example.clinic.entity.User;
import com.example.clinic.service.PatientService;
import com.example.clinic.service.UserService;
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
    private final UserService userService;

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
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Adaugă pacient");
            return "patients/form";
        }

        try {
            boolean usernameCompleted = username != null && !username.isBlank();
            boolean passwordCompleted = password != null && !password.isBlank();

            if (usernameCompleted || passwordCompleted) {
                if (!usernameCompleted || !passwordCompleted) {
                    model.addAttribute("pageTitle", "Adaugă pacient");
                    model.addAttribute("errorMessage", "Pentru cont pacient trebuie completate atât username-ul, cât și parola.");
                    return "patients/form";
                }

                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setEmail(patient.getEmail());

                User savedUser = userService.createUser(user);
                patient.setUser(savedUser);
            }

            patientService.createPatient(patient);

            return "redirect:/admin/patients";

        } catch (Exception e) {
            model.addAttribute("pageTitle", "Adaugă pacient");
            model.addAttribute("errorMessage", e.getMessage());

            return "patients/form";
        }
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

        try {
            patientService.updatePatient(id, patient);

            return "redirect:/admin/patients";

        } catch (Exception e) {
            model.addAttribute("pageTitle", "Editează pacient");
            model.addAttribute("errorMessage", e.getMessage());

            return "patients/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);

        return "redirect:/admin/patients";
    }
}