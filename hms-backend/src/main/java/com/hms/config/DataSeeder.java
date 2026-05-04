package com.hms.config;

import com.hms.entity.User;
import com.hms.entity.enums.Role;
import com.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@hms.com")) {
            User admin = User.builder()
                    .name("Main Admin")
                    .email("admin@hms.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(java.util.Objects.requireNonNull(admin));
            System.out.println("Default Admin created: admin@hms.com / admin123");
        }
    }
}
