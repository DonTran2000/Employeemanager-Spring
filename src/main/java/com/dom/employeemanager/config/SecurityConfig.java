package com.dom.employeemanager.config;

import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.repo.EmployeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final EmployeeRepo employeeRepo;
  //user's detail object
  @Bean
  public UserDetailsService userDetailsService() {
    return subject -> {
      // Attempt to find user by phone number
      Optional<Employee> employeeByPhone = employeeRepo.findByPhone(subject);
      if (employeeByPhone.isPresent()) {
        return employeeByPhone.get(); // Return UserDetails if found
      }

      // If user not found by phone number, attempt to find by email
      Optional<Employee> employeeByEmail = employeeRepo.findByEmail(subject);
      if (employeeByEmail.isPresent()) {
        return employeeByEmail.get(); // Return UserDetails if found
      }

      // If user not found by either phone number or email, throw UsernameNotFoundException
      throw new UsernameNotFoundException("User not found with subject: " + subject);
    };
  }
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
