package com.dom.employeemanager.config;

import com.dom.employeemanager.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true) // if want to @PreAuthorize("hasRole('ROLE_ADMIN')")
public class WebSecurityConfig {
  @Value("${api.prefix}")
  private String apiPrefix;
  private final JwtTokenFilter jwtTokenFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
      .authorizeHttpRequests(requests -> {
        requests
          .requestMatchers(
            // Employee Manager
            String.format("%s/employeemanager/login", apiPrefix),
            String.format("%s/employeemanager/refreshToken", apiPrefix),

            // Employees
            String.format("%s/employees/register", apiPrefix),
            String.format("%s/employees/login", apiPrefix),
            String.format("%s/employees/refreshToken", apiPrefix),
            String.format("%s/employees/reset-password", apiPrefix),

            String.format("%s/email/otp/resetPassword", apiPrefix),
            String.format("%s/email/otp/send", apiPrefix),
            String.format("%s/email/otp/validate", apiPrefix),


            // Roles
            String.format("%s/roles", apiPrefix)
          ).permitAll()

          .anyRequest()
          .authenticated();
        //.anyRequest().permitAll();
      })
      .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
