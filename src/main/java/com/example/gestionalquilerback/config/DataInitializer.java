package com.example.gestionalquilerback.config;
import com.example.gestionalquilerback.model.entity.User;
import com.example.gestionalquilerback.model.enums.Role;
import com.example.gestionalquilerback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
@Component @RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${APP_BOOTSTRAP_ADMIN_ENABLED:false}") private boolean enabled;
    @Value("${APP_BOOTSTRAP_ADMIN_EMAIL:}") private String email;
    @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}") private String password;
    @Value("${APP_BOOTSTRAP_ADMIN_RESET_PASSWORD:false}") private boolean resetPassword;
    @Override public void run(String... args) {
        if (!enabled) return;
        if (email.isBlank() || password.length() < 16) throw new IllegalStateException("Configura el email y una contraseña de administrador de al menos 16 caracteres");
        var existing = userRepository.findByEmail(email);
        if (existing.isEmpty()) userRepository.save(User.builder().name("Administrador").email(email).password(passwordEncoder.encode(password)).role(Role.ADMIN).build());
        else if (resetPassword) {
            var admin = existing.get();
            if (admin.getRole() != Role.ADMIN) throw new IllegalStateException("El reinicio administrativo no puede convertir una cuenta normal en administradora");
            admin.setPassword(passwordEncoder.encode(password));
            userRepository.save(admin);
        }
    }
}
