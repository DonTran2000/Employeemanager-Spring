package com.dom.employeemanager.controller;

import com.dom.employeemanager.dtos.EmployeeDTO;
import com.dom.employeemanager.dtos.EmployeeLoginDTO;
import com.dom.employeemanager.dtos.RefreshTokenDTO;
import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.models.Token;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/employeemanager")
@RequiredArgsConstructor
public class EmployeeManagerController {

  private final EmployeeService employeeService;
  private final TokenService tokenService;

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

  @GetMapping("/all")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<List<Employee>> getAllEmployees() {
    List<Employee> employees = employeeService.findAllUsers();
    return new ResponseEntity<>(employees, HttpStatus.OK);
  }

  @GetMapping("/find/{id}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
    Employee employee = employeeService.findEmployeeById(id);
    return new ResponseEntity<>(employee, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/add")
  public ResponseEntity<ResponseObject> addEmployee(@RequestBody EmployeeDTO employeeDTO) throws Exception {
    if (employeeDTO.getEmail() == null || employeeDTO.getEmail().trim().isBlank()) {
      if (employeeDTO.getPhone() == null || employeeDTO.getPhone().isBlank()) {
        return ResponseEntity.badRequest().body(ResponseObject.builder().status(HttpStatus.BAD_REQUEST).data(null).message("At least email or phone number is required").build());
      }
    }
    if (!employeeDTO.getPassword().equals(employeeDTO.getRetypePassword())) {
      //registerResponse.setMessage();
      return ResponseEntity.badRequest().body(ResponseObject.builder().status(HttpStatus.BAD_REQUEST).data(null).message("Password not match!").build());
    }
    Employee employee = employeeService.createEmployee(employeeDTO);
    return ResponseEntity.ok(ResponseObject.builder().status(HttpStatus.CREATED).data(employee).message("Account registration successful").build());
  }

  @PutMapping("/block/{userId}/{active}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<ResponseObject> blockOrEnable(@PathVariable long userId, @PathVariable int active) throws Exception {
    employeeService.blockOrEnable(userId, active > 0);
    String message = active > 0 ? "Successfully enabled the user." : "Successfully blocked the user.";
    return ResponseEntity.ok().body(ResponseObject.builder().message(message).status(HttpStatus.OK).data(null).build());
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
}
