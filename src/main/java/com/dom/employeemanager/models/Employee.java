package com.dom.employeemanager.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee implements Serializable, UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, updatable = false)
  private Long id;
  private String name;
  private String email;
  private String phone;
  private String password;
  private String jobTitle;
  private String imageUrl;
  @Column(name = "is_active")
  private boolean active;
  @Column(nullable = false, updatable = false)
  private String employeeCode;
  @ManyToOne
  @JoinColumn(name = "role_id")
  private Role role;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
    authorityList.add(new SimpleGrantedAuthority("ROLE_" + getRole().getName()));
    return authorityList;
  }

  @Override
  public String getUsername() {
    if (phone != null && !phone.isEmpty()) {
      return phone;
    } else if (email != null && !email.isEmpty()) {
      return email;
    }
    return "";
  }

}
