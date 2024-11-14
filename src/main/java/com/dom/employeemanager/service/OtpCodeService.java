package com.dom.employeemanager.service;

import com.dom.employeemanager.dtos.ResetPasswordDTO;
import com.dom.employeemanager.exception.UserNotFoundException;
import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.models.OtpCode;
import com.dom.employeemanager.repo.EmployeeRepo;
import com.dom.employeemanager.repo.OtpCodeRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpCodeService {
  private final OtpCodeRepository otpCodeRepository;
  private final EmployeeRepo employeeRepo;
  private final EmailService emailService;
  private final PasswordEncoder passwordEncoder;

  @Value("${otp.expiration.time}")
  private long otpExpirationTime;  // Đơn vị: phút

  public String generateAndSendOtpCode(String email, String phone) throws MessagingException {
    // check db
    Optional<Employee> existingEmployee = employeeRepo.findByEmailAndPhone(email, phone);

    if (existingEmployee.isPresent()) {
      // Kiểm tra nếu OTP cũ có tồn tại trước khi xoá
      Optional<OtpCode> existingOtp = otpCodeRepository.findByEmail(email);
      if (existingOtp.isPresent()) {
        // Xoá OTP cũ nếu có
        otpCodeRepository.deleteByEmail(email);
      }

      // Tạo OTP mới
      String otpCode = String.format("%06d", new Random().nextInt(999999));
      LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(otpExpirationTime);

      // Lưu OTP vào database
      OtpCode optCodeNew = OtpCode.builder()
        .email(email)
        .otpCode(otpCode)
        .expirationTime(expirationTime)
        .build();

      otpCodeRepository.save(optCodeNew);

      // Gửi OTP qua email
      String message = "Your OTP code is: " + otpCode + ". It will expire in " + otpExpirationTime + " minutes.";
      emailService.sendOtpMessage(email, "OTP Code", message);
      return otpCode;
    } else {
      throw new UserNotFoundException("User not existing");
    }
  }

  public boolean validateOtp(String email, String otpCode) {
    Optional<OtpCode> existingOtpCode = otpCodeRepository.findByEmailAndOtpCode(email, otpCode);
    if (existingOtpCode.isPresent()) {
      if (existingOtpCode.get().getExpirationTime().isAfter(LocalDateTime.now())) {
        return true;  // OTP hợp lệ
      } else {
        otpCodeRepository.deleteByEmail(email);  // Xoá OTP hết hạn
      }
    }
    return false;  // OTP không hợp lệ
  }

  public boolean resetPassword(ResetPasswordDTO resetPasswordDTO) {
    Optional<OtpCode> emailAndOtpCode =
      otpCodeRepository.findByEmailAndOtpCode(resetPasswordDTO.getEmail(), resetPasswordDTO.getOtp());

    // From otp table, get Email
    if (emailAndOtpCode.isPresent()){
      String existingEmail = emailAndOtpCode.get().getEmail();
      Optional<Employee> existingEmployee = employeeRepo.findByEmail(existingEmail);
      existingEmployee.ifPresent(employee -> {
        // Set the new password
        employee.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));

        // Save the updated employee in the database
        employeeRepo.save(employee);
      });
      return true;
    }
    return false;
  }
}
