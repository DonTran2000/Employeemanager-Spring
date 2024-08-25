package com.dom.employeemanager.repo;

import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
  Token findByToken(String token);

  List<Token> findByEmployee(Employee employee);

  Token findByRefreshToken(String refreshToken);
}
