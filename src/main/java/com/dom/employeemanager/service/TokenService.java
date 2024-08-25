package com.dom.employeemanager.service;

import com.dom.employeemanager.components.JwtTokenUtils;
import com.dom.employeemanager.models.Employee;
import com.dom.employeemanager.models.Token;
import com.dom.employeemanager.repo.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {
  private static final int MAX_TOKENS = 3;
  @Value("${jwt.expiryHour}")
  private long expiryHour; //save to an environment variable

  @Value("${jwt.expiryDay}")
  private long expiryDay;

  private final TokenRepository tokenRepository;
  private final JwtTokenUtils jwtTokenUtil;

  @Transactional
  public Token addToken(Employee employee, String token, boolean isMobileDevice) {
    List<Token> employeeTokens = tokenRepository.findByEmployee(employee);
    int tokenCount = employeeTokens.size();

    // Số lượng token vượt quá giới hạn, xóa một token cũ
    if (tokenCount >= MAX_TOKENS) {
      //kiểm tra xem trong danh sách userTokens có tồn tại ít nhất
      //một token không phải là thiết bị di động (non-mobile)
      boolean hasNonMobileToken = !employeeTokens.stream().allMatch(Token::isMobile);
      Token tokenToDelete;
      if (hasNonMobileToken) {
        tokenToDelete = employeeTokens.stream()
          .filter(userToken -> !userToken.isMobile())
          .findFirst()
          .orElse(employeeTokens.get(0));
      } else {
        //tất cả các token đều là thiết bị di động,
        //chúng ta sẽ xóa token đầu tiên trong danh sách
        tokenToDelete = employeeTokens.get(0);
      }
      tokenRepository.delete(tokenToDelete);
    }
    long expirationInSeconds = 60 * 60 * expiryHour;
    LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expirationInSeconds);
    // Tạo mới một token cho người dùng
    Token newToken = Token.builder()
      .employee(employee)
      .token(token)
      .revoked(false)
      .expired(false)
      .tokenType("Bearer")
      .expirationDate(expirationDateTime)
      .isMobile(isMobileDevice)
      .build();

    // sau khi tạo token mới xong, thì tạo refreshToken mới
    String newRefreshToken = jwtTokenUtil.generateRefreshToken((UserDetails) employee);

    newToken.setRefreshToken(newRefreshToken);
    newToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(60 * 60 * 24 * expiryDay));
    tokenRepository.save(newToken);
    return newToken;
  }

  @Transactional
  public Token refreshToken(String refreshToken, Employee employee) throws Exception {
    Token existingRefreshToken = tokenRepository.findByRefreshToken(refreshToken);
    if (existingRefreshToken == null) {
      throw new Exception("Refresh token does not exist");
    }
    if (existingRefreshToken.getRefreshExpirationDate().compareTo(LocalDateTime.now()) < 0) {
      tokenRepository.delete(existingRefreshToken);
      throw new Exception("Refresh token is expired");
    }
    // Thiết lập lại Token mới
    String newToken = jwtTokenUtil.generateToken(employee);
    LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(60 * 60 * expiryHour); // Vì token nên 1h
    existingRefreshToken.setExpirationDate(expirationDateTime);
    existingRefreshToken.setToken(newToken);

    // Thiết lập lại Refresh Token mới
    String newRefreshToken = jwtTokenUtil.generateRefreshToken(employee);

    existingRefreshToken.setRefreshToken(newRefreshToken);

    // vì refreshToken nên 1 day
    LocalDateTime refreshExpirationDate = LocalDateTime.now().plusSeconds(60 * 60 * 24 * expiryDay);
    existingRefreshToken.setRefreshExpirationDate(refreshExpirationDate);
    return existingRefreshToken;
  }
}
