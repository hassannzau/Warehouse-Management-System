package org.example;

import org.example.enums.Role;
import org.example.entity.Store;
import org.example.entity.User;
import org.example.repository.StoreRepository;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*
 * Ensures a default store and admin account exist on startup. Hibernate/JPA manages
 * the table structure itself ({@code spring.jpa.hibernate.ddl-auto=update}); this
 * only seeds the reference rows the rest of the app assumes are present, replacing
 * the INSERTs that used to live in schema.sql.
 */
@Component
public class DefaultDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultDataSeeder.class);

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultDataSeeder(StoreRepository storeRepository, UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (storeRepository.count() == 0) {
            storeRepository.save(new Store("Main Store", ""));
        }
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.warn("No users found - created default admin account (username: admin, password: admin123). " +
                    "Log in and create a real admin user, then remove or change this one.");
        }
    }
}
