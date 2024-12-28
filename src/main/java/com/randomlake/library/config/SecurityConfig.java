package com.randomlake.library.config;

import com.randomlake.library.service.CustomUserDetailsService;
import com.randomlake.library.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Autowired private CustomUserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // Use BCrypt for password hashing
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtUtil jwtUtil, UserDetailsService userDetailsService) throws Exception {

    JwtAuthenticationFilter jwtAuthenticationFilter =
        new JwtAuthenticationFilter(jwtUtil, userDetailsService);

    http
        // Disable CSRF for simplicity in testing; enable for production with proper configuration
        .csrf(csrf -> csrf.disable())

        // Configure endpoint access rules
        .authorizeHttpRequests(
            auth ->
                auth
                    // Allow all for development and testing
                    .requestMatchers("/api/auth/login")
                    .permitAll()
                    // Allow public access to authentication endpoints (e.g., login, signup)
                    // .requestMatchers("/api/auth/**").permitAll() // Public endpoints
                    // .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin-only endpoints
                    // Protect all other endpoints; require authentication
                    .anyRequest()
                    .authenticated())
        // Configure HTTP Basic authentication for testing
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(new AuthEntryPointJwt()));

    return http.build();
  }
}
