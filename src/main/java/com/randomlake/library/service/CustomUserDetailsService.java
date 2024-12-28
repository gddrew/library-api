package com.randomlake.library.service;

import com.randomlake.library.model.ApplicationUser;
import com.randomlake.library.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  @Autowired private UserRepository userRepository;

  @Autowired
  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostConstruct
  public void initAdminUser() {
    if (userRepository.count() == 0) {
      ApplicationUser admin = new ApplicationUser();
      admin.setUsername("admin");
      admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
      admin.setRole("ROLE_ADMIN");
      userRepository.save(admin);
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    ApplicationUser applicationUser =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new org.springframework.security.core.userdetails.User(
        applicationUser.getUsername(),
        applicationUser.getPassword(),
        Arrays.stream(applicationUser.getRole().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList()));
  }
}
