package com.dom.employeemanager.dtos;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordDTO {
  private String email;
  private String otp;
  private String newPassword;
}
