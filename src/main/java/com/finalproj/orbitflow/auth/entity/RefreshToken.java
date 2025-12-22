package com.finalproj.orbitflow.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RefreshToken
 * @since : 2025-12-18 목요일
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    public RefreshToken(Long companyId, Long employeeId, String token, Instant expiresAt) {
        this.companyId = companyId;
        this.employeeId = employeeId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
