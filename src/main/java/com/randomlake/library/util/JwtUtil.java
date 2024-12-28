package com.randomlake.library.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret.key:}")
  private String base64KeyFromProperties;

  private Key key;
  private final long expirationMs = 86400000;

  @PostConstruct
  public void init() {
    if (base64KeyFromProperties.isBlank()) {
      System.out.println("JWT_SECRET_KEY not found. Generating a temporary key for development.");
      key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    } else {
      key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64KeyFromProperties));
    }
  }

  public String generateToken(String username) {
    return Jwts.builder()
        .subject(username)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expirationMs))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SignatureException e) {
      System.out.println("Invalid JWT signature: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Invalid JWT token: " + e.getMessage());
    }
    return false;
  }

  public String extractUsername(String token) {
    Claims claims = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
    return claims.getSubject(); // The subject field is the username
  }
}
