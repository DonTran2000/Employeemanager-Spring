package com.dom.employeemanager.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeDTO {
  private String name;
  private String phone = "";
  private String email = "";
  private String password = "";
  @JsonProperty("retype_password")
  private String retypePassword = "";

  @JsonProperty("job_title")
  private String jobTitle;

  private String employeeCode;

  @JsonProperty("role_id")
  //role admin not permitted
  private Integer roleId;

  @JsonProperty("is_active")
  private boolean active = true;

  public String createEmployeeCode() {
    return UUID.randomUUID().toString();
  }
}
