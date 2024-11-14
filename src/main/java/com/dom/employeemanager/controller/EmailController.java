package com.dom.employeemanager.controller;

import com.dom.employeemanager.dtos.OtpCodeDTO;
import com.dom.employeemanager.dtos.ResetPasswordDTO;
import com.dom.employeemanager.repo.EmployeeRepo;
import com.dom.employeemanager.responses.ResponseObject;
import com.dom.employeemanager.service.EmailService;
import com.dom.employeemanager.service.EmployeeService;
import com.dom.employeemanager.service.OtpCodeService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/email")
@RequiredArgsConstructor
public class EmailController {

  private final EmailService emailService;
  private final EmployeeService employeeService;
  private final PasswordEncoder passwordEncoder;
  private final EmployeeRepo employeeRepo;

//  @PostMapping("/send-reset-password")
//  public ResponseEntity<ResponseObject> sendResetPasswordEmail(@RequestBody ResetPasswordDTO resetPasswordDTO) {
//    Optional<Employee> existingEmployee = employeeService.findByEmailAndPhone(
//      resetPasswordDTO.getEmail(),
//      resetPasswordDTO.getPhone()
//    );
//
//    if (existingEmployee.isEmpty()) {
//      throw new UserNotFoundException("User not found");
//    }
//
//    if (!existingEmployee.get().isActive()) {
//      throw new UserNotActiveException("User not active");
//    }
//
//    String newPassword = generateNewPassword();
//    existingEmployee.get().setPassword(passwordEncoder.encode(newPassword));
//
//    // if User exist, update new password
//    employeeRepo.saveAndFlush(existingEmployee.get());
//    // Sending email to the user
//    emailService.sendSimpleEmail(resetPasswordDTO.getEmail(), "Reset Password", "Your new password is: " + newPassword);
//    return ResponseEntity.ok().body(ResponseObject.builder()
//      .message("Reset password successfully. Please check your email.")
//      .status(HttpStatus.OK)
//      .build());
//  }
//
//  private String generateNewPassword() {
//    return UUID.randomUUID().toString().substring(0, 5);
//  }

  private final OtpCodeService otpCodeService;

  // OTP
  @PostMapping("/otp/send")
  public ResponseEntity<ResponseObject> sendOtp(@RequestBody OtpCodeDTO otpCodeDTO) {
    try {
      String optCode = otpCodeService.generateAndSendOtpCode(otpCodeDTO.getEmail(), otpCodeDTO.getPhone());
      return ResponseEntity.ok().body(
        ResponseObject.builder()
          .message("OTP has been sent to your email.")
          .data(optCode)
          .status(HttpStatus.OK)
          .build()
      );
    } catch (MessagingException e) {
      return ResponseEntity.ok().body(
        ResponseObject.builder()
          .message(ResponseEntity.status(500).toString())
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .build()
      );
    }
  }

  @PostMapping("/otp/validate")
  public ResponseEntity<ResponseObject> validateOtp(@RequestBody OtpCodeDTO otpCodeDTO) {
    boolean isValid = otpCodeService.validateOtp(otpCodeDTO.getEmail(), otpCodeDTO.getOtp());
    if (isValid) {
      return ResponseEntity.ok(ResponseObject.builder()
        .message("Otp is valid")
        .status(HttpStatus.OK)
        .build()
      );
    } else {
      return ResponseEntity.internalServerError().body(
        ResponseObject.builder()
          .message("Invalid or expired OTP.")
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .build()
      );
    }
  }

  @PostMapping("/otp/resetPassword")
  public ResponseEntity<ResponseObject> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
    boolean result = otpCodeService.resetPassword(resetPasswordDTO);
    if (result) {
      return ResponseEntity.ok(ResponseObject.builder()
        .message("Mật khẩu đã được đặt lại thành công.")
        .status(HttpStatus.OK)
        .build()
      );
    } else {
      return ResponseEntity.internalServerError().body(
        ResponseObject.builder()
          .message("Có lỗi xảy ra khi đặt lại mật khẩu.")
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .build()
      );
    }
  }
}


