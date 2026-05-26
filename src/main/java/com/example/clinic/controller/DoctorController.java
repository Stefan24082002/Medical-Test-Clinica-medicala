package com.example.clinic.controller;

import com.example.clinic.entity.Department;
import com.example.clinic.entity.Doctor;
import com.example.clinic.service.DepartmentService;
import com.example.clinic.service.DoctorService;
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
@RequestMapping("/admin/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final DepartmentService departmentService;

    @GetMapping
    public String listDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Doctor> doctorPage = doctorService.getAllDoctors(pageable);

        model.addAttribute("doctorPage", doctorPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "doctors/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("pageTitle", "Adaugă doctor");

        return "doctors/form";
    }

    @PostMapping
    public String createDoctor(
            @Valid @ModelAttribute("doctor") Doctor doctor,
            BindingResult bindingResult,
            @RequestParam(required = false) Long departmentId,
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("pageTitle", "Adaugă doctor");
            return "doctors/form";
        }

        try {
            if (departmentId != null) {
                Department department = departmentService.getDepartmentById(departmentId);
                doctor.setDepartment(department);
            }

            doctorService.createDoctorWithAccount(doctor, username, password);

            return "redirect:/admin/doctors";

        } catch (Exception e) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("pageTitle", "Adaugă doctor");
            model.addAttribute("errorMessage", e.getMessage());

            return "doctors/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);

        model.addAttribute("doctor", doctor);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("pageTitle", "Editează doctor");

        return "doctors/form";
    }

    @PostMapping("/edit/{id}")
    public String updateDoctor(
            @PathVariable Long id,
            @Valid @ModelAttribute("doctor") Doctor doctor,
            BindingResult bindingResult,
            @RequestParam(required = false) Long departmentId,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("pageTitle", "Editează doctor");
            return "doctors/form";
        }

        try {
            if (departmentId != null) {
                Department department = departmentService.getDepartmentById(departmentId);
                doctor.setDepartment(department);
            } else {
                doctor.setDepartment(null);
            }

            doctorService.updateDoctor(id, doctor);

            return "redirect:/admin/doctors";

        } catch (Exception e) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("pageTitle", "Editează doctor");
            model.addAttribute("errorMessage", e.getMessage());

            return "doctors/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return "redirect:/admin/doctors";
    }
}