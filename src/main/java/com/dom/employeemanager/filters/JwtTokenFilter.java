package com.dom.employeemanager.filters;

import com.dom.employeemanager.components.JwtTokenUtils;
import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.utils.TokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
  @Value("${api.prefix}")
  private String apiPrefix;
  private final UserDetailsService userDetailsService;
  private final JwtTokenUtils jwtTokenUtil;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
    throws ServletException, IOException {
    try {
      if (isBypassToken(request)) {
        filterChain.doFilter(request, response); //enable bypass
        return;
      }
      final String authHeader = request.getHeader("Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        response.sendError(
          HttpServletResponse.SC_UNAUTHORIZED,
          "authHeader null or not started with Bearer");
        return;
      }
      final String token = authHeader.substring(7);
      final String phone = jwtTokenUtil.getSubject(token, TokenType.ACCESS_TOKEN);
      if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        Employee userDetails = (Employee) userDetailsService.loadUserByUsername(phone);
        if (jwtTokenUtil.isValid(token, TokenType.ACCESS_TOKEN, userDetails)) {
          UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
          );
          authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
      }
      filterChain.doFilter(request, response); //enable bypass
    } catch (Exception e) {
      //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write(e.getMessage());
    }

  }

  private boolean isBypassToken(@NonNull HttpServletRequest request) {
    final List<Pair<String, String>> bypassTokens = Arrays.asList(
      // Employee Manager
      Pair.of(String.format("%s/employeemanager/login", apiPrefix), "POST"),
//      Pair.of(String.format("%s/employeemanager/register", apiPrefix), "POST"),
      Pair.of(String.format("%s/employeemanager/refreshToken", apiPrefix), "POST"),

      // Employees
      Pair.of(String.format("%s/employees/register", apiPrefix), "POST"),
      Pair.of(String.format("%s/employees/login", apiPrefix), "POST"),
      Pair.of(String.format("%s/employees/refreshToken", apiPrefix), "POST"),
      Pair.of(String.format("%s/employees/reset-password", apiPrefix), "POST"),

      Pair.of(String.format("%s/email/otp/resetPassword", apiPrefix), "POST"),
      Pair.of(String.format("%s/email/otp/send", apiPrefix), "POST"),
      Pair.of(String.format("%s/email/otp/validate", apiPrefix), "POST"),

      // Roles
      Pair.of(String.format("%s/roles", apiPrefix), "GET")
    );

    String requestPath = request.getServletPath();
    String requestMethod = request.getMethod();

    for (Pair<String, String> token : bypassTokens) {
      String path = token.getFirst();
      String method = token.getSecond();
      // Check if the request path and method match any pair in the bypassTokens list
      if (requestPath.matches(path.replace("**", ".*")) && requestMethod.equalsIgnoreCase(method)) {
        return true;
      }
    }
    return false;
  }
}
