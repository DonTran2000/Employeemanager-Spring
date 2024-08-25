package com.dom.employeemanager.repo;

import com.dom.employeemanager.models.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
  Optional<OtpCode> findByEmailAndOtpCode(String email, String otpCode);

  void deleteByEmail(String email);
}
