package com.randomlake.library.config;

import com.randomlake.library.model.ApplicationUser;
import com.randomlake.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  @Bean
  public CommandLineRunner loadData(UserRepository userRepository) {
    return args -> {
      if (userRepository.count() == 0) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        ApplicationUser adminUser = new ApplicationUser();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole("ROLE_ADMIN");

        userRepository.save(adminUser);

        log.info("Default admin user created with username: admin and password: admin123");
      } else {
        log.info("Users already exist in the database. Skipping initialization");
      }
    };
  }
}
