package com.dom.employeemanager.controller;

import com.dom.employeemanager.dtos.EmployeeDTO;
import com.dom.employeemanager.dtos.EmployeeLoginDTO;
import com.dom.employeemanager.dtos.RefreshTokenDTO;
import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.models.Token;
import com.dom.employeemanager.responses.EmployeeResponse;
import com.dom.employeemanager.responses.LoginResponse;
import com.dom.employeemanager.responses.ResponseObject;
import com.dom.employeemanager.service.EmployeeService;
import com.dom.employeemanager.service.TokenService;
import com.dom.employeemanager.utils.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;
  private final TokenService tokenService;
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/register")
  public ResponseEntity<ResponseObject> register(@RequestBody EmployeeDTO employeeDTO) throws Exception {
    employeeService.createEmployee(employeeDTO);

    return ResponseEntity.ok().body(ResponseObject
      .builder()
      .message("Register successfully")
      .status(HttpStatus.CREATED)
      .data(employeeDTO)
      .build());
  }

  @PostMapping("/login")
  public ResponseEntity<ResponseObject> login(
    @RequestBody EmployeeLoginDTO employeeLoginDTO,
    HttpServletRequest request)
    throws Exception {
    String token = employeeService.login(employeeLoginDTO);
    String userAgent = request.getHeader("User-Agent");
    Employee userDetail = employeeService.getUserDetailsFromToken(token, TokenType.ACCESS_TOKEN);
    Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

    LoginResponse loginResponse = LoginResponse
      .builder()
      .message("Login successfully")
      .token(jwtToken.getToken())
      .refreshToken(jwtToken.getRefreshToken())
      .tokenType(jwtToken.getTokenType())
      .roles(userDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
      .id(userDetail.getId())
      .build();
    return ResponseEntity.ok().body(ResponseObject
      .builder()
      .message("Login successfully")
      .data(loginResponse)
      .status(HttpStatus.OK)
      .build());
  }

  private boolean isMobileDevice(String userAgent) {
    // Kiểm tra User-Agent header để xác định thiết bị di động
    // Ví dụ đơn giản:
    return userAgent.toLowerCase().contains("mobile");
  }


  @PostMapping("/refreshToken")
  public ResponseEntity<LoginResponse> refreshToken(
    @RequestBody RefreshTokenDTO refreshTokenDTO
  ) throws Exception {
    Employee userDetail = employeeService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
    Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(), userDetail);
    LoginResponse loginResponse = LoginResponse.builder()
      .message("Refresh token successfully")
      .token(jwtToken.getToken())
      .tokenType(jwtToken.getTokenType())
      .refreshToken(jwtToken.getRefreshToken())
      .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
      .id(userDetail.getId()).build();
    return ResponseEntity.ok().body(loginResponse);
  }

  @PostMapping("/details")
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
  public ResponseEntity<ResponseObject> getEmployeeDetails(
    @RequestHeader("Authorization") String authorizationHeader
  ) throws Exception {
    String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
    Employee employee = employeeService.getUserDetailsFromToken(extractedToken, TokenType.ACCESS_TOKEN);
    return ResponseEntity.ok().body(
      ResponseObject.builder()
        .message("Get user's detail successfully")
        .data(EmployeeResponse.fromEmployee(employee))
        .status(HttpStatus.OK)
        .build()
    );
  }

}
