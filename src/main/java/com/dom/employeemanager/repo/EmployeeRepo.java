package com.dom.employeemanager.repo;

import com.dom.employeemanager.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {
  @Query("SELECT e FROM Employee e WHERE e.role.name = 'USER'")
  List<Employee> findAllUsers();

  void deleteEmployeeById(Long id);

  Optional<Employee> findByPhone(String phone);

  Optional<Employee> findByEmail(String email);

  Optional<Employee> findEmployeeById(Long id);

  boolean existsByPhone(String phone);

  boolean existsByEmail(String email);

  Optional<Employee> findByPhoneOrEmail(String phone, String email);

  Optional<Employee> findByEmailAndPhone(String email, String phone);
}
