package com.dom.employeemanager.service;

import com.dom.employeemanager.models.Role;
import com.dom.employeemanager.repo.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
  private final RoleRepository roleRepository;

  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }
}
