package com.example.clinic.bootstrap;

import com.example.clinic.entity.Role;
import com.example.clinic.entity.User;
import com.example.clinic.repository.RoleRepository;
import com.example.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_DOCTOR");
        createRoleIfNotExists("ROLE_PATIENT");

        createAdminIfNotExists();
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .name(roleName)
                    .build();

            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }

    private void createAdminIfNotExists() {
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN nu există"));

        User admin = User.builder()
                .username("admin")
                .email("admin@clinic.com")
                .password(passwordEncoder.encode("admin123"))
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Created default admin user");
    }
}