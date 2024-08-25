package com.dom.employeemanager.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class OtpCodeDTO {
  private String email;
  private String otp;
}
