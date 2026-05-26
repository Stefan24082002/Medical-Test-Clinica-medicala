package com.example.clinic.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("authorities", authentication.getAuthorities());

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_DOCTOR"));

        boolean isPatient = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_PATIENT"));

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isDoctor", isDoctor);
        model.addAttribute("isPatient", isPatient);

        return "dashboard";
    }
}