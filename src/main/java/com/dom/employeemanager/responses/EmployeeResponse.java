package com.dom.employeemanager.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponse {
  @JsonProperty("id")
  private Long id;

  private String name;

  @JsonProperty("is_active")
  private boolean active;

  @JsonProperty("role")
  private com.dom.employeemanager.models.Role role;

  public static EmployeeResponse fromEmployee(com.dom.employeemanager.models.Employee employee) {
    return EmployeeResponse.builder()
      .id(employee.getId())
      .name(employee.getName())
      .active(employee.isActive())
      .role(employee.getRole())
      .build();
  }
}
