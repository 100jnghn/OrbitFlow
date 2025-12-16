package com.finalproj.orbitflow.global.security.jwt;

import com.finalproj.orbitflow.global.security.SecurityUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : JwtProvider
 * @since : 2025-12-16 화요일
 */
@Component
public class JwtProvider {

    private final JwtProperties properties;
    private final Key key;

    public JwtProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(
                properties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * JWT 생성
     */
    public String createToken(SecurityUser user) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpiration());

        return Jwts.builder()
                .setSubject(String.valueOf(user.getEmployeeId()))
                .addClaims(Map.of("companyId", user.getCompanyId()))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getEmployeeId(String token) {
        return Long.valueOf(parse(token).getBody().getSubject());
    }

    public Long getCompanyId(String token) {
        Object v = parse(token).getBody().get("companyId");
        if (v == null) return null;
        return Long.valueOf(String.valueOf(v));
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
