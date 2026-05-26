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

        validateUserForCreate(user);

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

    public User createUserWithRole(String username, String email, String password, String roleName) {
        log.info("Creating user with username: {} and role: {}", username, roleName);

        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username-ul este obligatoriu.");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email-ul este obligatoriu.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Parola este obligatorie.");
        }

        if (roleName == null || roleName.isBlank()) {
            throw new RuntimeException("Rolul este obligatoriu.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Există deja un utilizator cu username-ul: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Există deja un utilizator cu email-ul: " + email);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rolul nu există: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    public User createDoctorUser(String username, String email, String password) {
        return createUserWithRole(username, email, password, "ROLE_DOCTOR");
    }

    public User createPatientUser(String username, String email, String password) {
        return createUserWithRole(username, email, password, "ROLE_PATIENT");
    }

    public User createAdminUser(String username, String email, String password) {
        return createUserWithRole(username, email, password, "ROLE_ADMIN");
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

        if (updatedUser.getUsername() == null || updatedUser.getUsername().isBlank()) {
            throw new RuntimeException("Username-ul este obligatoriu.");
        }

        if (updatedUser.getEmail() == null || updatedUser.getEmail().isBlank()) {
            throw new RuntimeException("Email-ul este obligatoriu.");
        }

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

        if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
            existingUser.setRoles(updatedUser.getRoles());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public void changePassword(Long id, String newPassword) {
        log.info("Changing password for user id: {}", id);

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Parola nouă este obligatorie.");
        }

        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        User user = getUserById(id);
        userRepository.delete(user);
    }

    private void validateUserForCreate(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new RuntimeException("Username-ul este obligatoriu.");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("Email-ul este obligatoriu.");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Parola este obligatorie.");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Există deja un utilizator cu username-ul: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Există deja un utilizator cu email-ul: " + user.getEmail());
        }
    }
}