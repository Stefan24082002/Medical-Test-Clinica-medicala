package com.example.clinic.controller;

import com.example.clinic.entity.Department;
import com.example.clinic.service.DepartmentService;
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
@RequestMapping("/admin/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public String listDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Department> departmentPage = departmentService.getAllDepartments(pageable);

        model.addAttribute("departmentPage", departmentPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "departments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        model.addAttribute("pageTitle", "Adaugă departament");
        return "departments/form";
    }

    @PostMapping
    public String createDepartment(
            @Valid @ModelAttribute("department") Department department,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Adaugă departament");
            return "departments/form";
        }

        departmentService.createDepartment(department);
        return "redirect:/admin/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Department department = departmentService.getDepartmentById(id);

        model.addAttribute("department", department);
        model.addAttribute("pageTitle", "Editează departament");

        return "departments/form";
    }

    @PostMapping("/edit/{id}")
    public String updateDepartment(
            @PathVariable Long id,
            @Valid @ModelAttribute("department") Department department,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Editează departament");
            return "departments/form";
        }

        departmentService.updateDepartment(id, department);
        return "redirect:/admin/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return "redirect:/admin/departments";
    }
}