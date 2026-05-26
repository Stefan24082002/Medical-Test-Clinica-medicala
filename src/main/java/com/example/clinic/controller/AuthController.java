package com.example.clinic.controller;

import com.example.clinic.entity.Patient;
import com.example.clinic.entity.User;
import com.example.clinic.service.PatientService;
import com.example.clinic.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PatientService patientService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerPatient(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String phone,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            User savedUser = userService.createUser(user);

            Patient patient = new Patient();
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(savedUser.getEmail());
            patient.setPhone(phone);
            patient.setUser(savedUser);

            patientService.createPatient(patient);

            return "redirect:/login?registered";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
}