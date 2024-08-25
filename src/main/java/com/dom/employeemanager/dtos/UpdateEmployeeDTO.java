package com.dom.employeemanager.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateEmployeeDTO {
  private String name;
  private String phone;
  private String email;

  @JsonProperty("password")
  private String password;

  @JsonProperty("retype_password")
  private String retypePassword;

}
