package com.dom.employeemanager.repo;

import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.models.OtpCode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
  Optional<OtpCode> findByEmailAndOtpCode(String email, String otpCode);

  Optional<OtpCode> findByEmail(String email);

  @Transactional
  void deleteByEmail(String email);
}
