package com.dom.employeemanager.components;

import com.dom.employeemanager.repo.TokenRepository;
import com.dom.employeemanager.utils.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.dom.employeemanager.utils.TokenType.ACCESS_TOKEN;
import static com.dom.employeemanager.utils.TokenType.REFRESH_TOKEN;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
  @Value("${jwt.expiryHour}")
  private Long expiryHour;

  @Value("${jwt.expiryDay}")
  private Long expiryDay;

  @Value("${jwt.secretKey}")
  private String secretKey;

  @Value("${jwt.refreshKey}")
  private String refreshKey;

  private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtils.class);
  private final TokenRepository tokenRepository;

  public String extractUsername(String token, TokenType type) {
    return extractClaim(token, type, Claims::getSubject);
  }

  public boolean isValid(String token, TokenType type, UserDetails userDetails) {
    final String username = extractUsername(token, type);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token, type);
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  private String generateToken(Map<String, Object> claims, UserDetails userDetails) {
    return Jwts.builder()
      .setClaims(claims)
      .setSubject(userDetails.getUsername())
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * expiryHour))
      .signWith(getSignInKey(ACCESS_TOKEN), SignatureAlgorithm.HS256)
      .compact();
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return generateRefreshToken(new HashMap<>(), userDetails);
  }

  private String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
    return Jwts.builder()
      .setClaims(claims)
      .setSubject(userDetails.getUsername())
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * expiryDay))
      .signWith(getSignInKey(REFRESH_TOKEN), SignatureAlgorithm.HS256)
      .compact();
  }

  private Key getSignInKey(TokenType type) {
    byte[] bytes;
    if (ACCESS_TOKEN.equals(type)) {
      bytes = Decoders.BASE64.decode(secretKey);
    } else {
      bytes = Decoders.BASE64.decode(refreshKey);
    }

    return Keys.hmacShaKeyFor(bytes);
  }

  private Claims extractAllClaims(String token, TokenType type) {
    return Jwts.parserBuilder()
      .setSigningKey(getSignInKey(type))
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  public <T> T extractClaim(String token, TokenType type, Function<Claims, T> claimsResolver) {
    final Claims claims = this.extractAllClaims(token, type);
    return claimsResolver.apply(claims);
  }

  //check expiration
  public boolean isTokenExpired(String token, TokenType type) {
    Date expirationDate = this.extractClaim(token, type, Claims::getExpiration);
    return expirationDate.before(new Date());
  }

  public String getSubject(String token, TokenType type) {
    return extractClaim(token, type, Claims::getSubject);
  }

}
