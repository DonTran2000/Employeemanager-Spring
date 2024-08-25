package com.dom.employeemanager.service;

import com.dom.employeemanager.components.JwtTokenUtils;
import com.dom.employeemanager.dtos.EmployeeDTO;
import com.dom.employeemanager.dtos.EmployeeLoginDTO;
import com.dom.employeemanager.dtos.UpdateEmployeeDTO;
import com.dom.employeemanager.exception.EmailAlreadyExistsException;
import com.dom.employeemanager.exception.PhoneNumberAlreadyExistsException;
import com.dom.employeemanager.exception.UserNotFoundException;
import com.dom.employeemanager.exception.WrongPhoneNumberOrPasswordException;
import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.models.Role;
import com.dom.employeemanager.models.Token;
import com.dom.employeemanager.repo.EmployeeRepo;
import com.dom.employeemanager.repo.OtpCodeRepository;
import com.dom.employeemanager.repo.RoleRepository;
import com.dom.employeemanager.repo.TokenRepository;
import com.dom.employeemanager.utils.TokenType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepo employeeRepo;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenUtils jwtTokenUtil;
  private final RoleRepository roleRepository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final OtpCodeRepository otpCodeRepository;

  public List<Employee> findAllUsers() {
    return employeeRepo.findAllUsers();
  }

  @Transactional
  public Employee updateEmployee(Long id, UpdateEmployeeDTO updatedEmployeeDTO) throws Exception {
    // Find the existing user by userId
    Employee existingEmployee = employeeRepo.findById(id)
      .orElseThrow(() -> new UserNotFoundException("User not found"));

    // Update user information based on the DTO
    if (updatedEmployeeDTO.getName() != null) {
      existingEmployee.setName(updatedEmployeeDTO.getName());
    }

    // Update the password if it is provided in the DTO
    if (updatedEmployeeDTO.getPassword() != null && !updatedEmployeeDTO.getPassword().isEmpty()) {
      if (!updatedEmployeeDTO.getPassword().equals(updatedEmployeeDTO.getRetypePassword())) {
        throw new Exception("Password and retype password not the same");
      }
      String newPassword = updatedEmployeeDTO.getPassword();
      String encodedPassword = passwordEncoder.encode(newPassword);
      existingEmployee.setPassword(encodedPassword);
    }
    // Save the updated user
    return employeeRepo.save(existingEmployee);
  }

  public Employee findEmployeeById(Long id) {
    return employeeRepo.findEmployeeById(id)
      .orElseThrow(() -> new UserNotFoundException("User by id " + id + " was not found"));
  }

  public void deleteEmployeeById(Long id) {
    employeeRepo.deleteById(id);
  }

  @Transactional
  public Employee createEmployee(EmployeeDTO employeeDTO) throws Exception {
    //register user
    if (!employeeDTO.getPhone().isBlank() && employeeRepo.existsByPhone(employeeDTO.getPhone())) {
      throw new PhoneNumberAlreadyExistsException("Phone number already exists");
    }
    if (!employeeDTO.getEmail().isBlank() && employeeRepo.existsByEmail(employeeDTO.getEmail())) {
      throw new EmailAlreadyExistsException("Email already exists");
    }
    Role role = roleRepository.findById(employeeDTO.getRoleId())
      .orElseThrow(() -> new Exception("ROLE_DOES_NOT_EXISTS"));
    if (role.getName().equalsIgnoreCase(Role.ADMIN)) {
      throw new Exception("Registering admin accounts is not allowed");
    }

    //convert from userDTO => user
    Employee newEmployee = Employee.builder()
      .employeeCode(UUID.randomUUID().toString())
      .name(employeeDTO.getName())
      .phone(employeeDTO.getPhone())
      .email(employeeDTO.getEmail())
      .jobTitle(employeeDTO.getJobTitle())
      .active(employeeDTO.isActive())
      .build();

    newEmployee.setRole(role);

    String password = employeeDTO.getPassword();
    String encodedPassword = passwordEncoder.encode(password);
    newEmployee.setPassword(encodedPassword);

    return employeeRepo.save(newEmployee);
  }

  public String login(EmployeeLoginDTO employeeLoginDTO) throws Exception {
    Optional<Employee> optionalEmployee = Optional.empty();
    String subject = null;
    if (employeeLoginDTO.getPhone() != null) {
      optionalEmployee = employeeRepo.findByPhone(employeeLoginDTO.getPhone());
      subject = employeeLoginDTO.getPhone();
    }

    if (employeeLoginDTO.getEmail() != null) {
      optionalEmployee = employeeRepo.findByEmail(employeeLoginDTO.getEmail());
      subject = employeeLoginDTO.getEmail();
    }

    if (optionalEmployee.isEmpty()) {
      throw new WrongPhoneNumberOrPasswordException("Wrong phone number or password");
    }

    Employee existingEmployee = optionalEmployee.get();

    // Check password
    if (!passwordEncoder.matches(employeeLoginDTO.getPassword(), existingEmployee.getPassword())) {
      throw new WrongPhoneNumberOrPasswordException("Wrong phone number or password");
    }

    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
      subject,
      employeeLoginDTO.getPassword(),
      existingEmployee.getAuthorities()
    );
    //authenticate with Java Spring security
    authenticationManager.authenticate(authenticationToken);

    // tạo ra refreshToken luôn
    jwtTokenUtil.generateRefreshToken(existingEmployee);

    return jwtTokenUtil.generateToken(existingEmployee);
  }

  @Transactional
  public void resetPassword(Long id, String newPassword) throws Exception {
    Employee existingEmployee = employeeRepo.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    String encodedPassword = passwordEncoder.encode(newPassword);
    existingEmployee.setPassword(encodedPassword);
    employeeRepo.save(existingEmployee);

    //reset password => clear token
    List<Token> tokens = tokenRepository.findByEmployee(existingEmployee);
    for (Token token : tokens) {
      tokenRepository.delete(token);
    }

    // Sau khi đặt lại mật khẩu thành công, xóa OTP để tránh sử dụng lại
    otpCodeRepository.deleteByEmail(existingEmployee.getEmail());
  }

  public Employee getUserDetailsFromToken(String token, TokenType type) throws Exception {
    if (jwtTokenUtil.isTokenExpired(token, type)) {
      throw new Exception("Token is expired");
    }
    String subject = jwtTokenUtil.getSubject(token, type);
    Optional<Employee> employee;
    employee = employeeRepo.findByPhone(subject);
    if (employee.isEmpty()) {
      employee = employeeRepo.findByEmail(subject);
    }
    return employee.orElseThrow(() -> new Exception("User not found"));
  }

  public Employee getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
    Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
    return getUserDetailsFromToken(existingToken.getToken(), TokenType.ACCESS_TOKEN);
  }

  @Transactional
  public void blockOrEnable(Long employeeId, boolean active) throws Exception {
    Employee existingEmployee = employeeRepo.findById(employeeId)
      .orElseThrow(() -> new Exception("User not found"));
    // if active > 0 ? block : enable
    existingEmployee.setActive(!active);

    employeeRepo.save(existingEmployee);
  }

  public Optional<Employee> findByEmailAndPhone(String email, String phone) {
    return employeeRepo.findByEmailAndPhone(email, phone);
  }
}
