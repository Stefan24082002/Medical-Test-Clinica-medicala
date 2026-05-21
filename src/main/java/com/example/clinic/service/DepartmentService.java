package com.example.clinic.service;

import com.example.clinic.entity.Department;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public Department createDepartment(Department department) {
        log.info("Creating department with name: {}", department.getName());

        if (departmentRepository.existsByName(department.getName())) {
            throw new DuplicateResourceException("Departamentul există deja: " + department.getName());
        }

        return departmentRepository.save(department);
    }

    public List<Department> getAllDepartments() {
        log.info("Fetching all departments");
        return departmentRepository.findAll();
    }

    public Page<Department> getAllDepartments(Pageable pageable) {
        log.info("Fetching departments with pagination");
        return departmentRepository.findAll(pageable);
    }

    public Department getDepartmentById(Long id) {
        log.info("Fetching department with id: {}", id);

        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamentul nu a fost găsit cu id-ul: " + id));
    }

    public Department updateDepartment(Long id, Department updatedDepartment) {
        log.info("Updating department with id: {}", id);

        Department existingDepartment = getDepartmentById(id);

        if (!existingDepartment.getName().equals(updatedDepartment.getName())
                && departmentRepository.existsByName(updatedDepartment.getName())) {
            throw new DuplicateResourceException("Există deja un departament cu numele: " + updatedDepartment.getName());
        }

        existingDepartment.setName(updatedDepartment.getName());
        existingDepartment.setDescription(updatedDepartment.getDescription());

        return departmentRepository.save(existingDepartment);
    }

    public void deleteDepartment(Long id) {
        log.info("Deleting department with id: {}", id);

        Department department = getDepartmentById(id);
        departmentRepository.delete(department);
    }
}