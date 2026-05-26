package com.example.clinic.controller;

import com.example.clinic.entity.Department;
import com.example.clinic.entity.Treatment;
import com.example.clinic.service.DepartmentService;
import com.example.clinic.service.TreatmentService;
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
@RequestMapping("/admin/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final DepartmentService departmentService;

    @GetMapping
    public String listTreatments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Treatment> treatmentPage = treatmentService.getAllTreatments(pageable);

        model.addAttribute("treatmentPage", treatmentPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "treatments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("treatment", new Treatment());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("pageTitle", "Adaugă serviciu medical");

        return "treatments/form";
    }

    @PostMapping
    public String createTreatment(
            @Valid @ModelAttribute("treatment") Treatment treatment,
            BindingResult bindingResult,
            @RequestParam(required = false) Long departmentId,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("pageTitle", "Adaugă serviciu medical");
            return "treatments/form";
        }

        try {
            if (departmentId != null) {
                Department department = departmentService.getDepartmentById(departmentId);
                treatment.setDepartment(department);
            }

            treatmentService.createTreatment(treatment);

            return "redirect:/admin/treatments";

        } catch (Exception e) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("pageTitle", "Adaugă serviciu medical");
            model.addAttribute("errorMessage", e.getMessage());

            return "treatments/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Treatment treatment = treatmentService.getTreatmentById(id);

        model.addAttribute("treatment", treatment);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("pageTitle", "Editează serviciu medical");

        return "treatments/form";
    }

    @PostMapping("/edit/{id}")
    public String updateTreatment(
            @PathVariable Long id,
            @Valid @ModelAttribute("treatment") Treatment treatment,
            BindingResult bindingResult,
            @RequestParam(required = false) Long departmentId,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("pageTitle", "Editează serviciu medical");
            return "treatments/form";
        }

        try {
            if (departmentId != null) {
                Department department = departmentService.getDepartmentById(departmentId);
                treatment.setDepartment(department);
            } else {
                treatment.setDepartment(null);
            }

            treatmentService.updateTreatment(id, treatment);

            return "redirect:/admin/treatments";

        } catch (Exception e) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("pageTitle", "Editează serviciu medical");
            model.addAttribute("errorMessage", e.getMessage());

            return "treatments/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteTreatment(@PathVariable Long id) {
        treatmentService.deleteTreatment(id);

        return "redirect:/admin/treatments";
    }
}