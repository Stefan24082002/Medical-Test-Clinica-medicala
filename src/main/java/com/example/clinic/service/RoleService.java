package com.example.clinic.service;

import com.example.clinic.entity.Role;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    public Role createRole(Role role) {
        log.info("Creating role with name: {}", role.getName());

        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new DuplicateResourceException("Rolul există deja: " + role.getName());
        }

        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        log.info("Fetching role with id: {}", id);

        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rolul nu a fost găsit cu id-ul: " + id));
    }

    public Role getRoleByName(String name) {
        log.info("Fetching role with name: {}", name);

        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Rolul nu a fost găsit: " + name));
    }

    public Role updateRole(Long id, Role updatedRole) {
        log.info("Updating role with id: {}", id);

        Role existingRole = getRoleById(id);

        if (!existingRole.getName().equals(updatedRole.getName())
                && roleRepository.findByName(updatedRole.getName()).isPresent()) {
            throw new DuplicateResourceException("Există deja un rol cu numele: " + updatedRole.getName());
        }

        existingRole.setName(updatedRole.getName());

        return roleRepository.save(existingRole);
    }

    public void deleteRole(Long id) {
        log.info("Deleting role with id: {}", id);

        Role role = getRoleById(id);
        roleRepository.delete(role);
    }
}