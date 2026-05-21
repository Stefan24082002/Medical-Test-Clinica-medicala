package com.example.clinic.service;

import com.example.clinic.entity.Role;
import com.example.clinic.entity.User;
import com.example.clinic.exception.DuplicateResourceException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.repository.RoleRepository;
import com.example.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        log.info("Creating user with username: {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Există deja un utilizator cu username-ul: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Există deja un utilizator cu email-ul: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                    .orElseThrow(() -> new ResourceNotFoundException("Rolul ROLE_PATIENT nu există."));
            Set<Role> roles = new HashSet<>();
            roles.add(patientRole);
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        log.info("Fetching users with pagination");
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long id) {
        log.info("Fetching user with id: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit cu id-ul: " + id));
    }

    public User getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit: " + username));
    }

    public User updateUser(Long id, User updatedUser) {
        log.info("Updating user with id: {}", id);

        User existingUser = getUserById(id);

        if (!existingUser.getUsername().equals(updatedUser.getUsername())
                && userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new DuplicateResourceException("Există deja un utilizator cu username-ul: " + updatedUser.getUsername());
        }

        if (!existingUser.getEmail().equals(updatedUser.getEmail())
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new DuplicateResourceException("Există deja un utilizator cu email-ul: " + updatedUser.getEmail());
        }

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setEnabled(updatedUser.isEnabled());
        existingUser.setRoles(updatedUser.getRoles());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        User user = getUserById(id);
        userRepository.delete(user);
    }
}