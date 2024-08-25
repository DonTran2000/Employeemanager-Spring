package com.dom.employeemanager.service;

import com.dom.employeemanager.models.OtpCode;
import com.dom.employeemanager.repo.OtpCodeRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpCodeService {
  private final OtpCodeRepository otpCodeRepository;

  private final EmailService emailService;

  @Value("${otp.expiration.time}")
  private long otpExpirationTime;  // Đơn vị: phút

  public void generateAndSendOtpCode(String email) throws MessagingException {
    // Xoá OTP cũ nếu tồn tại
    otpCodeRepository.deleteByEmail(email);

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
}
