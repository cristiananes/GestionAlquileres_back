package com.example.gestionalquilerback.config;

import com.example.gestionalquilerback.model.entity.User;
import com.example.gestionalquilerback.model.enums.Role;
import com.example.gestionalquilerback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@gestionalquiler.com")) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@gestionalquiler.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Usuario admin creado: admin@gestionalquiler.com / admin123");
        }
    }
}
